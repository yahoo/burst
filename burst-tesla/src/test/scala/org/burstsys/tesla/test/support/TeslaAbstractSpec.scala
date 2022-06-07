/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.test.support

import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
  * Mix in things that make unit testing work
  */
trait TeslaAbstractSpec extends AnyFlatSpec with Matchers with BeforeAndAfterEach with TeslaSpecLog {

}
