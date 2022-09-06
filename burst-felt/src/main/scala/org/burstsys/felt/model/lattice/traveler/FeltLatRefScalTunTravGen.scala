/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.lattice.traveler

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types.{BrioReferenceScalarRelation, BrioReferenceVectorRelation, BrioValueMapRelation, BrioValueVectorRelation}
import org.burstsys.brio.types.BrioTypes.BrioVersionKey
import org.burstsys.felt.model.lattice.FeltLatTravGenContext
import org.burstsys.felt.model.schema.traveler.{FeltSearchMethod, FeltTraveler}
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, FeltNoCode, I, I2}

import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

/**
 * generate code to implement a [[FeltTraveler]]
 * for a specific brio schema that manages a specific sequence of reference scalar relationships
 * so they can be accessed directly in a closure using a.b.c notation
 * This is support for the ''following'' of a sequence or path
 * of one or more linked scalar references. We have a rule
 * where you can always see through a sequence of scalar references so we have to
 * make sure those variables are setup in case they are needed.
 * We have a special lookup generated into the FeltSweep that is used
 * to determine if these tunnels are used in a specific analysis so we
 * can avoid wasting the time where not needed.
 *
 */
trait FeltLatRefScalTunTravGen extends Any {

  self: FeltLatTravGenContext =>

  final
  def tunnelReferenceScalarRelations(treeNode: BrioNode)(implicit cursor: FeltCodeCursor): FeltCode = {
    val instance = latticeRelationViaSchemaRuntime(treeNode.pathName)
    val tunnelVersions = tunnelReferenceScalarRelationsVersions(treeNode)(cursor indentRight)
    if (tunnelVersions.isEmpty) return FeltNoCode
    s"""|
        |${resetRelationsToNulls(treeNode)}
        |$I$instance.versionKey($blobReaderSym) match {
        |$tunnelVersions
        |$I}""".stripMargin
  }

  private
  def tunnelReferenceScalarRelationsVersions(treeNode: BrioNode)(implicit cursor: FeltCodeCursor): FeltCode = (
    for (version <- 1 to cursor.schema.brioSchema.versionCount) yield {
      val tunnelVersion = tunnelReferenceScalarRelationVersion(treeNode, version)(cursor indentRight)
      if (tunnelVersion.isEmpty) return FeltNoCode
      s"""|
          |${I}case $version => { // schema version $version
          |$tunnelVersion
          |$I}""".stripMargin
    }).mkString

  private
  def tunnelReferenceScalarRelationVersion(parentNode: BrioNode, version: BrioVersionKey)(implicit cursor: FeltCodeCursor): FeltCode = {
    val parentSchematic = schematic(parentNode.relation.referenceStructure.structureTypeName, version)
    val parentRelation = latticeRelationViaSchemaRuntime(parentNode.pathName)
    val childNodes = parentNode.childrenForVersionAndForms(version, BrioReferenceScalarRelation)
    if (childNodes.isEmpty) return FeltNoCode

    // create the search methods
    val methods = new ArrayBuffer[FeltSearchMethod]
    childNodes foreach {
      childNode =>
        val pathName = childNode.pathName
        val pathKey = childNode.pathKey
        val relationOrdinal = childNode.relation.relationOrdinal
        val childRelation = latticeRelationViaSchemaRuntime(childNode.pathName)
        val childRelationInstanceIsNull = latticeRelationIsNullViaSchemaRuntime(childNode.pathName)
        val methodName = s"tunnel_v${version}_${pathName.replace(".", "_")}"
        methods += FeltSearchMethod(generator.runtimeClassName, methodName, {
          implicit cursor =>
            s"""|
                |${C(s"tunnel scalar reference path='$pathName:$pathKey' version='$version' ordinal=$relationOrdinal")}
                |${I}if ($schemaSweepSym.$skipTunnelPathSym($pathKey) || $parentRelation.relationIsNull($blobReaderSym, $parentSchematic, $relationOrdinal)) {
                |$I2$childRelationInstanceIsNull = true;
                |$I} else {
                |$I2$childRelationInstanceIsNull = false;
                |$I2$childRelation = $parentRelation.referenceScalar($blobReaderSym, $parentSchematic, $relationOrdinal);
                |${tunnelReferenceScalarRelations(childNode)(cursor indentRight 1)}
                |$I}""".stripMargin
        })
    }

    generator ++= methods.toList

    // return the search method calls
    methods.map {
      method =>
        s"""|
            |$I${method.methodCall};""".stripMargin
    }.mkString
  }

  private
  def resetRelationsToNulls(treeNode: BrioNode)(implicit cursor: FeltCodeCursor): FeltCode = {
    def nodeResets(implicit cursor: FeltCodeCursor): FeltCode = {
      val nodes = treeNode.descendantsForForms(BrioReferenceScalarRelation, BrioReferenceVectorRelation, BrioValueVectorRelation, BrioValueMapRelation)
      if (nodes.isEmpty) return FeltNoCode
      nodes.map {
        node =>
          s"""
             |$I${latticeRelationIsNullViaSchemaRuntime(node.pathName)} = true; // reset""".stripMargin
      }.mkString
    }

    val resets = nodeResets
    if (resets.isEmpty) return FeltNoCode
    resets
  }

}
