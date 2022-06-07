/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio

import com.fasterxml.jackson.core.{JsonParser, JsonToken}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.BrioPressInstance
import org.burstsys.brio.types.BrioPath.BrioPathKey
import org.burstsys.brio.types.BrioTypes
import org.burstsys.brio.types.BrioTypes.BrioVersionKey

import java.io.InputStream

package object json {

  final case class JsonCursor(override val schemaVersion: BrioVersionKey, jsonNode:  JsonNode, pathKey: BrioPathKey) extends BrioPressInstance {
    def nodeForRelation(relation: BrioTypes.BrioRelationName): JsonNode =
      if (jsonNode.has(relation)) {
        val n = jsonNode.get(relation)
        if (n.isNull) null else n
      } else null
  }

  private final val mapper = new ObjectMapper().registerModule(DefaultScalaModule)
  final def getJsonSource(schema: BrioSchema, source: InputStream): Iterator[BrioPressInstance] = {
    val parser: JsonParser = mapper.getFactory.createParser(source)

    parser.nextToken()
    if (!parser.isExpectedStartArrayToken)
      throw new IllegalStateException(s"expected the start of a Json array, but found ${parser.currentToken} at line ${parser.getCurrentLocation.getCharOffset}")

    parser.nextToken()
    if (parser.hasToken(JsonToken.END_ARRAY)) {
      // empty
      return new Iterator[JsonCursor]() {
        override def hasNext: Boolean = {
          false
        }

        override def next(): JsonCursor = {
          throw new java.util.NoSuchElementException()
        }
      }
    } else if (!parser.isExpectedStartObjectToken)
      throw new IllegalStateException(s"expected the start of a Json object, but found ${parser.currentToken} at line ${parser.getCurrentLocation.getCharOffset}")

    new Iterator[JsonCursor]() {
      override def hasNext: Boolean = {
        parser.isExpectedStartObjectToken
      }

      override def next(): JsonCursor = {
        val rootNode: JsonNode = mapper.readTree(parser)
        parser.nextToken()
        JsonCursor(schema.versionCount, rootNode, schema.rootNode.pathKey)
      }
    }
  }

}
