/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.model.schema.encoding

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.model.schema.types._
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.brio.types.{BrioException, BrioInvalidRelationError, BrioNulls}
import org.burstsys.tesla.TeslaTypes._
import com.koloboke.collect.map.hash.{HashObjObjMap, HashObjObjMaps}

import scala.collection.JavaConverters._

/**
 * high performance lookups used in runtime scans
 * the cost of laziness (https://dzone.com/articles/cost-laziness) spoiler - its not much but it's not nothing...
 */
trait BrioSchematicLookups extends Any {

  /**
   * this is version of this structure
   *
   * @return
   */
  def versionKey: BrioVersionKey

  /**
   * return the relation information for a relation ordinal
   *
   * @param relationOrdinal
   * @return
   */
  def form(relationOrdinal: BrioRelationOrdinal): BrioRelationForm

  /**
   * return the relation ordinal for a relation name
   *
   * @param relationName
   * @return
   */
  def relationOrdinal(relationName: BrioRelationName): BrioRelationOrdinal

}

/**
 * Meta-data for Brio Types that describes the binary encoded structure of each type within the blob
 * '''Some''' of these routes are called in inner loops in GIST code. Be very careful of throwing objects...
 */
trait BrioSchematic extends Any with BrioSchematicLookups {

  /**
   *
   * @return
   */
  def schema: BrioSchema

  /**
   * this is type key for this structure
   *
   * @return
   */
  def structureKey: BrioTypeKey

  /**
   * this is the total number of  relations
   *
   * @return
   */
  def relationCount: BrioRelationCount

  /**
   * this is the subtotal of fixed size relations
   *
   * @return
   */
  def fixedRelationCount: BrioRelationCount

  /**
   * this is the subtotal of variable size relations
   *
   * @return
   */
  def variableRelationCount: BrioRelationCount

  /**
   * this is where you can look up the type key for the value part of a relation
   * remember that relation keys are generally one based and this a zero based collection
   */
  def valueTypeKey: Array[BrioTypeKey]

  /**
   * Details on the appropriate encoding for the value
   */
  def valueEncoding: Array[BrioValueEncoding]

  /**
   * this is where you can look up the type key for the key part of a map relation
   * remember that relation keys are generally one based and this a zero based collection
   */
  def mapTypeKey: Array[BrioTypeKey]

  /**
   * Details on the appropriate encoding for the key
   */
  def keyEncoding: Array[BrioValueEncoding]

  /**
   * this is the offset from the container location where you can find the nulls bitmap block
   *
   * @return
   */
  def nullsMapStart: TeslaMemoryOffset

  /**
   * this is the size of the nulls bitmap block
   *
   * @return
   */
  def nullsMapSize: TeslaMemoryOffset

  /**
   * this is the offset from the container location where you can find the fixed relation data block
   *
   * @return
   */
  def fixedRelationsStart: TeslaMemoryOffset

  /**
   * this is the size of the fixed relations block
   *
   * @return
   */
  def fixedRelationsSize: TeslaMemoryOffset

  /**
   * this is the lookup for for finding the offset from the container location where you can find a fixed relation
   *
   * @return
   */
  def fixedRelationsOffsets: Array[TeslaMemoryOffset]

  /**
   * this is the offset from the container location where you can find the variable relation offset section
   *
   * @return
   */
  def variableRelationOffsetsStart: TeslaMemoryOffset

  /**
   * this is the size of the variable relations offset lookup array
   *
   * @return
   */
  def variableRelationsOffsetsSize: TeslaMemoryOffset

  /**
   * this is the offset from the container location where you can find the variable relation data section
   *
   * @return
   */
  def variableRelationsDataStart: TeslaMemoryOffset

  /**
   * convert from relation key to index into the variable relation's offset array
   *
   * @return
   */
  def variableRelationOffsetKeys: Array[BrioRelationOrdinal]

  /**
   * all the variable size relations
   *
   * @return
   */
  def variableSizeRelationArray: Array[BrioRelation]

  /**
   * all the fixed size relations
   *
   * @return
   */
  def fixedSizeRelationArray: Array[BrioRelation]

  /**
   * all the relations (fixed + variable)
   *
   * @return
   */
  def relationArray: Array[BrioRelation]

}

object BrioSchematic {
  def apply(schema: BrioSchema, structure: BrioStructure, version: BrioVersionKey): BrioSchematic =
    BrioSchematicContext(schema, structure, version)
}

private final case
class BrioSchematicContext(schema: BrioSchema, structure: BrioStructure, versionKey: BrioVersionKey) extends BrioSchematic {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Internal State
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _versionStart = 0

  private[this]
  val _versionSize = SizeOfInteger

  private[this]
  val _relationNameMap: HashObjObjMap[BrioRelationName, BrioRelation] = {
    val contents = structure.normalizedStructureRelationNameMap.filter(_._2.validVersionSet.contains(versionKey))
    HashObjObjMaps.newImmutableMap(contents.asJava)
  }

