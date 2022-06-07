/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.press.roller

import org.burstsys.brio.model.schema.encoding.{BrioSchematic, BrioSchematicContext}
import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types.{BrioReferenceScalarRelation, BrioReferenceVectorRelation, BrioValueMapRelation, BrioValueVectorRelation}
import org.burstsys.brio.press.{BrioPressInstance, BrioPresserContext}
import org.burstsys.brio.types.BrioNulls
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer

import scala.language.postfixOps

/**
  * Roll structure
  *
  */
trait BrioStructureRoller extends Any {

  self: BrioPresserContext =>

  /**
    *
    * @param instance
    * @param node
    */
  protected final
  def rollStructure(instance: BrioPressInstance, node:BrioNode): Unit = {

    val structureVersion = instance.schemaVersion

    // get metadata about the reference parent container instance
    val schematic = schemaContext.schematic(node.relation.valueOrReferenceTypeKey, structureVersion)

    // this is the very beginning of structure - save it so roller can access header info
    val structureStartOffset = rollOffset

    // structures have a fixed size header for version, nulls, offsets etc
    initializeStructureHeader(structureVersion, schematic, structureStartOffset)

    // value scalars are all fixed size within structure
    rollValueScalarRelations(instance, schematic, structureStartOffset, node)

    // roll forward to start of variable size relations section
    rollOffset += schematic.variableRelationsDataStart

    // all others are variable size and require offset table
    rollVariableSizeStructureRelations(instance, schematic, structureStartOffset, node)

    //    log debug printStructure(schematic, structureStartOffset)

  }

  protected final
  def nullMapOffset(structureOffset: TeslaMemoryOffset): TeslaMemoryOffset = structureOffset + SizeOfVersion

  /**
    *
    * @param instance
    * @param schematic
    * @param structureStartOffset
    */
  protected final
  def rollVariableSizeStructureRelations(instance: BrioPressInstance, schematic: BrioSchematic,
                                         structureStartOffset: TeslaMemoryOffset, node:BrioNode): Unit = {
    val size = schematic.variableSizeRelationArray.length

    var i = 0
    while (i < size) {
      val relation = schematic.variableSizeRelationArray(i)
      // TODO this is churning [StringBuilder] objects
      val childNode = node.brioSchema.nodeForPathName(node.pathName + "." + relation.relationName)
      //log debug burstStdMsg(s"${relation.relationPathName}:${relation.relationForm}")

      capture.reset
      cursor initialize childNode

      if (relation.validVersionSet.contains(instance.schemaVersion))
        relation.relationForm match {

          case BrioValueVectorRelation =>
            rollValueVectorStructureRelation(relation, instance, schematic, structureStartOffset)

          case BrioValueMapRelation =>
            rollValueMapStructureRelation(relation, instance, schematic, structureStartOffset)

          case BrioReferenceScalarRelation =>
            rollReferenceScalarStructureRelation(childNode, instance, schematic, structureStartOffset)

          case BrioReferenceVectorRelation =>
            rollReferenceVectorStructureRelation(childNode, instance, schematic, structureStartOffset)

        }
      i += 1
    }

  }

  /**
    * write offset to structure's variable size relation offset table
    * Offsets are stored as offsets from start of structure...
    *
    * @param schematic
    * @param ordinal
    * @param relationOffset
    * @param buffer
    * @param structureStartOffset
    */
  protected final
  def updateOffsetTable(schematic: BrioSchematic, ordinal: BrioRelationOrdinal,
                        relationOffset: TeslaMemoryOffset, buffer: TeslaMutableBuffer,
                        structureStartOffset: TeslaMemoryOffset): Unit = {
    val variableRelationKey = schematic.variableRelationOffsetKeys(ordinal)
    val offsetIntoOffsets = variableRelationKey * SizeOfOffset
    val position = structureStartOffset + schematic.variableRelationOffsetsStart + offsetIntoOffsets
    buffer.writeOffset(relationOffset, position)
  }

  /**
    * initialize fixed size/location header info for structure
    *
    * @param structureVersion
    * @param schematic
    */
  protected final
  def initializeStructureHeader(structureVersion: BrioVersionKey, schematic: BrioSchematic,
                                structureStartOffset: TeslaMemoryOffset): Unit = {
    var headerOffset = structureStartOffset

    // record version
    sink.buffer.writeInt(structureVersion, headerOffset)
    headerOffset += SizeOfInteger

    // set all relations to null
    BrioNulls.setAllNull(schematic.relationCount, sink.buffer, headerOffset)

    // set offset table to defaults.
    var offsetTableOffset = structureStartOffset + schematic.variableRelationOffsetsStart
    var i = 0
    while (i < schematic.variableRelationCount) {
      // initialize all offsets to zero
      sink.buffer.writeOffset(0, offsetTableOffset)
      offsetTableOffset += SizeOfOffset
      i += 1
    }
  }

}
