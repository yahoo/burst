/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test

import org.burstsys.vitals.logging.VitalsLog
import org.burstsys.vitals.properties.VitalsPropertyRegistry
import org.burstsys.{tesla, vitals}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

trait NexusSpec extends AnyFlatSpec with Matchers with BeforeAndAfterEach with BeforeAndAfterAll {
  protected val beginMarker = "vvvvvvvvvvvvvvv"
  protected val endMarker = "^^^^^^^^^^^^^^^"

  VitalsLog.configureLogging("nexus", consoleOnly = true)
  VitalsPropertyRegistry.logReport
  vitals.configuration.configureForUnitTests()
  tesla.configuration.configureForUnitTests()
}
