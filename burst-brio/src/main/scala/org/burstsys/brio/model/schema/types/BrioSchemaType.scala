/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.model.schema.types

import java.util.concurrent.atomic.AtomicInteger

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.model.schema.encoding.BrioSchematic
import org.burstsys.brio.types.BrioPath.{BrioPathKey, BrioPathName}
import org.burstsys.brio.types.BrioTypes.{BrioRelationName, BrioTypeKey, BrioTypeName, BrioVersionKey}
import org.burstsys.vitals.errors.VitalsException

import scala.collection.mutable

/**
 * Schema to type (BrioRelation, BrioStructure)
 *
 * @see BrioRelation
 * @see BrioStructure
 */
trait BrioSchemaTypes extends Any {

  /**
   * The root relation name for the schemas root object in a complex object tree - note this is
   * chosen when writing the schema
   *
   * @return
   */
  def rootRelationName: BrioRelationName

  /**
   * lookup sorted list of relation by version and structure keys
   *
   * @param version
   * @param structureType
   * @return
   */
  @inline
  def relationList(version: BrioVersionKey, structureType: BrioTypeKey): Array[BrioRelation]

  /**
   * The root structure type name for the schema's root object
   *
   * @return
   */
  def rootStructureType: BrioTypeName

  /**
   *
   * @param structure
   * @return
   */
  def structureKey(structure: BrioTypeName): BrioTypeKey

  /**
   *
   * @param structure
   * @return
   */
  def structureName(structure: BrioTypeKey): BrioTypeName

  /**
   *
   * @return
   */
  def structures: Array[BrioStructure]

  /**
   * fast runtime lookup for schematics
   *
   * @param path
   * @param version
   * @return
   */
  def schematic(path: BrioPathName, version: BrioVersionKey): BrioSchematic

  /**
   * fast runtime lookup for schematics
   *
   * @param structureType
   * @param version
   * @return
   */
  def schematic(structureType: BrioTypeKey, version: BrioVersionKey): BrioSchematic

}

trait BrioSchemaTypeContext extends AnyRef with BrioSchemaTypes {

  self: BrioSchema =>

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[brio]
  var _rootRelation: BrioRelation = _

  private[model]
  val elasticOffsetIndex: AtomicInteger = new AtomicInteger

  private[model]
  val elasticOffsetMap: mutable.HashMap[String, Int] = new mutable.HashMap[String, Int]

  private[model]
  val lookupTypeTableIndex: AtomicInteger = new AtomicInteger

  private[model]
  val lookupTypeTableMap: mutable.HashMap[String, Int] = new mutable.HashMap[String, Int]

  private[model]
  var typeNameToStructureMap: Map[BrioTypeName, BrioStructure] = _

  private[model]
  var typeNameToKeyMap: Map[BrioTypeName, BrioTypeKey] = _

  private[model]
  var typeKeyToNameMap: Map[BrioTypeKey, BrioTypeName] = _

  private[model]
  var schematicStructureVersionTypeKeyLookup: Array[Array[BrioSchematic]] = _

  private[model]
  var relationStructureVersionTypeKeyLookup: Array[Array[Array[BrioRelation]]] = _

  private[model]
  var _firstStructureType: BrioTypeKey = _

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def rootRelation: BrioRelation = _rootRelation

  final override
  def structureKey(structure: BrioTypeName): BrioTypeKey = {
    typeNameToStructureMap(structure).structureTypeKey
  }

  final override
  def structureName(structure: BrioTypeKey): BrioTypeName = {
    typeKeyToNameMap(structure)
  }

  final override
  def structures: Array[BrioStructure] = typeNameToStructureMap.values.toArray

  final override
  def relationList(version: BrioVersionKey, structureType: BrioTypeKey): Array[BrioRelation] = {
    validateVersion(version)
    if (structureType < _firstStructureType) Array.empty
    else relationStructureVersionTypeKeyLookup(version)(structureType - _firstStructureType)
  }

  final override
  def schematic(path: BrioPathName, version: BrioVersionKey): BrioSchematic = {
    validateVersion(version)
    keyForPath(path) match {
      case -1 =>
        null
      case pathKey =>
        val structure = nodeForPathKey(pathKey).relation.referenceStructure
        if (structure == null)
          throw VitalsException(s"schematic for path=$path, version=$version not a reference in schema '$name'")
        schematicStructureVersionTypeKeyLookup(version)(structure.structureTypeKey - _firstStructureType)
    }
  }

  final override
  def schematic(structureType: BrioTypeKey, version: BrioVersionKey): BrioSchematic = {
    validateVersion(version)
    val schematics = schematicStructureVersionTypeKeyLookup(version)
    val structureOffset = structureType - _firstStructureType
    if (structureOffset < 0 || structureOffset >= schematics.length)
      throw VitalsException(s"structureType=$structureType, version=$version not valid")
    val s = schematics(structureOffset)
    if (s == null)
      throw VitalsException(s"schematic for structureType=$structureType, version=$version not found in schema '$name'")
    s
  }

  private
  def validateVersion(version: BrioVersionKey): Unit = {
    if (version < 1 || version > versionCount)
      throw VitalsException(s"version=$version not valid in schema '$name'")
  }

}
