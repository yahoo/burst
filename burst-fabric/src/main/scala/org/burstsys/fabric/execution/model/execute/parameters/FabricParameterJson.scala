/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.execute.parameters

import com.fasterxml.jackson.core.{JsonParser, JsonToken}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.burstsys.brio.types.BrioTypes
import org.burstsys.fabric.execution.model.execute.parameters.FabricParameterValue.{map, nullScalar, nullVector, scalar, vector}
import org.burstsys.vitals.errors.safely

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex
import scala.jdk.CollectionConverters._

object FabricParameterJson {
  private val mapper = (new ObjectMapper()).registerModule(DefaultScalaModule)
  private val nameAndType:Regex = "(\\w+)\\s*:\\s*(\\w+)".r
  private val nameOnly:Regex = "(\\w+)".r

  def jsonToValues(sourceJson: String): Array[FabricParameterValue] = {
    val parser: JsonParser = mapper.getFactory.createParser(sourceJson)

    val data: ArrayBuffer[FabricParameterValue] = ArrayBuffer()

    if (parser.nextToken() != JsonToken.START_OBJECT)
      throw new IllegalStateException(s"expected the start of a Json object, but found ${parser.currentToken} at line ${parser.getCurrentLocation.getCharOffset}")

    do {
      val root: JsonNode = mapper.readTree(parser)
      for (cnode <- root.fields().asScala) {
        val (name: String, declaredType: Option[BrioTypes.BrioTypeKey]) = cnode.getKey match {
          case nameAndType(n, tname) =>
            val typ = try {
              BrioTypes.brioDataTypeKeyFromName(tname.capitalize)
            } catch safely {
              case e: Exception =>
                throw new IllegalStateException(s"invalid type modifier, '$tname', found at line ${parser.getCurrentLocation.getCharOffset}")
            }
            (n, Some(typ))
          case nameOnly(n) =>
            (n, None)
          case n =>
            throw new IllegalStateException(s"invalid field name, '$n', found at line ${parser.getCurrentLocation.getCharOffset}")
        }
        val node = cnode.getValue
        node.getNodeType match {
          case JsonNodeType.OBJECT =>
            // represent a map object
            val omap = node.fields.asScala map { me =>
              val key = me.getKey
              val value = me.getValue
              if (!value.isTextual)
                throw new IllegalStateException(s"expected a text value for element $key at line ${parser.getCurrentLocation.getCharOffset}")
              key -> value.asText()
            }
            data.append(map(name, omap.toMap))
          case JsonNodeType.ARRAY =>
            val size = node.size
            if (size == 0) {
              data.append(nullVector(name))
            } else if (node.get(0).isTextual) {
              val avalue: ArrayBuffer[String] = mutable.ArrayBuffer()
              for (i <- 0 until size) {
                val ae = node.get(i)
                if (!ae.isTextual)
                  throw new IllegalStateException(s"unexpected value node, ${ae.getNodeType}, found in array index $i at line ${parser.getCurrentLocation.getCharOffset}")
                avalue.append(ae.asText)
              }
              data.append(vector(name, avalue.toArray))
            } else if (node.get(0).isIntegralNumber) {
              val avalue: ArrayBuffer[Long] = mutable.ArrayBuffer()
              for (i <- 0 until size) {
                val ae = node.get(i)
                if (!ae.isIntegralNumber)
                  throw new IllegalStateException(s"unexpected value node, ${ae.getNodeType}, found in array index $i at line ${parser.getCurrentLocation.getCharOffset}")
                avalue.append(ae.asLong)
              }
              data.append(vector(name, avalue.toArray))
            } else if (node.get(0).isFloatingPointNumber) {
              val avalue: ArrayBuffer[Double] = mutable.ArrayBuffer()
              for (i <- 0 until size) {
                val ae = node.get(i)
                if (!ae.isFloatingPointNumber)
                  throw new IllegalStateException(s"unexpected value node, ${ae.getNodeType}, found in array index $i at line ${parser.getCurrentLocation.getCharOffset}")
                avalue.append(ae.asDouble())
              }
              data.append(vector(name, avalue.toArray))
            } else {
              throw new IllegalStateException(s"unexpected a value node, but found ${node.getNodeType}, found in first element of array at line ${parser.getCurrentLocation.getCharOffset}")
            }
          case JsonNodeType.BOOLEAN =>
            data.append(grabValue(parser, name, declaredType, BrioTypes.BrioBooleanKey, node))
          case JsonNodeType.NULL =>
            data.append(nullScalar(name))
          case JsonNodeType.NUMBER =>
            if (node.isIntegralNumber) {
              data.append(grabValue(parser, name, declaredType, BrioTypes.BrioLongKey, node))
            } else if (node.isFloatingPointNumber) {
              data.append(grabValue(parser, name, declaredType, BrioTypes.BrioDoubleKey, node))
            } else
              throw new IllegalStateException(s"unexpected Json number node, ${node.toPrettyString}, found at line ${parser.getCurrentLocation.getCharOffset}")
          case JsonNodeType.STRING =>
            data.append(grabValue(parser, name, declaredType, BrioTypes.BrioStringKey, node))
          case _ =>
            throw new IllegalStateException(s"unexpected Json node, ${node.toPrettyString}, found at line ${parser.getCurrentLocation.getCharOffset}")
        }

      }
    } while (parser.nextToken() != null)
    data.toArray

  }

  private def grabValue(parser: JsonParser, name: String, declaredType: Option[BrioTypes.BrioTypeKey], maxType: BrioTypes.BrioTypeKey, node: JsonNode): FabricParameterValue = {
    val expectedType = declaredType.getOrElse(maxType)
    assert(node.isValueNode)
    expectedType match {
      case BrioTypes.BrioBooleanKey =>
        if (node.getNodeType != JsonNodeType.BOOLEAN)
          throw new IllegalStateException(s"unexpected value of type, ${declaredType}, but found ${maxType} at line ${parser.getCurrentLocation.getCharOffset}")
        scalar(name, node.asBoolean)
      case BrioTypes.BrioStringKey =>
        if (node.getNodeType != JsonNodeType.STRING)
          throw new IllegalStateException(s"unexpected value of type, ${declaredType}, but found ${maxType} at line ${parser.getCurrentLocation.getCharOffset}")
        scalar(name, node.asText)
      case BrioTypes.BrioByteKey =>
        if (!node.isInt)
          throw new IllegalStateException(s"unexpected value of type, ${declaredType}, but found ${maxType} at line ${parser.getCurrentLocation.getCharOffset}")
        scalar(name, node.asInt.toByte)
      case BrioTypes.BrioIntegerKey =>
        if (!node.isInt)
          throw new IllegalStateException(s"unexpected value of type, ${declaredType}, but found ${maxType} at line ${parser.getCurrentLocation.getCharOffset}")
        scalar(name, node.asInt)
      case BrioTypes.BrioLongKey =>
        if (!node.isLong && !node.isInt)
          throw new IllegalStateException(s"unexpected value of type, ${declaredType}, but found ${maxType} at line ${parser.getCurrentLocation.getCharOffset}")
        scalar(name, node.asLong)
      case BrioTypes.BrioDoubleKey =>
        if (!node.isDouble && !node.isInt && !node.isDouble )
          throw new IllegalStateException(s"unexpected value of type, ${declaredType}, but found ${maxType} at line ${parser.getCurrentLocation.getCharOffset}")
        scalar(name, node.asDouble)
      case _ =>
        throw new IllegalStateException(s"unexpected value of type, ${maxType} at line ${parser.getCurrentLocation.getCharOffset}")
    }
  }
}
