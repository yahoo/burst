/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test.model

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.test.BrioAbstractSpec
import org.burstsys.vitals.errors.{VitalsException, safely}

class BrioReadAccessSpec extends BrioAbstractSpec {

  it should "test read access in self container" in {
    val schema = BrioSchema("unity")
    val traversalPoint = schema.nodeForPathName("user.sessions.events")
    val nodeToBeRead = schema.nodeForPathName("user.sessions.events.eventType")
    traversalPoint.canReachRelation(nodeToBeRead) should be (true)
  }

  it should "test read access in root container" in {
    val schema = BrioSchema("unity")
    val traversalPoint = schema.nodeForPathName("user.sessions.events")
    val nodeToBeRead = schema.nodeForPathName("user.id")
    traversalPoint.canReachRelation(nodeToBeRead) should be (true)
  }

  it should "test read access in parent container" in {
    val schema = BrioSchema("unity")
    val traversalPoint = schema.nodeForPathName("user.sessions.events")
    val nodeToBeRead = schema.nodeForPathName("user.sessions.startTime")
    traversalPoint.canReachRelation(nodeToBeRead) should be (true)
  }

  it should "test read access in child container" in {
    val schema = BrioSchema("unity")
    val traversalPoint = schema.nodeForPathName("user")
    val nodeToBeRead = schema.nodeForPathName("user.sessions.startTime")
    traversalPoint.canReachRelation(nodeToBeRead) should be (false)
  }

  it should "test read access in one step reference scalar tunnel" in {
    val schema = BrioSchema("unity")
    val traversalPoint = schema.nodeForPathName("user.sessions.events")
    val nodeToBeRead = schema.nodeForPathName("user.application.id")
    traversalPoint.canReachRelation(nodeToBeRead) should be (true)
  }

  it should "test read access in two step reference scalar tunnel" in {
    val schema = BrioSchema("unity")
    val traversalPoint = schema.nodeForPathName("user.sessions.events")
    val nodeToBeRead = schema.nodeForPathName("user.application.firstUse.cityId")
    traversalPoint.canReachRelation(nodeToBeRead) should be (true)
  }

  it should "test read access to incorrect two step reference scalar tunnel" in {
    val schema = BrioSchema("unity")
    val traversalPoint = schema.nodeForPathName("user.sessions.events")
    val nodeToBeRead = schema.nodeForPathName("user.application.firstUse")
    try {
      traversalPoint.canReachRelation(nodeToBeRead)
    } catch safely {
      case t:RuntimeException =>
        t.getMessage should include ("is not a value type")
    }
  }
  it should "test read access to incorrect provided node" in {
    val schema = BrioSchema("unity")
    val traversalPoint = schema.nodeForPathName("user.sessions.startTime")
    val nodeToBeRead = schema.nodeForPathName("user.application.firstUse.cityId")
    try {
      traversalPoint.canReachRelation(nodeToBeRead)
    } catch safely {
      case t:RuntimeException =>
        t.getMessage should include ("not a reference type")
    }
  }

}
