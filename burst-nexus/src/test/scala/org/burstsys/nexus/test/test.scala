/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus

import org.burstsys.vitals.logging._
import org.burstsys.{brio, tesla, vitals}
import org.burstsys.vitals.properties.VitalsPropertyRegistry
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

package object test extends VitalsLogger {

  trait NexusSpec extends AnyFlatSpec with Matchers {
    VitalsLog.configureLogging("nexus", consoleOnly = true)
    VitalsPropertyRegistry.logReport
    brio.provider.loadBrioSchemaProviders()

    vitals.configuration.configureForUnitTests()
    tesla.configuration.configureForUnitTests()

  }


}
