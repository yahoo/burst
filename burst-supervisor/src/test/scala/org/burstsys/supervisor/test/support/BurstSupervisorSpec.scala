/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.test.support

import org.burstsys
import org.burstsys.vitals.logging.VitalsLog
import org.burstsys.{fabric, tesla, vitals}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BurstSupervisorSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  VitalsLog.configureLogging("supervisor", consoleOnly = true)
  vitals.configuration.configureForUnitTests()
  tesla.configuration.configureForUnitTests()
  burstsys.fabric.wave.configuration.configureForUnitTests()

}
