/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.model.schema.types

import org.burstsys.brio.model.schema.BrioSchemaContext
import org.burstsys.brio.types.BrioTypes._

/**
 * A set of relations in a structure in the schema
 */
trait BrioStructure extends Any {

  /**
   * Active schema for this structure
   *
   * @return
   */
  def schema: BrioSchemaContext

  /**
   * structure type name
   *
   * @return
   */
  def structureTypeName: BrioTypeName

  /**
   * structure type key
   *
   * @return
   */
  def structureTypeKey: BrioTypeKey

  /**
   * super structure type name
   *
   * @return
   */
  def superStructure: BrioStructure

  /**
   * a set of structures for which this is a super type
   *
   * @return
   */
  def subStructures: Array[BrioStructure]

  /**
   * the set of schema versions this structure is valid in
   *
   * @return
   */
  def validVersionSet: Set[BrioVersionKey]

  /**
   * all relations for this specific type  mapped by its relation ordinal
   *
   * @return
   */
  def specificStructureRelationOrdinalMap: BrioRelationOrdinalMap

  /**
   * all relations for this specific type mapped by its relation name
   *
   * @return
   */
  def specificStructureRelationNameMap: BrioRelationNameMap

  /**
   * all relations for this specific type and all its supertypes mapped by its relation ordinal
   *
   * @return
   */
  def transitiveStructureRelationOrdinalMap: BrioRelationOrdinalMap

  /**
   * all relations for this specific type and all its supertypes
   *
   * @return
   */
  def transitiveStructureRelationNameMap: BrioRelationNameMap

  /**
   * all relations for this specific type and all its supertypes and subtypes mapped by its relation ordinal
   *
   * @return
   */
  def normalizedStructureRelationOrdinalMap: BrioRelationOrdinalMap

  /**
   * all relations for this specific type and all its supertypes and subtypes mapped by its relation name
   *
   * @return
   */
  def normalizedStructureRelationNameMap: BrioRelationNameMap

}

/**
 * constructors
 */
object BrioStructure {
  def apply(schema: BrioSchemaContext, typeName: BrioTypeName, typeKey: BrioTypeKey): BrioStructure =
    BrioStructureContext(schema, typeName, typeKey)
}

/**
 * state
 *
 * @param schema
 * @param structureTypeName
 * @param structureTypeKey
 */
final case
class BrioStructureContext(schema: BrioSchemaContext, structureTypeName: BrioTypeName, structureTypeKey: BrioTypeKey)
  extends BrioStructure {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  var validVersionSet: Set[BrioVersionKey] = _

  var superStructure: BrioStructure = _

  var subStructures: Array[BrioStructure] = _

  var specificStructureRelationNameMap: BrioRelationNameMap = _

  var specificStructureRelationOrdinalMap: BrioRelationOrdinalMap = _

  var transitiveStructureRelationOrdinalMap: BrioRelationOrdinalMap = _

  var transitiveStructureRelationNameMap: BrioRelationNameMap = _

  var normalizedStructureRelationOrdinalMap: BrioRelationOrdinalMap = _

  var normalizedStructureRelationNameMap: BrioRelationNameMap = _

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def root: BrioStructureContext = {
    var root = this
    while (root.superStructure != null) {
      root = root.superStructure.asInstanceOf[BrioStructureContext]
    }
    root
  }

  def collectSuperRelations: BrioRelationNameMap = {
    if (superStructure == null)
      Map.empty
    else
      superStructure.asInstanceOf[BrioStructureContext].collectSuperRelations ++ superStructure.specificStructureRelationNameMap
  }

  def collectSubRelations: BrioRelationNameMap = {
    val relations = subStructures flatMap {
      case s: BrioStructureContext => s.collectSubRelations ++ s.specificStructureRelationNameMap
    }
    relations.map(r => r._1 -> r._2).toMap
  }


  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Misc
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  override def toString: String =
    s"Structure(structureTypeName='$structureTypeName', structureTypeKey=$structureTypeKey)"
}
