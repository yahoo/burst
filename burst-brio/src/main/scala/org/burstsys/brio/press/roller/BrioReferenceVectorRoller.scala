/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.press.roller

import org.burstsys.brio.model.schema.encoding.BrioSchematic
import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.press.{BrioPressInstance, BrioPresserContext}
import org.burstsys.brio.types.BrioNulls
import org.burstsys.tesla.TeslaTypes._

import scala.language.postfixOps

/**
  * Roll reference vector relation
  *
  */
trait BrioReferenceVectorRoller extends Any {

  self: BrioPresserContext =>

  protected final
  def rollReferenceVectorStructureRelation(node: BrioNode, instance: BrioPressInstance,
                                           schematic: BrioSchematic, structureStartOffset: TeslaMemoryOffset): Unit = {
    val ordinal = node.relation.relationOrdinal

    // first write the start of this relation into the offset table
    updateOffsetTable(schematic, ordinal, rollOffset - structureStartOffset, sink.buffer, structureStartOffset)

    val iterator = source.extractReferenceVector(cursor, instance)
    if (iterator == null) {
      BrioNulls.relationSetNull(sink.buffer, sink.buffer, ordinal, nullMapOffset(structureStartOffset))
    } else {
      BrioNulls.relationClearNull(sink.buffer, sink.buffer, ordinal, nullMapOffset(structureStartOffset))

      // we are going to store the number of vector members once we have it
      val vectorEntryCountOffset = rollOffset

      // leave room for this count to be back-written
      rollOffset += SizeOfShort

      var vectorSize = 0
      while (iterator.hasNext) {

        // get the next vector member
        val memberInstance = iterator.next()

        // we need to record the byte size of each vector member once we have it
        val byteSizeOffset = rollOffset

        // leave room for this vector member size
        rollOffset += SizeOfInteger

        // start counting byte size of member here
        val byteSizeTallyStart = rollOffset

        // roll vector member
        rollStructure(memberInstance, node)

        // now write the vector member size (once rolled) back *before* the member itself
        sink.buffer.writeInt(rollOffset - byteSizeTallyStart, byteSizeOffset)

        vectorSize += 1
      }

      // back-write count of vector members
      sink.buffer.writeShort(vectorSize.toShort, vectorEntryCountOffset)
    }

  }

}
