/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.lattice.traveler

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.types.BrioTypes
import org.burstsys.brio.types.BrioTypes.BrioVersionKey
import org.burstsys.felt.model.lattice.FeltLatTravGenContext
import org.burstsys.felt.model.schema.traveler.{FeltSearchMethod, FeltTraveler}
import org.burstsys.felt.model.sweep.splice._
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, I, I2, I3}

/**
 * generate code to implement a [[FeltTraveler]]
 * for a specific brio schema that traverses a specific value vector node/relation
 */
trait FeltLatValVecTravGen extends Any {

  self: FeltLatTravGenContext =>

  final
  def processValVecRel(parentNode: BrioNode, version: BrioVersionKey, childNode: BrioNode)(implicit cursor: FeltCodeCursor): FeltCode = {
    val parentSchema = schematic(parentNode.relation.referenceStructure.structureTypeName, version)
    val parentRelation = latticeRelationViaSchemaRuntime(parentNode.pathName)
    val childRelationIsNull = latticeRelationIsNullViaSchemaRuntime(childNode.pathName)
    val childPathName = childNode.pathName
    val childPathKey = childNode.pathKey
    val childOrdinal = childNode.relation.relationOrdinal
    val header = s"value-vector path='$childPathName:$childPathKey' version=$version ordinal=$childOrdinal"
    val contentKey = childNode.relation.valueEncoding.typeKey

    val methodName = s"iterate_valvec_v${version}_${childPathName.replace(".", "_")}"
    val method = FeltSearchMethod(generator.runtimeClassName, methodName, {
      implicit cursor =>
        s"""|
            |$I2$childRelationIsNull = false;
            |$I2${latticeVectorIsFirstViaSchemaRuntime(childPathName)} = false;
            |$I2${latticeVectorIsLastViaSchemaRuntime(childPathName)} = false;
            |${I2}val memberIterator = $parentRelation.valueVectorIterator($blobReaderSym, $parentSchema, $childOrdinal);
            |${I2}var memberOffset = memberIterator.start($blobReaderSym)
            |${I2}val memberCount = memberIterator.length($blobReaderSym);
            |$I2$schemaSweepSym.valueVectorSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorAllocPlace.key});    // $childPathName $FeltVectorAllocPlace
            |$I2$schemaSweepSym.valueVectorSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorBeforePlace.key});    // $childPathName $FeltVectorBeforePlace
            |${I2}var i = 0;
            |${I2}while (i < memberCount && !$schemaRuntimeSym.skipControlMemberPath($childPathKey) ) {
            |$I3${latticeVectorIsFirstViaSchemaRuntime(childPathName)} = (i == 0);
            |$I3${latticeVectorIsLastViaSchemaRuntime(childPathName)} = i == (memberCount - 1);
            |$I3$schemaSweepSym.valueVectorMemberSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorMemberAllocPlace.key})  // $childPathName $FeltVectorMemberAllocPlace
            |${valueVectorMemberData(childNode)(cursor indentRight 2)}
            |$I3$schemaSweepSym.valueVectorMemberSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorMemberSituPlace.key})  // $childPathName $FeltVectorMemberSituPlace
            |$I3$schemaSweepSym.valueVectorMemberSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorMemberMergePlace.key})  // $childPathName $FeltVectorMemberMergePlace
            |$I3$schemaSweepSym.valueVectorMemberSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorMemberFreePlace.key})  // $childPathName $FeltVectorMemberFreePlace
            |${I3}memberOffset = memberIterator.advance($blobReaderSym, memberOffset, $contentKey)
            |${I3}i += 1;
            |$I2}
            |$I2${latticeVectorIsFirstViaSchemaRuntime(childPathName)} = false; // this should be false for 'after' splices
            |$I2${latticeVectorIsLastViaSchemaRuntime(childPathName)} = false; // this should be false for 'after' splices
            |$I2$schemaSweepSym.valueVectorSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorAfterPlace.key})  // $childPathName $FeltVectorAfterPlace
            |$I2$schemaSweepSym.valueVectorSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorFreePlace.key})  // $childPathName $FeltVectorFreePlace""".stripMargin
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

  private
  def valueVectorMemberData(treeNode: BrioNode)(implicit cursor: FeltCodeCursor): FeltCode = {
    val ve = treeNode.relation.valueEncoding
    val valueData = ve.typeKey match {
      case BrioTypes.BrioBooleanKey => s"memberIterator.readBoolean($blobReaderSym, memberOffset)"
      case BrioTypes.BrioByteKey => s"memberIterator.readByte($blobReaderSym, memberOffset)"
      case BrioTypes.BrioShortKey => s"memberIterator.readShort($blobReaderSym, memberOffset)"
      case BrioTypes.BrioIntegerKey => s"memberIterator.readInt($blobReaderSym, memberOffset)"
      case BrioTypes.BrioLongKey => s"memberIterator.readLong($blobReaderSym, memberOffset)"
      case BrioTypes.BrioDoubleKey => s"memberIterator.readDouble($blobReaderSym, memberOffset)"
      case BrioTypes.BrioStringKey =>
        s"memberIterator.readString($blobReaderSym, memberOffset)($schemaRuntimeSym.text, $blobDictionarySym)"
      case _ => ???
    }
    s"""|
        |${C(s"data for value vector ${treeNode.pathName}")}
        |$I${latticeValueVectorValueViaSchemaRuntime(treeNode.pathName)} = $valueData
        |$I${latticeValueVectorValueIsNullViaSchemaRuntime(treeNode.pathName)} = false""".stripMargin
  }


}
