/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.press.roller

import org.burstsys.brio.model.schema.encoding.BrioSchematic
import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.press.{BrioPressInstance, BrioPresserContext}
import org.burstsys.brio.types.BrioNulls
import org.burstsys.tesla.TeslaTypes._

import scala.language.postfixOps

/**
  * Roll reference scalar relation
  *
  */
trait BrioReferenceScalarRoller extends Any {

  self: BrioPresserContext =>

  protected final
  def rollReferenceScalarStructureRelation(node: BrioNode, instance: BrioPressInstance,
                                           schematic: BrioSchematic, structureStartOffset: TeslaMemoryOffset): Unit = {

    val ordinal = node.relation.relationOrdinal

    // first write the start of this relation into the offset table
    updateOffsetTable(schematic, ordinal, rollOffset - structureStartOffset, sink.buffer, structureStartOffset)

    val referencedInstance = source.extractReferenceScalar(cursor, instance)
    val nullsMapOffset = nullMapOffset(structureStartOffset)
    if (referencedInstance == null) {
      BrioNulls.relationSetNull(sink.buffer, sink.buffer, ordinal, nullsMapOffset)
    } else {
      BrioNulls.relationClearNull(sink.buffer, sink.buffer, ordinal, nullsMapOffset)
      rollStructure(referencedInstance, node)
    }

  }

}
