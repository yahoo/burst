/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.press.roller

import org.burstsys.brio.model.schema.encoding.BrioSchematic
import org.burstsys.brio.model.schema.types.BrioRelation
import org.burstsys.brio.press.{BrioPressInstance, BrioPresserContext, BrioValueVectorPressCapture}
import org.burstsys.brio.types.BrioNulls
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.tesla.TeslaTypes._

import scala.language.postfixOps

/**
  * Roll value vector relation
  *
  */
trait BrioValueVectorRoller extends Any {

  self: BrioPresserContext =>

  protected final
  def rollValueVectorStructureRelation(relation: BrioRelation, instance: BrioPressInstance, schematic: BrioSchematic,
                                       structureStartOffset: TeslaMemoryOffset): Unit = {
    val ordinal = relation.relationOrdinal

    // first write the start of this relation into the offset table
    updateOffsetTable(schematic, ordinal, rollOffset - structureStartOffset, sink.buffer, structureStartOffset)

    // capture the data
    capture.relationValueDataType = relation.valueEncoding.typeKey
    source.extractValueVector(cursor, instance, capture.asInstanceOf[BrioValueVectorPressCapture])
    capture.validateVectorCapture

    // deal with it
    if (capture.isNull) {
      BrioNulls.relationSetNull(sink.buffer, sink.buffer, ordinal, nullMapOffset(structureStartOffset))
    } else {
      BrioNulls.relationClearNull(sink.buffer, sink.buffer, ordinal, nullMapOffset(structureStartOffset))
      rollValueVectorData(schematic, ordinal, capture.valueVectorEntries.toShort)
    }
  }

  private
  def rollValueVectorData(schematic: BrioSchematic, ordinal: BrioRelationOrdinal, vectorSize: Short): Unit = {
    /**
      * start with recording size of vector as a short
      */
    sink.buffer.writeShort(vectorSize, rollOffset)
    rollOffset += SizeOfShort

    val typeKey = schematic.valueTypeKey(ordinal)
    typeKey match {
      case BrioBooleanKey =>
        var k = 0
        while (k < vectorSize) {
          sink.buffer.writeBoolean(capture.booleanValueVector(k), rollOffset)
          rollOffset += SizeOfBoolean
          k += 1
        }
      case BrioByteKey =>
        var k = 0
        while (k < vectorSize) {
          sink.buffer.writeByte(capture.byteValueVector(k), rollOffset)
          rollOffset += SizeOfByte
          k += 1
        }
      case BrioShortKey =>
        var k = 0
        while (k < vectorSize) {
          sink.buffer.writeShort(capture.shortValueVector(k), rollOffset)
          rollOffset += SizeOfShort
          k += 1
        }
      case BrioIntegerKey =>
        var k = 0
        while (k < vectorSize) {
          sink.buffer.writeInt(capture.integerValueVector(k), rollOffset)
          rollOffset += SizeOfInteger
          k += 1
        }
      case BrioLongKey =>
        var k = 0
        while (k < vectorSize) {
          sink.buffer.writeLong(capture.longValueVector(k), rollOffset)
          rollOffset += SizeOfLong
          k += 1
        }
      case BrioDoubleKey =>
        var k = 0
        while (k < vectorSize) {
          sink.buffer.writeDouble(capture.doubleValueVector(k), rollOffset)
          rollOffset += SizeOfDouble
          k += 1
        }
      case BrioStringKey =>
        // normalize strings as you copy
        var k = 0
        while (k < vectorSize) {
          sink.buffer.writeShort(capture.stringValueVector(k), rollOffset)
          rollOffset += SizeOfString
          k += 1
        }

      // TODO EXTENDED TYPES
      case BrioElasticKey =>
        var k = 0
        while (k < vectorSize) {
          sink.buffer.writeLong(capture.longValueVector(k), rollOffset)
          rollOffset += SizeOfLong
          k += 1
        }

      // TODO EXTENDED TYPES
      case BrioLookupKey =>
        var k = 0
        while (k < vectorSize) {
          sink.buffer.writeLong(capture.longValueVector(k), rollOffset)
          rollOffset += SizeOfLong
          k += 1
        }

      case _ => throw VitalsException(s"vector roll can't handle typeKey=$typeKey")
    }
  }
}
