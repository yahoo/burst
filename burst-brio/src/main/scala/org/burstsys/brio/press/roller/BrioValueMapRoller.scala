/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.press.roller

import org.burstsys.brio.model.schema.encoding.BrioSchematic
import org.burstsys.brio.model.schema.types.BrioRelation
import org.burstsys.brio.press.{BrioPressInstance, BrioPresserContext, BrioValueMapPressCapture}
import org.burstsys.brio.types.BrioNulls
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.tesla.TeslaTypes._

import scala.language.postfixOps

/**
  * Roll value map relation
  *
  */
trait BrioValueMapRoller extends Any {

  self: BrioPresserContext =>

  protected final
  def rollValueMapStructureRelation(relation: BrioRelation, instance: BrioPressInstance, schematic: BrioSchematic,
                                    structureStartOffset: TeslaMemoryOffset): Unit = {

    val ordinal = relation.relationOrdinal
    val nullsMapOffset = nullMapOffset(structureStartOffset)

    // first write the start of this relation into the offset table
    updateOffsetTable(schematic, ordinal, rollOffset - structureStartOffset, sink.buffer, structureStartOffset)

    /**
      * setup capture for map data
      */
    capture.relationValueDataType = relation.valueEncoding.typeKey
    capture.relationKeyDataType = relation.keyEncoding.typeKey

    /**
      * ask source for map data
      */
    source.extractValueMap(cursor, instance, capture.asInstanceOf[BrioValueMapPressCapture])
    capture.validateMapCapture

    /**
      * look at what we got
      */
    if (capture.isNull) {
      BrioNulls.relationSetNull(sink.buffer, sink.buffer, ordinal, nullsMapOffset)
    } else {
      BrioNulls.relationClearNull(sink.buffer, sink.buffer, ordinal, nullsMapOffset)
      rollValueMapData(schematic, relation, structureStartOffset)
    }

  }

  private
  def rollValueMapData(schematic: BrioSchematic, relation: BrioRelation, structureStartOffset: TeslaMemoryOffset): Unit = {
    val ordinal = relation.relationOrdinal

    val entries = capture.valueMapEntries

    // copy over entries as a short
    sink.buffer.writeShort(entries.toShort, rollOffset)
    rollOffset += SizeOfShort

    // Avoid using tuple matching as it creates objects before doing a match
    val keyType = schematic.mapTypeKey(ordinal)
    val valueType = schematic.valueTypeKey(ordinal)
    lazy val errorMsg = s"cannot handle a Map[${brioDataTypeNameFromKey(keyType)}, ${brioDataTypeNameFromKey(valueType)}]"
    // TODO EXTENDED TYPES
    // TODO VALUE MAPS
    keyType match {
      case BrioLongKey =>
        valueType match {
          case BrioStringKey =>
            var i = 0
            while (i < entries.toInt) {
              sink.buffer.writeLong(capture.longKeyVector(i), rollOffset)
              rollOffset += SizeOfLong
              i += 1
            }
            i = 0
            while (i < entries.toInt) {
              sink.buffer.writeShort(capture.stringValueVector(i), rollOffset)
              rollOffset += SizeOfString
              i += 1
            }
          case _ => throw VitalsException(errorMsg)
        }
      case BrioStringKey =>
        valueType match {
          case BrioStringKey =>
            var i = 0
            while (i < entries.toInt) {
              sink.buffer.writeShort(capture.stringKeyVector(i), rollOffset)
              rollOffset += SizeOfString
              i += 1
            }
            i = 0
            while (i < entries.toInt) {
              sink.buffer.writeShort(capture.stringValueVector(i), rollOffset)
              rollOffset += SizeOfString
              i += 1
            }
          case _ => throw VitalsException(errorMsg)
        }
      case _ => throw VitalsException(errorMsg)
    }
  }


}
