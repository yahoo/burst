/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.alloy

import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream
import com.fasterxml.jackson.core.{JsonFactory, JsonGenerator}
import org.burstsys.brio.blob.BrioBlob
import org.burstsys.brio.dictionary.BrioDictionary
import org.burstsys.brio.lattice.BrioLatticeReference
import org.burstsys.brio.model.schema._
import org.burstsys.brio.model.schema.types._
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.tesla.buffer.TeslaBufferReader
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.text.VitalsTextCodec

final case
class BlobToJson(schemaName: BrioSchemaName = "Quo", fileName: String) {
  private final val jFactory = new JsonFactory()

  @transient lazy val schema: BrioSchema = BrioSchema(schemaName)
  implicit val text: VitalsTextCodec = VitalsTextCodec()

  private var json = if (fileName.endsWith(".gz")) new GZIPOutputStream(new FileOutputStream(fileName)) else new FileOutputStream(fileName)
  private var writer = jFactory.createGenerator(json) //.setPrettyPrinter(new DefaultPrettyPrinter())

  def start(): BlobToJson = {
    writer.writeStartArray()
    this
  }

  def apply(item: BrioBlob): BlobToJson = {
    val dictionary = item.dictionary
    // val dictionaryDump = dictionary.dump
    val buffer = item.data
    val reference = item.reference
    val rootStructureType = schema.structureKey(schema.rootStructureType)
    recurse(dictionary, buffer, rootStructureType, reference, writer)
    writer.flush()
    this
  }

  def close: BlobToJson = {
    writer.writeEndArray()
    writer.close()
    this
  }


  private
  def recurse(implicit dictionary: BrioDictionary, reader: TeslaBufferReader, structureType: BrioTypeKey, reference: BrioLatticeReference, writer: JsonGenerator): Unit = {
    val versionKey = reference.versionKey(reader)
    val schematic = schema.schematic(structureType, versionKey)

    writer.writeStartObject()

    var i = 0
    while (i < schematic.relationCount) {
      if (!reference.relationIsNull(reader, schematic, i.toByte)) {
        val childRelation = schematic.relationArray(i.toByte)
        val childName = childRelation.relationName
        schematic.form(i.toByte) match {
          case BrioValueScalarRelation =>
            childRelation.valueOrReferenceTypeKey match {
              case BrioStringKey =>
                val v = dictionary.stringLookup(reference.valueScalarString(reader, schematic, i.toByte))
                writer.writeStringField(childName, v)
              case BrioBooleanKey =>
                val v = reference.valueScalarBoolean(reader, schematic, i.toByte)
                writer.writeBooleanField(childName, v)
              case BrioByteKey =>
                val v = reference.valueScalarByte(reader, schematic, i.toByte)
                writer.writeNumberField(childName, v)
              case BrioShortKey =>
                val v = reference.valueScalarShort(reader, schematic, i.toByte)
                writer.writeNumberField(childName, v)
              case BrioIntegerKey =>
                val v = reference.valueScalarInteger(reader, schematic, i.toByte)
                writer.writeNumberField(childName, v)
              case BrioLongKey =>
                val v = reference.valueScalarLong(reader, schematic, i.toByte)
                writer.writeNumberField(childName, v)
              case BrioDoubleKey =>
                val v = reference.valueScalarDouble(reader, schematic, i.toByte)
                writer.writeNumberField(childName, v)
              case t =>
                throw new IllegalStateException(s"unknown brio type: $t")
            }
          case BrioValueVectorRelation =>
            writer.writeArrayFieldStart(childName)
            val child = reference.referenceScalar(reader, schematic, i.toByte)
            val childType = schematic.valueTypeKey(i.toByte)
            val vector = reference.valueVectorIterator(reader, schematic, i.toByte)
            var j = 0
            var vectorIndex = vector.start(reader)
            while (j < vector.length(reader)) {
              childRelation.valueOrReferenceTypeKey match {
                case BrioStringKey =>
                  val v = vector.readString(reader, vectorIndex)(text, dictionary)
                  writer.writeStringField(childName, v)
                case BrioBooleanKey =>
                  val v = vector.readBoolean(reader, vectorIndex)
                  writer.writeBooleanField(childName, v)
                case BrioByteKey =>
                  val v = vector.readByte(reader, vectorIndex)
                  writer.writeNumberField(childName, v)
                case BrioShortKey =>
                  val v = vector.readShort(reader, vectorIndex)
                  writer.writeNumberField(childName, v)
                case BrioIntegerKey =>
                  val v = vector.readInteger(reader, vectorIndex)
                  writer.writeNumberField(childName, v)
                case BrioLongKey =>
                  val v = vector.readLong(reader, vectorIndex)
                  writer.writeNumberField(childName, v)
                case BrioDoubleKey =>
                  val v = vector.readDouble(reader, vectorIndex)
                  writer.writeNumberField(childName, v)
                case t =>
                  throw new IllegalStateException(s"unknown brio type: $t")
              }
              vectorIndex = vector.advance(reader, vectorIndex, childType)
              j += 1
            }
            val size = reference.relationSize(reader, schematic, i.toByte)
            writer.writeEndArray()
          case BrioValueMapRelation =>
            writer.writeObjectFieldStart(childName)
            val mapKeys = reference.valueMapStringStringKeys(reader, schematic, i.toByte)
            for (k <- mapKeys) {
              val kv = dictionary.stringLookup(k)
              val vk = reference.valueMapStringString(reader, schematic, i.toByte, k)
              val vv = dictionary.stringLookup(vk)
              writer.writeStringField(kv, vv)
            }
            writer.writeEndObject()
          case BrioReferenceScalarRelation =>
            val child = reference.referenceScalar(reader, schematic, i.toByte)
            val childType = schematic.valueTypeKey(i.toByte)
            writer.writeFieldName(childName)
            recurse(dictionary, reader, childType, child, writer)
          case BrioReferenceVectorRelation =>
            writer.writeArrayFieldStart(childName)
            val vector = reference.referenceVectorIterator(reader, schematic, i.toByte)
            val memberType = schematic.valueTypeKey(i.toByte)
            var j = 0
            var vectorIndex = vector.start(reader)
            while (j < vector.length(reader)) {
              val member = vector.member(reader, vectorIndex)
              recurse(dictionary, reader, memberType, member, writer)
              vectorIndex = vector.advance(reader, vectorIndex)
              j += 1
            }
            writer.writeEndArray()
          case _ =>
            val msg = s"bad relation for key=$i fieldKey=$i"
            log error burstStdMsg(msg)
            throw VitalsException(msg)

        }
      }
      i += 1
    }
    writer.writeEndObject()

  }
}
