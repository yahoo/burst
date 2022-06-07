/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.lattice.traveler

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.types.BrioPath.BrioPathName
import org.burstsys.brio.types.BrioTypes.BrioVersionKey
import org.burstsys.felt.model.lattice.FeltLatTravGenContext
import org.burstsys.felt.model.schema.traveler.{FeltSearchMethod, FeltTraveler}
import org.burstsys.felt.model.sweep.splice._
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.code._

import scala.language.postfixOps

/**
 * generate code to implement a [[FeltTraveler]]
 * for a specific brio schema that traverses a specific reference scalar node/relation
 */
trait FeltLatRefScalTravGen extends Any {

  self: FeltLatTravGenContext =>

  final
  def processRefScal(treeNode: BrioNode)(implicit cursor: FeltCodeCursor): FeltCode = {
    val instance = latticeRelationViaSchemaRuntime(treeNode.pathName)

    val tunnels = tunnelReferenceScalarRelations(treeNode)(cursor indentRight)

    def refScalTunnels(implicit cursor: FeltCodeCursor): FeltCode = if (tunnels.isEmpty) "" else
      s"""|
          |$I{ // BEGIN reference scalar tunnels for '${treeNode.pathName}'
          |$tunnels
          |$I} // END reference scalar tunnels for '${treeNode.pathName}' """.stripMargin

    val relations = processRefScalVersions(treeNode)(cursor indentRight)

    def refScalRelations(implicit cursor: FeltCodeCursor): FeltCode =
      s"""|
          |$I{ // BEGIN reference scalar relations for '${treeNode.pathName}'
          |$I2$instance.versionKey($blobReaderSym) match {
          |$relations
          |$I2}
          |$I} // END reference scalar relations for '${treeNode.pathName}' """.stripMargin

    s"""|
        |$refScalTunnels
        |$refScalRelations""".stripMargin
  }

  final
  def dynamicRelationsVisit(treeNode: BrioNode)(implicit cursor: FeltCodeCursor): FeltCode = {
    val placeName = FeltDynamicVisitPlace.toString
    val placeKey = FeltDynamicVisitPlace.key
    s"""|
        |${I}$schemaSweepSym.dynamicRelationSplices( $schemaRuntimeSym, ${treeNode.pathKey}, $placeKey ); // '${treeNode.pathName}' '$placeName'""".stripMargin
  }

  final
  def dynamicRelationsJoin(treeNode: BrioNode)(implicit cursor: FeltCodeCursor): FeltCode = {
    val placeName = FeltDynamicJoinPlace.toString
    val placeKey = FeltDynamicJoinPlace.key
    s"""|
        |${I}$schemaSweepSym.dynamicRelationSplices( $schemaRuntimeSym, ${treeNode.pathKey}, $placeKey  ); // '${treeNode.pathName}'  '$placeName'""".stripMargin
  }

  final
  def dynamicRelationsCleanup(treeNode: BrioNode)(implicit cursor: FeltCodeCursor): FeltCode = {
    val placeName = FeltDynamicCleanupPlace.toString
    val placeKey = FeltDynamicCleanupPlace.key
    s"""|
        |${I}$schemaSweepSym.dynamicRelationSplices( $schemaRuntimeSym, ${treeNode.pathKey}, $placeKey  ); // '${treeNode.pathName}'  '$placeName'""".stripMargin
  }

