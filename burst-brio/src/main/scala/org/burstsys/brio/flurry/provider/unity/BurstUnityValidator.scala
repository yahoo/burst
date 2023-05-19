/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider.unity

import org.burstsys.brio.BrioLatticeRoot
import org.burstsys.brio.blob.BrioMutableBlob
import org.burstsys.brio.lattice.BrioLatticeReference
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.model.schema.encoding.BrioSchematic
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.tesla.offheap
import org.burstsys.vitals.errors.messageFromException
import org.burstsys.vitals.errors._
import org.burstsys.vitals.text.VitalsTextCodec


object BurstUnityValidator {
  val schema: BrioSchema = BrioSchema("unity")

  def validateBlob(blob: TeslaMutableBuffer): Unit = {

    try {
      implicit val text: VitalsTextCodec = VitalsTextCodec()

      val blobItem = BrioMutableBlob(blob)

      val dictionary = blobItem.dictionary
      val reader = blobItem.data
      val sourceRootInstance = blobItem.reference

      // check encoding version
      val blobEncodingVersion = offheap.getInt(blobItem.dataPtr)

      // check root version
      val rootBrioObjectVersion = offheap.getInt(blobItem.dataPtr + SizeOfInteger)

      val userStructureKey = schema.structureKey(schema.rootStructureType)
      val userSchematic: BrioSchematic = schema.schematic(userStructureKey, rootBrioObjectVersion) // version 1 (0)

      val outputRootInstance1 = BrioLatticeReference(BrioLatticeRoot)

      ///////////////////////////
      // look at the output lattice
      ///////////////////////////
      val userInstance = BrioLatticeReference(BrioLatticeRoot)

      // now traverse the new blob
      {
        // check flurryid
        val fieldKey = schema.relationList(1, userStructureKey).filter(_.relationName == "id").head.relationOrdinal
        val stringKey = userInstance.valueScalarString(reader, userSchematic, fieldKey)
        val string = dictionary.stringLookup(stringKey)
      }

      // TODO SHOULD EXTEND TO FULL TRAVERSAL

    } catch safely {
      case t: Throwable =>
        log error(s"unity blob validation failed: ${messageFromException(t)}", t)
        throw t
    }
  }


}
