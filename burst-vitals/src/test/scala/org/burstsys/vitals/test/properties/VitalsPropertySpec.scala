/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test.properties

import org.burstsys.vitals.properties._
import org.burstsys.vitals.test.VitalsAbstractSpec

import scala.concurrent.duration._
import scala.language.postfixOps

class VitalsPropertySpec extends VitalsAbstractSpec {


  "VitalsPropertyRegistry" should "print all properties" in {
    VitalsPropertyRegistry.logReport
  }

  it should "export/import durations 1" in {
    System.setProperty(mockDurationProperty2.key, (5 minutes).toString)
    val duration = mockDurationProperty2.getOrThrow
    duration should equal (5 minutes)
  }

  it should "export/import durations 2" in {
    VitalsPropertyRegistry.importProperties(Map[String, String]("freddy" -> (10 minutes).toString))
    mockDurationProperty.getOrThrow should equal (10 minutes)
    System.setProperty("freddy", (10 minutes).toString)
    System.getProperty("freddy") should equal("10 minutes")
    Duration(System.getProperty("freddy")) should equal (10 minutes)
  }

  "property" should "handle boolean empty strings" in {
    property("foo", true) should equal(true)
    property("foo", false) should equal(false)
    System.setProperty("foo", "")
    property("foo", false) should equal(false)
    property("foo", true) should equal(true)

    property("bar", false) should equal(false)
    property("bar", true) should equal(true)

    System.setProperty("boof", true.toString)
    property("boof", false) should equal(true)
    property("boof", true) should equal(true)
  }

  it should "handle Double empty strings" in {
    property("foo1", 1.0) should equal(1.0)
    property("foo1", 0.0) should equal(0.0)
    System.setProperty("foo1", 1.0.toString)
    property("foo1", 1.0) should equal(1.0)
    property("foo1", 0.0) should equal(1.0)

    property("bar1", 1.0) should equal(1.0)
    property("bar1", 0.0) should equal(0.0)

    System.setProperty("boof1", 1.0.toString)
    property("boof1", 1.0) should equal(1.0)
    property("boof1", 0.0) should equal(1.0)
  }

  it should "handle Long empty strings" in {
    property("foo2", 1L) should equal(1L)
    property("foo2", 0L) should equal(0L)
    System.setProperty("foo2", 1L.toString)
    property("foo2", 1L) should equal(1L)
    property("foo2", 0L) should equal(1L)

    property("bar2", 1L) should equal(1L)
    property("bar2", 0L) should equal(0L)

    System.setProperty("boof2", 1L.toString)
    property("boof2", 1L) should equal(1L)
    property("boof2", 0L) should equal(1L)
  }

  it should "handle string empty strings" in {
    property("foo3", "hello") should equal("hello")
    property("foo3", "goodbye") should equal("goodbye")
    System.setProperty("foo3", "hello".toString)
    property("foo3", "hello") should equal("hello")
    property("foo3", "goodbye") should equal("hello")

    property("bar3", "hello") should equal("hello")
    property("bar3", "goodbye") should equal("goodbye")

    System.setProperty("boof3", "hello".toString)
    property("boof3", "hello") should equal("hello")
    property("boof3", "goodbye") should equal("hello")
  }

}
