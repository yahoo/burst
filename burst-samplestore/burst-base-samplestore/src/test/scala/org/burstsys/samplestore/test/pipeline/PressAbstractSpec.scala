/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.test.pipeline

import org.burstsys.vitals.logging._
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

abstract class PressAbstractSpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  VitalsLog.configureLogging("brio", consoleOnly = true)

}