  private[this]
  val _relationOrdinalMap: HashObjObjMap[BrioRelationOrdinal, BrioRelation] = {
    val contents = structure.normalizedStructureRelationOrdinalMap.filter(_._2.validVersionSet.contains(versionKey))
    HashObjObjMaps.newImmutableMap(contents.asJava)
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  val relationArray: Array[BrioRelation] = _relationOrdinalMap.asScala.toList.sortBy(_._2.relationOrdinal).map(_._2).toArray

  override
  val fixedSizeRelationArray: Array[BrioRelation] = relationArray.filter(_.relationForm == BrioValueScalarRelation)

  override
  val variableSizeRelationArray: Array[BrioRelation] = relationArray.filter(_.relationForm != BrioValueScalarRelation)

  override
  val structureKey: BrioTypeKey = structure.structureTypeKey

  override
  val relationCount: BrioRelationCount = _relationOrdinalMap.size

  override
  val fixedRelationCount: BrioRelationCount = _relationOrdinalMap.asScala.count(_._2.relationForm == BrioValueScalarRelation)

  override
  val variableRelationCount: BrioRelationCount = relationCount - fixedRelationCount

  override
  val nullsMapStart: TeslaMemoryOffset = _versionStart + _versionSize

  override
  val nullsMapSize: TeslaMemoryOffset = BrioNulls.mapLongCount(relationCount) * SizeOfLong

  override
  val fixedRelationsStart: TeslaMemoryOffset = nullsMapStart + nullsMapSize

  override
  val fixedRelationsSize: TeslaMemoryOffset =
    _relationOrdinalMap.asScala.filter(_._2.relationForm == BrioValueScalarRelation).foldLeft(0)(_ + _._2.valueEncoding.bytes)

  override
  val variableRelationOffsetsStart: TeslaMemoryOffset = fixedRelationsStart + fixedRelationsSize

  override
  val variableRelationsOffsetsSize: TeslaMemoryOffset = variableRelationCount * SizeOfOffset

  override
  val variableRelationsDataStart: TeslaMemoryOffset = variableRelationOffsetsStart + variableRelationsOffsetsSize

  override
  val fixedRelationsOffsets: Array[TeslaMemoryOffset] = {
    var offset = fixedRelationsStart
    _relationOrdinalMap.asScala.toList.sortBy(_._1).map {
      case (_, relation) =>
        if (relation.relationForm == BrioValueScalarRelation) {
          val newoffset = offset
          offset += relation.valueEncoding.bytes
          newoffset
        } else BrioAnyTypeKey
    }.toArray
  }

  override
  val variableRelationOffsetKeys: Array[BrioRelationOrdinal] = {
    var index = 0
    _relationOrdinalMap.asScala.toList.sortBy(_._1).map {
      case (_, relation) =>
        if (relation.relationForm != BrioValueScalarRelation) {
          val result = index
          index += 1
          result
        } else BrioAnyTypeKey
    }.toArray
  }

  override
  val valueTypeKey: Array[BrioTypeKey] = structure.normalizedStructureRelationNameMap.values.toList.sortBy(_.relationOrdinal).map {
    relation =>
      relation.relationForm match {
        case BrioValueScalarRelation | BrioValueVectorRelation | BrioValueMapRelation =>
          relation.valueEncoding.typeKey
        case BrioReferenceScalarRelation | BrioReferenceVectorRelation =>
          relation.referenceStructure.structureTypeKey
      }
  }.toArray

  override
  val mapTypeKey: Array[BrioTypeKey] = structure.normalizedStructureRelationNameMap.values.toList.sortBy(_.relationOrdinal).map {
    relation =>
      relation.relationForm match {
        case BrioValueMapRelation => relation.keyEncoding.typeKey
        case _ => BrioRelationOrdinalNotFound
      }
  }.toArray

  override
  val valueEncoding: Array[BrioValueEncoding] = structure.normalizedStructureRelationNameMap.values.toList.sortBy(_.relationOrdinal).map(_.valueEncoding).toArray

  override
  val keyEncoding: Array[BrioValueEncoding] = structure.normalizedStructureRelationNameMap.values.toList.sortBy(_.relationOrdinal).map(_.keyEncoding).toArray

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API (RUNTIME LOOKUPS)
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def form(relation: BrioRelationOrdinal): BrioRelationForm = {
    relationArray(relation).relationForm
  }

  override
  def relationOrdinal(relationName: BrioRelationName): BrioRelationOrdinal = {
    val relation = _relationNameMap.get(relationName)
    if (relation == null)
      throw BrioException(BrioInvalidRelationError, s"relation '$relationName' not found in schematic")
    relation.relationOrdinal
  }

}