  final
  def processRefScalChildRel(parentNode: BrioNode, version: BrioVersionKey, childNode: BrioNode)
                            (implicit cursor: FeltCodeCursor): FeltCode = {
    val parentSchema = schematic(parentNode.relation.referenceStructure.structureTypeName, version)
    val parentInstance = latticeRelationViaSchemaRuntime(parentNode.pathName)
    val childInstance = latticeRelationViaSchemaRuntime(childNode.pathName)
    val childInstanceIsNull = latticeRelationIsNullViaSchemaRuntime(childNode.pathName)
    val childPathName = childNode.pathName
    val childPathKey = childNode.pathKey
    val childOrdinal = childNode.relation.relationOrdinal
    val header = s"reference-scalar path='$childPathName:$childPathKey' version=$version ordinal=$childOrdinal"
    s"""|
        |${C(s"START $header")}
        |${I}if ($schemaSweepSym.$skipVisitPathSym($childPathKey) || $parentInstance.relationIsNull($blobReaderSym, $parentSchema, $childOrdinal)) {
        |${I2}if ($schemaSweepSym.$skipTunnelPathSym($childPathKey)) {
        |$I3$childInstanceIsNull = true;
        |$I2}
        |$I} else {
        |$I2$childInstanceIsNull = false;
        |$I2$childInstance = $parentInstance.referenceScalar($blobReaderSym, $parentSchema, $childOrdinal);
        |${processRefScal(childNode)(cursor indentRight 1)}
        |$I} // END $header""".stripMargin
  }

  private
  def processRefScalVersions(treeNode: BrioNode)(implicit cursor: FeltCodeCursor): FeltCode = {
    val pathName = treeNode.pathName
    val pathKey = treeNode.pathKey

    def visitRelations(pathName: BrioPathName, version: BrioVersionKey)(implicit cursor: FeltCodeCursor): FeltCode = {
      val staticVisits = mergeStatRefScalChildren(treeNode, version)
      val dynamicVisits = dynamicRelationsVisit(treeNode)
      s"""|
          |${C1(s"START CHILD RELATION VISITS (BEFORE POST) '$pathName' ")}
          |${C1(s"visit static child relation(s) before dynamic ones ")}
          |$staticVisits
          |${C1(s"visit dynamic child relation(s) after static ones ")}
          |$dynamicVisits
          |${C1(s"END CHILD RELATION VISITS '$pathName'")} """.stripMargin
    }

    def joinRelations(pathName: BrioPathName, version: BrioVersionKey)(implicit cursor: FeltCodeCursor): FeltCode = {
      val staticJoins = joinRefScalChildren(treeNode, version)
      val dynamicJoins = dynamicRelationsJoin(treeNode)
      s"""|
          |${C1(s"START CHILD RELATION JOINS (AFTER POST) '$pathName'")}
          |${C1(s"join static child relation(s) before dynamic ones ")}
          |$staticJoins
          |${C1(s"join dynamic child relation(s) after static ones ")}
          |$dynamicJoins
          |${C1(s"END CHILD RELATION JOINS '$pathName'")}
          |${C1(s"DYNAMIC RELATIONS CLEANUP '$pathName'")}
          |${dynamicRelationsCleanup(treeNode)}""".stripMargin
    }

    (for (version <- 1 to cursor.schema.brioSchema.versionCount) yield {

      val methodName = s"search_v${version}_${pathName.replace(".", "_")}"

      val method = FeltSearchMethod(generator.runtimeClassName, methodName, {
        implicit cursor =>
          s"""|
              |$I2$schemaSweepSym.referenceScalarSplice($schemaRuntimeSym, $pathKey, ${FeltInstanceAllocPlace.key}) // $pathName $FeltInstanceAllocPlace
              |$I2$schemaSweepSym.referenceScalarSplice($schemaRuntimeSym, $pathKey, ${FeltInstancePrePlace.key}) // $pathName $FeltInstancePrePlace
              |${visitRelations(pathName, version)(cursor indentRight)}
              |$I2$schemaSweepSym.referenceScalarSplice($schemaRuntimeSym, $pathKey, ${FeltInstancePostPlace.key}) // $pathName $FeltInstancePostPlace
              |${joinRelations(pathName, version)(cursor indentRight)}
              |$I2$schemaSweepSym.referenceScalarSplice($schemaRuntimeSym, $pathKey, ${FeltInstanceFreePlace.key}) // $pathName $FeltInstanceFreePlace""".stripMargin
      })

      generator += method

      s"""|
          |${I2}case $version ⇒ { // schema version $version
          |$I3${method.methodCall};
          |$I2}""".stripMargin

    }).mkString

  }

}
