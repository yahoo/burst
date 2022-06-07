/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test

import org.burstsys.vitals.logging._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait VitalsAbstractSpec extends AnyFlatSpec with Matchers {

  VitalsLog.configureLogging("vitals", consoleOnly = true)

}
