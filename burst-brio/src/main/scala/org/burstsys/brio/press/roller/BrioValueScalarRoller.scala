/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.press.roller

import org.burstsys.brio.model.schema.encoding._
import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.press.{BrioPressInstance, BrioPresserContext, BrioValueScalarPressCapture}
import org.burstsys.brio.types.BrioNulls
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.tesla.TeslaTypes._

import scala.language.postfixOps

/**
  * Roll value scalar relations
  *
  */
trait BrioValueScalarRoller extends Any {

  self: BrioPresserContext =>

  /**
    * now iterate through value scalar (fixed size) relations
    *
    * @param instance
    * @param schematic
    * @param structureStartOffset
    * @param node the location of the type of the instance object being pressed, in the Brio tree
    */
  protected final
  def rollValueScalarRelations(instance: BrioPressInstance, schematic: BrioSchematic, structureStartOffset: TeslaMemoryOffset, node: BrioNode): Unit = {

    val size = schematic.fixedSizeRelationArray.length
    var i = 0
    while (i < size) {
      val relation = schematic.fixedSizeRelationArray(i)
      // TODO this is churning [StringBuilder] objects
      val childNode = node.brioSchema.nodeForPathName(node.pathName + "." + relation.relationName)
      //log debug burstStdMsg(s"${relation.relationPathName}:${relation.relationForm}")
      capture.reset
      cursor initialize childNode
      val relationOrdinal = relation.relationOrdinal
      val relationOffset = structureStartOffset + schematic.fixedRelationsOffsets(relationOrdinal)
      val nullsMapOffset = nullMapOffset(structureStartOffset)
      if (relation.validVersionSet.contains(instance.schemaVersion))
        relation valueEncoding match {
          case BrioBooleanValueEncoding =>
            capture.relationValueDataType = BrioBooleanKey
            source.extractValueScalar(cursor, instance, capture.asInstanceOf[BrioValueScalarPressCapture])
            capture.validateScalarCapture
            if (capture.isNull) {
              BrioNulls.relationSetNull(sink.buffer, sink.buffer, relationOrdinal, nullsMapOffset)
            } else {
              BrioNulls.relationClearNull(sink.buffer, sink.buffer, relationOrdinal, nullsMapOffset)
              sink.buffer.writeBoolean(capture.booleanValue, relationOffset)
            }

          case BrioByteValueEncoding =>
            capture.relationValueDataType = BrioByteKey
            source.extractValueScalar(cursor, instance, capture.asInstanceOf[BrioValueScalarPressCapture])
            capture.validateScalarCapture
            if (capture.isNull) {
              BrioNulls.relationSetNull(sink.buffer, sink.buffer, relationOrdinal, nullsMapOffset)
            } else {
              BrioNulls.relationClearNull(sink.buffer, sink.buffer, relationOrdinal, nullsMapOffset)
              sink.buffer.writeByte(capture.byteValue, relationOffset)
            }

          case BrioShortValueEncoding =>
            capture.relationValueDataType = BrioShortKey
            source.extractValueScalar(cursor, instance, capture.asInstanceOf[BrioValueScalarPressCapture])
            capture.validateScalarCapture
            if (capture.isNull) {
              BrioNulls.relationSetNull(sink.buffer, sink.buffer, relationOrdinal, nullsMapOffset)
            } else {
              BrioNulls.relationClearNull(sink.buffer, sink.buffer, relationOrdinal, nullsMapOffset)
              sink.buffer.writeShort(capture.shortValue, relationOffset)
            }

          case BrioIntegerValueEncoding =>
            capture.relationValueDataType = BrioIntegerKey
            source.extractValueScalar(cursor, instance, capture.asInstanceOf[BrioValueScalarPressCapture])
            capture.validateScalarCapture
            if (capture.isNull) {
              BrioNulls.relationSetNull(sink.buffer, sink.buffer, relationOrdinal, nullsMapOffset)
            } else {
              BrioNulls.relationClearNull(sink.buffer, sink.buffer, relationOrdinal, nullsMapOffset)
              sink.buffer.writeInt(capture.integerValue, relationOffset)
            }

          case BrioLongValueEncoding =>
            capture.relationValueDataType = BrioLongKey
            source.extractValueScalar(cursor, instance, capture.asInstanceOf[BrioValueScalarPressCapture])
            capture.validateScalarCapture
            if (capture.isNull) {
              BrioNulls.relationSetNull(sink.buffer, sink.buffer, relationOrdinal, nullsMapOffset)
            } else {
              BrioNulls.relationClearNull(sink.buffer, sink.buffer, relationOrdinal, nullsMapOffset)
              sink.buffer.writeLong(capture.longValue, relationOffset)
            }

          case BrioDoubleValueEncoding =>
            capture.relationValueDataType = BrioDoubleKey
            source.extractValueScalar(cursor, instance, capture.asInstanceOf[BrioValueScalarPressCapture])
            capture.validateScalarCapture
            if (capture.isNull) {
              BrioNulls.relationSetNull(sink.buffer, sink.buffer, relationOrdinal, nullsMapOffset)
            } else {
              BrioNulls.relationClearNull(sink.buffer, sink.buffer, relationOrdinal, nullsMapOffset)
              sink.buffer.writeDouble(capture.doubleValue, relationOffset)
            }

          case BrioStringValueEncoding =>
            capture.relationValueDataType = BrioStringKey
            source.extractValueScalar(cursor, instance, capture.asInstanceOf[BrioValueScalarPressCapture])
            capture.validateScalarCapture
            if (capture.isNull) {
              BrioNulls.relationSetNull(sink.buffer, sink.buffer, relationOrdinal, nullsMapOffset)
            } else {
              BrioNulls.relationClearNull(sink.buffer, sink.buffer, relationOrdinal, nullsMapOffset)
              sink.buffer.writeShort(capture.stringValue, relationOffset)
            }

          // TODO EXTENDED TYPES
          case e: BrioElasticValueEncoding =>
            capture.relationValueDataType = BrioElasticKey
            source.extractValueScalar(cursor, instance, capture.asInstanceOf[BrioValueScalarPressCapture])
            capture.validateScalarCapture
            if (capture.isNull) {
              BrioNulls.relationSetNull(sink.buffer, sink.buffer, relationOrdinal, nullsMapOffset)
            } else {
              BrioNulls.relationClearNull(sink.buffer, sink.buffer, relationOrdinal, nullsMapOffset)
              sink.buffer.writeLong(capture.longValue, relationOffset)
            }

          // TODO EXTENDED TYPES
          case e: BrioLookupValueEncoding =>
            capture.relationValueDataType = BrioLookupKey
            source.extractValueScalar(cursor, instance, capture.asInstanceOf[BrioValueScalarPressCapture])
            capture.validateScalarCapture
            if (capture.isNull) {
              BrioNulls.relationSetNull(sink.buffer, sink.buffer, relationOrdinal, nullsMapOffset)
            } else {
              BrioNulls.relationClearNull(sink.buffer, sink.buffer, relationOrdinal, nullsMapOffset)
              sink.buffer.writeLong(capture.longValue, relationOffset)
            }
        }
      i += 1
    }
  }
}
