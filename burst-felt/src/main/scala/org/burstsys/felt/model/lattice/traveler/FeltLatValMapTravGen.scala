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
 * for a specific brio schema that traverses a specific value map node/relation
 */
trait FeltLatValMapTravGen extends Any {

  self: FeltLatTravGenContext =>

  final
  def processValMapRel(parentNode: BrioNode, version: BrioVersionKey, childNode: BrioNode)(implicit cursor: FeltCodeCursor): FeltCode = {
    val parentSchema = schematic(parentNode.relation.referenceStructure.structureTypeName, version)
    val parentRelation = latticeRelationViaSchemaRuntime(parentNode.pathName)
    val childRelationIsNull = latticeRelationIsNullViaSchemaRuntime(childNode.pathName)
    val childPathName = childNode.pathName
    val childPathKey = childNode.pathKey
    val childOrdinal = childNode.relation.relationOrdinal
    val header = s"value-map path='$childPathName:$childPathKey' version=$version ordinal=$childOrdinal"

    val methodName = s"iterate_valmap_v${version}_${childPathName.replace(".", "_")}"
    val method = FeltSearchMethod(generator.runtimeClassName, methodName, {
      implicit cursor =>
        s"""|
            |$I2$childRelationIsNull = false;
            |${I2}${latticeVectorIsFirstViaSchemaRuntime(childPathName)} = false;
            |${I2}${latticeVectorIsLastViaSchemaRuntime(childPathName)} = false;
            |${I2}val memberIterator = $parentRelation.valueMapIterator($blobReaderSym, $parentSchema, $childOrdinal);
            |${I2}val memberCount = memberIterator.length($blobReaderSym);
            |$I2$schemaSweepSym.valueMapSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorAllocPlace.key}); // $childPathName $FeltVectorAllocPlace
            |$I2$schemaSweepSym.valueMapSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorBeforePlace.key}); // $childPathName $FeltVectorBeforePlace
            |${I2}var i = 0;
            |${I2}while (i < memberCount && !$schemaRuntimeSym.skipControlMemberPath($childPathKey) ) {
            |$I3${latticeVectorIsFirstViaSchemaRuntime(childPathName)} = (i == 0);
            |$I3${latticeVectorIsLastViaSchemaRuntime(childPathName)} = i == (memberCount - 1);
            |$I3$schemaSweepSym.valueMapMemberSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorMemberAllocPlace.key}); // $childPathName $FeltVectorMemberAllocPlace
            |${valueMapMemberData(childNode)(cursor indentRight 2)}
            |$I3$schemaSweepSym.valueMapMemberSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorMemberSituPlace.key}); // $childPathName $FeltVectorMemberSituPlace
            |$I3$schemaSweepSym.valueMapMemberSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorMemberMergePlace.key}); // $childPathName $FeltVectorMemberMergePlace
            |$I3$schemaSweepSym.valueMapMemberSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorMemberFreePlace.key}); // $childPathName $FeltVectorMemberFreePlace
            |${I3}i += 1;
            |$I2}
            |$I2${latticeVectorIsFirstViaSchemaRuntime(childPathName)} = false; // this should be false for 'after' splices
            |$I2${latticeVectorIsLastViaSchemaRuntime(childPathName)} = false; // this should be false for 'after' splices
            |$I2$schemaSweepSym.valueMapSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorAfterPlace.key}); // $childPathName $FeltVectorAfterPlace
            |$I2$schemaSweepSym.valueMapSplice($schemaRuntimeSym, $childPathKey, ${FeltVectorFreePlace.key}); // $childPathName $FeltVectorFreePlace """.stripMargin
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

  // TODO REWRITE FOR ALL POSSIBLE COMBINATIONS - NOW ONLY REALLY SUPPORT STRING STRING MAPS
  private
  def valueMapMemberData(treeNode: BrioNode)(implicit cursor: FeltCodeCursor): FeltCode = {
    val ke = treeNode.relation.keyEncoding
    val keyData = ke.typeKey match {
      case BrioTypes.BrioBooleanKey => s"memberIterator.readBooleanKey(i, $blobReaderSym)"
      case BrioTypes.BrioByteKey => s"memberIterator.readByteKey(i, $blobReaderSym)"
      case BrioTypes.BrioShortKey => s"memberIterator.readShortKey(i, $blobReaderSym)"
      case BrioTypes.BrioIntegerKey => s"memberIterator.readIntKey(i, $blobReaderSym)"
      case BrioTypes.BrioLongKey => s"memberIterator.readLongKey(i, $blobReaderSym)"
      case BrioTypes.BrioDoubleKey => s"memberIterator.readDoubleKey(i, $blobReaderSym)"
      case BrioTypes.BrioStringKey =>
        if (cursor.global.lexicon.enabled)
          s"memberIterator.readLexiconStringKey(i, $blobReaderSym)"
        else
          s"memberIterator.readStringKey(i, $blobReaderSym)($schemaRuntimeSym.text, $blobDictionarySym)"
      case _ => ???
    }
    val ve = treeNode.relation.valueEncoding
    val valueData = ve.typeKey match {
      case BrioTypes.BrioBooleanKey => s"memberIterator.readBooleanValue(i, $blobReaderSym)"
      case BrioTypes.BrioByteKey => s"memberIterator.readByteValue(i, $blobReaderSym)"
      case BrioTypes.BrioShortKey => s"memberIterator.readShortValue(i, $blobReaderSym)"
      case BrioTypes.BrioIntegerKey => s"memberIterator.readIntValue(i, $blobReaderSym)"
      case BrioTypes.BrioLongKey => s"memberIterator.readLongValue(i, $blobReaderSym)"
      case BrioTypes.BrioDoubleKey => s"memberIterator.readDoubleValue(i, $blobReaderSym)"
      case BrioTypes.BrioStringKey =>
        if (cursor.global.lexicon.enabled)
          s"memberIterator.readLexiconStringStringValue(i, $blobReaderSym)"
        else
          s"memberIterator.readStringStringValue(i, $blobReaderSym)($schemaRuntimeSym.text, $blobDictionarySym)"

      case _ => ???
    }
    s"""|
        |${C(s"current member key/value tuple for value map ${treeNode.pathName}")}
        |$I${latticeValueMapKeyViaSchemaRuntime(treeNode.pathName)} = $keyData;
        |$I${latticeValueMapValueViaSchemaRuntime(treeNode.pathName)} = $valueData; """.stripMargin
  }


}
