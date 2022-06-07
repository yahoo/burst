/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.schema.test

import org.burstsys.brio.flurry.provider.quo
import org.burstsys.brio.flurry.provider.unity
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.tesla
import org.burstsys.tesla.thread.request.TeslaRequestCoupler

class BrioSchemaSpec extends AbstractBrioSchemaSpec {


  it should "build quo model" in {
    TeslaRequestCoupler {

      val schema = BrioSchema("Quo")
      schema.name should equal("Quo")
      schema.rootRelationName should equal("user")
      schema.rootStructureType should equal("User")
      schema.versionCount should equal(3)
      schema.pathCount should equal(56)

      val events1 = schema.nodeForPathName("user.sessions.events").relation
      events1.relationName should be("events")
      events1.relationForm.isVector should be(true)
      events1.relationForm.isReference should be(true)

      val events2 = schema.nodeForPathKey(schema.keyForPath("user.sessions.events")).relation
      events2.relationName should be("events")
      events2.relationForm.isVector should be(true)
      events2.relationForm.isReference should be(true)
      events2.valueOrReferenceTypeKey should be(57)

      schema.structureKey("Event") should equal(57)
      schema.structureName(57) should equal("Event")

      val eventFields = schema.relationList(3, 57)
      eventFields.length should equal(6)
      eventFields.head.relationName should equal("eventId")

      val eventSchematic1 = schema.schematic("user.sessions.events", 3)
      eventSchematic1.versionKey should equal(3)
      eventSchematic1.relationCount should equal(6)
      eventSchematic1.structureKey should equal(57)

      val eventSchematic2 = schema.schematic(57, 3)
      eventSchematic2.versionKey should equal(3)
      eventSchematic2.relationCount should equal(6)
      eventSchematic2.structureKey should equal(57)

      eventSchematic2.relationOrdinal("eventId") should equal(0)
    }
  }

  it should "press mock quo model" in {
    TeslaRequestCoupler {
      val presses = quo.mockBlobs // TODO free buffers
      presses.length should equal(10)
      presses.foreach(tesla.buffer.factory.releaseBuffer)
    }
  }

  it should "press mock unity model" in {
    TeslaRequestCoupler {
      val presses = unity.mockBlobs // TODO free buffers
      presses.length should equal(1000)
      presses.foreach(tesla.buffer.factory.releaseBuffer)
    }
  }


}
