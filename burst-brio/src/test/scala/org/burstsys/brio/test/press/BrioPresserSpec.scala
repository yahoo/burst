/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test.press

import org.burstsys.brio.BrioLatticeRoot
import org.burstsys.brio.dictionary.BrioDictionary
import org.burstsys.brio.lattice.BrioLatticeReference
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.model.schema.encoding.BrioSchematic
import org.burstsys.brio.press._
import org.burstsys.brio.test.BrioAbstractSpec
import org.burstsys.tesla.buffer.factory
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.text.VitalsTextCodec
import org.burstsys.{brio, tesla}

//@Ignore
class BrioPresserSpec extends BrioAbstractSpec {

  "Brio Model" should "press test schema" in {
    TeslaWorkerCoupler {
      val presserSchema = BrioSchema("presser")
      implicit val text: VitalsTextCodec = VitalsTextCodec()
      val buffer = tesla.buffer.factory.grabBuffer(1e6.toInt)
      val dictionary = brio.dictionary.factory.grabMutableDictionary()
      val sink = BrioPressSink(buffer, dictionary)
      val presser = BrioPresser(sink)
      try {
        presser.press(presserSchema, BrioMockPressSource())
        val bytes = sink.buffer.toBytes
        sink.dictionary.words should equal(51)
        bytes.length should equal(818)
        validatePress(bytes, sink.dictionary)
      } finally {
        tesla.buffer.factory.releaseBuffer(buffer)
        brio.dictionary.factory.releaseMutableDictionary(dictionary)
      }

      def validatePress(bytes: Array[Byte], dictionary: BrioDictionary): Unit = {
        val reader = factory.grabBuffer(bytes.length)
        reader.loadBytes(bytes)
        val latticeRoot = BrioLatticeReference(BrioLatticeRoot)
        latticeRoot.versionKey(reader) should equal(2)
        val rootSchematic = presserSchema.schematic(presserSchema.structureKey("RootStructure"), BrioMockPressModel.schemaVersion)
        dictionary.stringLookup(latticeRoot.valueScalarString(reader, rootSchematic, rootSchematic.relationOrdinal("f0"))) should equal("rootytooty")
        latticeRoot.valueScalarLong(reader, rootSchematic, rootSchematic.relationOrdinal("f1")) should equal(1234567890)
        latticeRoot.valueScalarShort(reader, rootSchematic, rootSchematic.relationOrdinal("f2")) should equal(2222)
        validate2(reader, latticeRoot, rootSchematic)
        validate3(dictionary, reader, latticeRoot, rootSchematic)
        validateUnity(dictionary, reader, latticeRoot, rootSchematic)
        validate1(reader, latticeRoot, rootSchematic)
      }

      def validate1(reader: TeslaMutableBuffer, parentInstance: BrioLatticeReference, parentSchematic: BrioSchematic): Unit = {
        val f4Key = parentSchematic.relationOrdinal("f4")
        parentInstance.relationSize(reader, parentSchematic, f4Key) should equal(1)
        val vector = parentInstance.referenceVectorIterator(reader, parentSchematic, f4Key)
        var vectorIndex = vector.start(reader)
        val member = vector.member(reader, vectorIndex)
        val memberVersion = member.versionKey(reader)
        val memberSchematic = presserSchema.schematic(presserSchema.structureKey("SecondLevelStructure"), parentInstance.versionKey(reader))
        member.valueScalarLong(reader, memberSchematic, memberSchematic.relationOrdinal("f0")) should equal(331)
        member.valueScalarLong(reader, memberSchematic, memberSchematic.relationOrdinal("f1")) should equal(332)
        member.valueScalarDouble(reader, memberSchematic, memberSchematic.relationOrdinal("f2")).toInt should equal(333)
        validate5(reader, member, memberSchematic)
        validate6(reader, member, memberSchematic)
      }

      def validate5(reader: TeslaMutableBuffer, parentInstance: BrioLatticeReference, parentSchematic: BrioSchematic): Unit = {
        val f3Key = parentSchematic.relationOrdinal("f3")
        parentInstance.relationSize(reader, parentSchematic, f3Key) should equal(3)
        val vector = parentInstance.referenceVectorIterator(reader, parentSchematic, f3Key)
        var vectorIndex = vector.start(reader)
        val member = vector.member(reader, vectorIndex)
        val memberVersion = member.versionKey(reader)
        val memberSchematic = presserSchema.schematic(presserSchema.structureKey("ThirdLevelStructure"), parentInstance.versionKey(reader))
        member.valueScalarLong(reader, memberSchematic, memberSchematic.relationOrdinal("f0")) should equal(31)
        member.valueScalarLong(reader, memberSchematic, memberSchematic.relationOrdinal("f1")) should equal(32)
        member.valueMapStringStringKeys(reader, memberSchematic, memberSchematic.relationOrdinal("f2")).map(
          dictionary.stringLookup
        ).toList.sorted should equal(Array("11", "7", "9"))
        member.valueVectorDouble(reader, memberSchematic, memberSchematic.relationOrdinal("f3")).sorted should equal(Array(0.7, 0.8, 0.9))
      }

      def validate6(reader: TeslaMutableBuffer, parentInstance: BrioLatticeReference, parentSchematic: BrioSchematic): Unit = {
        val f4Key = parentSchematic.relationOrdinal("f4")
        parentInstance.relationSize(reader, parentSchematic, f4Key) should equal(1)
        val childInstance = parentInstance.referenceScalar(reader, parentSchematic, f4Key)
        childInstance should not equal null
        val childSchematic = presserSchema.schematic(presserSchema.structureKey("ThirdLevelStructure"), childInstance.versionKey(reader))
        childSchematic should not equal null
        childInstance.valueScalarLong(reader, childSchematic, childSchematic.relationOrdinal("f0")) should equal(61)
        childInstance.valueScalarLong(reader, childSchematic, childSchematic.relationOrdinal("f1")) should equal(62)
        childInstance.valueMapStringStringKeys(reader, childSchematic, childSchematic.relationOrdinal("f2")).map(
          dictionary.stringLookup(_).toDouble
        ).sorted should equal(Array(31.0, 33.0, 35.0, 37.0, 39.0, 41.0))
        childInstance.valueVectorDouble(reader, childSchematic, childSchematic.relationOrdinal("f3")).sorted should equal(Array(1.9, 2.0, 2.1, 2.2, 2.3, 2.4))
      }

      def validate2(reader: TeslaMutableBuffer, parentInstance: BrioLatticeReference, parentSchematic: BrioSchematic): Unit = {
        val childInstance = parentInstance.referenceScalar(reader, parentSchematic, parentSchematic.relationOrdinal("f3"))
        childInstance should not equal null
        val childSchematic = presserSchema.schematic(presserSchema.structureKey("SecondLevelStructure"), childInstance.versionKey(reader))
        childSchematic should not equal null
        childInstance.valueScalarLong(reader, childSchematic, childSchematic.relationOrdinal("f0")) should equal(221)
        childInstance.valueScalarLong(reader, childSchematic, childSchematic.relationOrdinal("f1")) should equal(222)
        childInstance.valueScalarDouble(reader, childSchematic, childSchematic.relationOrdinal("f2")).toInt should equal(223)
        validate4(reader, childInstance, childSchematic)
      }

      def validate4(reader: TeslaMutableBuffer, parentInstance: BrioLatticeReference, parentSchematic: BrioSchematic): Unit = {
        val f3Key = parentSchematic.relationOrdinal("f3")
        parentInstance.relationSize(reader, parentSchematic, f3Key) should equal(2)
        val vector = parentInstance.referenceVectorIterator(reader, parentSchematic, f3Key)
        var vectorIndex = vector.start(reader)
        var member = vector.member(reader, vectorIndex)
        var memberVersion = member.versionKey(reader)
        var childSchematic = presserSchema.schematic(presserSchema.structureKey("ThirdLevelStructure"), memberVersion)
        member.valueScalarLong(reader, childSchematic, childSchematic.relationOrdinal("f0")) should equal(71)
        member.valueScalarLong(reader, childSchematic, childSchematic.relationOrdinal("f1")) should equal(72)
        member.valueMapStringStringKeys(reader, childSchematic, childSchematic.relationOrdinal("f2")).map(
          dictionary.stringLookup
        ) should equal(Array("-1"))
        member.valueVectorDouble(reader, childSchematic, childSchematic.relationOrdinal("f3")).sorted should equal(Array(0.1, 0.2))

        vectorIndex = vector.advance(reader, vectorIndex)
        member = vector.member(reader, vectorIndex)
        memberVersion = member.versionKey(reader)
        childSchematic = presserSchema.schematic(presserSchema.structureKey("ThirdLevelStructure"), memberVersion)
        member.valueScalarLong(reader, childSchematic, childSchematic.relationOrdinal("f0")) should equal(73)
        member.valueScalarLong(reader, childSchematic, childSchematic.relationOrdinal("f1")) should equal(74)
        member.valueMapStringStringKeys(reader, childSchematic, childSchematic.relationOrdinal("f2")).sorted.map(
          dictionary.stringLookup
        ) should equal(Array("1"))
        member.valueVectorDouble(reader, childSchematic, childSchematic.relationOrdinal("f3")).sorted should equal(Array(0.3, 0.4))
      }

      def validate3(dictionary: BrioDictionary, reader: TeslaMutableBuffer, parentInstance: BrioLatticeReference, parentSchematic: BrioSchematic): Unit = {
        val addedKey = parentSchematic.relationOrdinal("added")
        parentInstance.relationIsNull(reader, parentSchematic, addedKey) should equal(false)
        parentInstance.relationSize(reader, parentSchematic, addedKey) should equal(1)
        val childInstance = parentInstance.referenceScalar(reader, parentSchematic, addedKey)
        childInstance should not equal null
        val childSchematic = presserSchema.schematic(presserSchema.structureKey("AddedStructure"), childInstance.versionKey(reader))
        dictionary.stringLookup(childInstance.valueScalarString(reader, childSchematic, childSchematic.relationOrdinal("f0"))) should equal("added")
        childInstance.valueScalarDouble(reader, childSchematic, childSchematic.relationOrdinal("f1")) should equal(666.666)
        childInstance.valueScalarBoolean(reader, childSchematic, childSchematic.relationOrdinal("f3")) should equal(true)
        childInstance.valueScalarBoolean(reader, childSchematic, childSchematic.relationOrdinal("f4")) should equal(false)
        childInstance.valueVectorString(reader, childSchematic, childSchematic.relationOrdinal("f2")).map(
          dictionary.stringLookup
        ) should equal(Array("Hello", "Goodbye"))
      }

      def validateUnity(dictionary: BrioDictionary, reader: TeslaMutableBuffer, parentInstance: BrioLatticeReference, parentSchematic: BrioSchematic): Unit = {
        val applicationKey = parentSchematic.relationOrdinal("application")
        parentInstance.relationIsNull(reader, parentSchematic, applicationKey) should equal(false)
        parentInstance.relationSize(reader, parentSchematic, applicationKey) should equal(1)
        val applicationInstance = parentInstance.referenceScalar(reader, parentSchematic, applicationKey)
        applicationInstance should not equal null
        val applicationSchematic = presserSchema.schematic(presserSchema.structureKey("ApplicationStructure"), applicationInstance.versionKey(reader))

        val firstUseKey = applicationSchematic.relationOrdinal("firstUse")
        val firstUseInstance = applicationInstance.referenceScalar(reader, applicationSchematic, firstUseKey)
        firstUseInstance should not equal null
        val firstUseSchematic = presserSchema.schematic(presserSchema.structureKey("UseStructure"), firstUseInstance.versionKey(reader))

        val mostUseKey = applicationSchematic.relationOrdinal("mostUse")
        val mostUseInstance = applicationInstance.referenceScalar(reader, applicationSchematic, mostUseKey)
        mostUseInstance should not equal null
        val mostUseSchematic = presserSchema.schematic(presserSchema.structureKey("UseStructure"), mostUseInstance.versionKey(reader))

        val lastUseKey = applicationSchematic.relationOrdinal("lastUse")
        val lastUseInstance = applicationInstance.referenceScalar(reader, applicationSchematic, lastUseKey)
        lastUseInstance should not equal null
        val lastUseSchematic = presserSchema.schematic(presserSchema.structureKey("UseStructure"), lastUseInstance.versionKey(reader))

        dictionary.stringLookup(firstUseInstance.valueScalarString(reader, firstUseSchematic, firstUseSchematic.relationOrdinal("tag"))) should equal("firstUse")
        dictionary.stringLookup(mostUseInstance.valueScalarString(reader, mostUseSchematic, mostUseSchematic.relationOrdinal("tag"))) should equal("mostUse")
        dictionary.stringLookup(lastUseInstance.valueScalarString(reader, lastUseSchematic, lastUseSchematic.relationOrdinal("tag"))) should equal("lastUse")
      }

    }
  }

}
