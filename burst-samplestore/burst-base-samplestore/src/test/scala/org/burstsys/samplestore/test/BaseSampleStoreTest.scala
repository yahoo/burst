/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.test

import org.apache.logging.log4j.Logger
import org.burstsys.samplestore.test
import org.burstsys.vitals.logging.VitalsLog
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BaseSampleStoreTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  VitalsLog.configureLogging("samplestores", consoleOnly = true)

  def log: Logger = test.log

  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    super.afterAll()
  }

}
