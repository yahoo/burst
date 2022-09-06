/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.json

import com.fasterxml.jackson.databind.node.ArrayNode
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press._
import org.burstsys.brio.types.BrioTypes

import scala.jdk.CollectionConverters._

final case
class JsonPressSource(schema: BrioSchema, root: BrioPressInstance) extends BrioPressSourceBase with BrioPressSource {

  override
  def extractRootReferenceScalar(): BrioPressInstance = {
    // log debug s"press flurryId=${root.asInstanceOf[AlloyJsonCursor].jsonNode.findValue("flurryId").asText()}"
    root
  }

  override
  def extractReferenceScalar(cursor: BrioPressCursor, parentInstance: BrioPressInstance): BrioPressInstance = {
    val jc = ensureJsonParentAt(cursor, parentInstance)
    val field = jc.nodeForRelation(cursor.relationName)
    if (field == null)
      return null
    else if (!field.isObject)
      throw new IllegalStateException(s"expected json object for scalar reference field ${cursor.pathName} in json document around `${field.textValue()}`")

    JsonCursor(jc.schemaVersion, field, cursor.pathKey)
  }

  override
  def extractReferenceVector(cursor: BrioPressCursor, parentInstance: BrioPressInstance): Iterator[BrioPressInstance] = {
    val jc = ensureJsonParentAt(cursor, parentInstance)
    val field = jc.nodeForRelation(cursor.relationName)
    if (field == null)
      return null
    else if (!field.isArray)
      throw new IllegalStateException(s"expected json array for reference vector field ${cursor.pathName} in json document around `${field.textValue()}`")

    new Iterator[BrioPressInstance]() {
      private val i = field.asInstanceOf[ArrayNode].elements()
      override def hasNext: Boolean = i.hasNext

      override def next(): BrioPressInstance = {
        JsonCursor(jc.schemaVersion, i.next(), cursor.pathKey)
      }
    }
  }

  override
  def extractValueScalar(cursor: BrioPressCursor, parentInstance: BrioPressInstance, capture: BrioValueScalarPressCapture): Unit = {
    val jc = ensureJsonParentAt(cursor, parentInstance)
    val field = jc.nodeForRelation(cursor.relationName)
    if (field == null) {
      capture.markRelationNull()
      return
    } else if (!field.isValueNode)
      throw new IllegalStateException(s"expected json value for scalar value field ${cursor.pathName} in json document around `${field.textValue()}`")

    schema.nodeForPathKey(cursor.pathKey).relation.valueEncoding.typeKey match {
      case BrioTypes.BrioBooleanKey => capture.booleanValue(field.asBoolean)
      case BrioTypes.BrioByteKey => capture.byteValue(field.asInt.toByte)
      case BrioTypes.BrioDoubleKey => capture.doubleValue(field.asDouble)
      case BrioTypes.BrioShortKey => capture.shortValue(field.asInt.toShort)
      case BrioTypes.BrioIntegerKey => capture.integerValue(field.asInt)
      case BrioTypes.BrioLongKey => capture.longValue(field.asLong)
      case BrioTypes.BrioStringKey => capture.stringValue(capture.dictionaryEntry(field.asText))
      case k =>
        throw new IllegalStateException(s"invalid BrioTypeKey $k seen")
    }
  }

  override
  def extractValueMap(cursor: BrioPressCursor, parentInstance: BrioPressInstance, capture: BrioValueMapPressCapture): Unit = {
    val jc = ensureJsonParentAt(cursor, parentInstance)
    val field = jc.nodeForRelation(cursor.relationName)
    if (field == null) {
      capture.markRelationNull()
      return
    } else if (!field.isObject)
      throw new IllegalStateException(s"expected json object for value map field ${cursor.pathName} in json document around `${field.textValue()}`")

    val relation = schema.nodeForPathKey(cursor.pathKey).relation
    val fields = field.fields.asScala
    (relation.keyEncoding.typeKey, relation.valueEncoding.typeKey) match {
      case (k, v) if k == BrioTypes.BrioStringKey && v == BrioTypes.BrioStringKey =>
        extractStringStringMap(capture, fields.filter(e => e.getValue.isValueNode).map(e => (e.getKey, e.getValue.asText)))
      case (k, v) if k == BrioTypes.BrioLongKey && v == BrioTypes.BrioStringKey =>
        extractLongStringMap(capture, fields.filter(e => e.getValue.isValueNode).map(e => (e.getKey.toLong, e.getValue.asText)).toMap)
      case (k, v) =>
        throw new IllegalStateException(s"Map found with invalid types $k -> $v seen at map field ${cursor.pathName} in json document around `${field.textValue()}`")
    }
  }

  override
  def extractValueVector(cursor: BrioPressCursor, parentInstance: BrioPressInstance, capture: BrioValueVectorPressCapture): Unit = {
    val jc = ensureJsonParentAt(cursor, parentInstance)
    if (!jc.jsonNode.has(cursor.relationName)) {
      capture.markRelationNull()
      return
    }

    val fieldNode = jc.jsonNode.get(cursor.relationName)
    if (fieldNode.isNull) {
      capture.markRelationNull()
      return
    } else if (!fieldNode.isArray)
      throw new IllegalStateException(s"expected json array for value vector field ${cursor.pathName} in json document around `${fieldNode.textValue()}`")

    val elements = fieldNode.elements.asScala
    schema.nodeForPathKey(cursor.pathKey).relation.valueEncoding.typeKey match {
      case BrioTypes.BrioBooleanKey => extractBooleanValueVector(capture, elements.map(_.asBoolean).toArray)
      case BrioTypes.BrioByteKey => extractByteValueVector(capture, elements.map(_.asInt.toByte).toArray)
      case BrioTypes.BrioDoubleKey => extractDoubleValueVector(capture, elements.map(_.asDouble).toArray)
      case BrioTypes.BrioShortKey => extractShortValueVector(capture, elements.map(_.asInt.toShort).toArray)
      case BrioTypes.BrioIntegerKey => extractIntegerValueVector(capture, elements.map[Integer](_.asInt).toArray)
      case BrioTypes.BrioLongKey => extractLongValueVector(capture, elements.map(_.asLong).toArray)
      case BrioTypes.BrioStringKey => extractStringValueVector(capture, elements.map(_.asText).toArray)
      case k =>
        throw new IllegalStateException(s"invalid BrioTypeKey $k seen for vector field ${cursor.pathName} in json document around `${fieldNode.textValue()}`")
    }
  }

  private def ensureJsonParentAt(cursor: BrioPressCursor, instance: BrioPressInstance): JsonCursor = {
    instance match {
      case jc: JsonCursor => jc
      case _ => throw new IllegalStateException(s"invalid BrioPressInstance ${instance.getClass} seen at ${cursor.relationName}")
    }
  }
}
