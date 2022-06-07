/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.lattice.traveler

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.types.BrioTypes.BrioVersionKey
import org.burstsys.felt.model.lattice.FeltLatTravGenContext
import org.burstsys.felt.model.schema.traveler.{FeltSearchMethod, FeltTraveler}
import org.burstsys.felt.model.sweep.splice._
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, I, I2, I3}

/**
 * generate code to implement a [[FeltTraveler]]
 * for a specific brio schema that traverses a specific reference vector node/relation
 */
trait FeltLatRefVecTravGen extends Any {

  self: FeltLatTravGenContext =>

  final
  def processRefVecRel(parentNode: BrioNode, version: BrioVersionKey, childNode: BrioNode)(implicit cursor: FeltCodeCursor): FeltCode = {
    val parentSchema = schematic(parentNode.relation.referenceStructure.structureTypeName, version)
    val parentRelation = latticeRelationViaSchemaRuntime(parentNode.pathName)
    val childRelation = latticeRelationViaSchemaRuntime(childNode.pathName)
    val childRelationIsNull = latticeRelationIsNullViaSchemaRuntime(childNode.pathName)
    val childPathName = childNode.pathName
    val childPathKey = childNode.pathKey
    val childOrdinal = childNode.relation.relationOrdinal
    val header = s"reference-vector path='$childPathName:$childPathKey' version=$version ordinal=$childOrdinal"

    val methodName = s"iterate_refvec_v${version}_${childPathName.replace(".", "_")}"
    val method = FeltSearchMethod(generator.runtimeClassName, methodName, {
      implicit cursor =>
        s"""|
            |$I2$childRelationIsNull = false;
            |$I2${latticeVectorIsFirstViaSchemaRuntime(childPathName)} = false;
            |$I2${latticeVectorIsLastViaSchemaRuntime(childPathName)} = false;
            |${I2}val memberIterator = $parentRelation.referenceVectorIterator($blobReaderSym, $parentSchema, $childOrdinal);
            |${I2}var vectorIndex = memberIterator.start($blobReaderSym);
            |${I2}val memberCount = memberIterator.length($blobReaderSym);
            |$I2$schemaSweepSym.referenceVectorSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorAllocPlace.key}); // $childPathName $FeltVectorAllocPlace
            |$I2$schemaSweepSym.referenceVectorSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorBeforePlace.key}); // $childPathName $FeltVectorBeforePlace
            |${I2}var i = 0;
            |${I2}while (i < memberCount && !$schemaRuntimeSym.skipControlMemberPath($childPathKey) ) {
            |$I3${latticeVectorIsFirstViaSchemaRuntime(childPathName)} = (i == 0);
            |$I3${latticeVectorIsLastViaSchemaRuntime(childPathName)} = i == (memberCount - 1);
            |$I3$childRelation = memberIterator.member($blobReaderSym, vectorIndex);
            |$I3$schemaSweepSym.referenceVectorMemberSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorMemberAllocPlace.key}); // $childPathName $FeltVectorMemberAllocPlace
            |${processRefScal(childNode)(cursor indentRight 2)}
            |$I3$schemaSweepSym.referenceVectorMemberSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorMemberMergePlace.key}); // $childPathName $FeltVectorMemberMergePlace
            |$I3$schemaSweepSym.referenceVectorMemberSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorMemberFreePlace.key}); // $childPathName $FeltVectorMemberFreePlace
            |${I3}vectorIndex = memberIterator.advance($blobReaderSym, vectorIndex);
            |${I3}i += 1;
            |$I2}
            |$I2${latticeVectorIsFirstViaSchemaRuntime(childPathName)} = false; // this should be false for 'after' splices
            |$I2${latticeVectorIsLastViaSchemaRuntime(childPathName)} = false; // this should be false for 'after' splices
            |$I2$schemaSweepSym.referenceVectorSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorAfterPlace.key}); // $childPathName $FeltVectorAfterPlace
            |$I2$schemaSweepSym.referenceVectorSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorFreePlace.key}); // $childPathName $FeltVectorFreePlace""".stripMargin
    })

    generator += method

    s"""|
        |${C(s"START $header")}
        |${I}if ($schemaSweepSym.$skipVisitPathSym($childPathKey) || $parentRelation.relationIsNull($blobReaderSym, $parentSchema, $childOrdinal)) {
        |$I2$childRelationIsNull = true;
        |$I} else {
        |$I2${method.methodCall};
        |$I} // END $header""".stripMargin

  }


}
