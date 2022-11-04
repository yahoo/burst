/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.mock

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.blob.BrioBlob
import org.burstsys.brio.lattice.BrioLatticeReference
import org.burstsys.brio.model.schema._
import org.burstsys.brio.model.schema.types._
import org.burstsys.brio.types.BrioTypes.{BrioSchemaName, BrioTypeKey}
import org.burstsys.fabric.wave.execution.model.gather.FabricGather
import org.burstsys.fabric.wave.execution.model.gather.data.MockDataGather
import org.burstsys.fabric.wave.execution.model.scanner.{FabricPlaneScanner, FabricPlaneScannerContext}
import org.burstsys.tesla.buffer.TeslaBufferReader
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.text.VitalsTextCodec

/**
 * scanner for unit tests
 *
 * @param schemaName
 */
final case
class MockScanner(var schemaName: BrioSchemaName) extends FabricPlaneScannerContext with FabricPlaneScanner {

  override def scannerName: String = "Mock"

  @transient lazy val schema: BrioSchema = BrioSchema(schemaName)

  /**
   * scan a single item. This is called by one of a number of particle threads.
   *
   * @param item
   * @return
   */
  override def apply(item: BrioBlob): FabricGather = {
    implicit val text: VitalsTextCodec = VitalsTextCodec()
    val buffer = item.data
    val reference = item.reference
    val rootStructureType = schema.structureKey(schema.rootStructureType)
    recurse(buffer, rootStructureType, reference)
    val gather = MockDataGather().initialize(this)
    // assumeMetrics(item, gather)
    gather
  }

  private
  def recurse(reader: TeslaBufferReader, structureType: BrioTypeKey, reference: BrioLatticeReference): Unit = {
    // setup our state axis recording all the active references in our traversal.
    val versionKey = reference.versionKey(reader)
    val schematic = schema.schematic(structureType, versionKey)

    var i = 0
    while (i < schematic.relationCount) {
      if (!reference.relationIsNull(reader, schematic, i.toByte)) {
        schematic.form(i.toByte) match {
          case BrioValueScalarRelation =>
            val size = reference.relationSize(reader, schematic, i.toByte)

          case BrioValueVectorRelation =>
            val size = reference.relationSize(reader, schematic, i.toByte)

          case BrioValueMapRelation =>
            val size = reference.relationSize(reader, schematic, i.toByte)
            val mapKeys = reference.valueMapStringStringKeys(reader, schematic, i.toByte)

          case BrioReferenceScalarRelation =>
            val size = reference.relationSize(reader, schematic, i.toByte)
            val child = reference.referenceScalar(reader, schematic, i.toByte)
            val childType = schematic.valueTypeKey(i.toByte)
            recurse(reader, childType, child)

          case BrioReferenceVectorRelation =>
            val structName = schema.structureName(schematic.structureKey)
            val field = schema.relationList(versionKey, schematic.structureKey)(i.toByte)
            val size = reference.relationSize(reader, schematic, i.toByte)
            val vector = reference.referenceVectorIterator(reader, schematic, i.toByte)
            val memberType = schematic.valueTypeKey(i.toByte)
            var j = 0
            var vectorIndex = vector.start(reader)
            while (j < vector.length(reader)) {
              val member = vector.member(reader, vectorIndex)
              val memberVersion = member.versionKey(reader)
              recurse(reader, memberType, member)
              vectorIndex = vector.advance(reader, vectorIndex)
              j += 1
            }

          case _ =>
            val msg = s"$burstModuleName bad relation for key=$i fieldKey=$i"
            log error msg
            throw new RuntimeException(msg)

        }
      }
      i += 1
    }

  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * The scanner is written on the supervisor to send to the workers to read. It is not sent back from the
   * worker to the supervisor.
   *
   * @param kryo
   * @param output
   */
  override
  def write(kryo: Kryo, output: Output): Unit = {
    try {
      super.write(kryo, output)
      output writeString schemaName
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }
  }

  /**
   * the scanner is read on the worker to start a query execution/scan
   *
   * @param kryo
   * @param input
   */
  override
  def read(kryo: Kryo, input: Input): Unit = {
    try {
      super.read(kryo, input)
      schemaName = input.readString
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }
  }

}
