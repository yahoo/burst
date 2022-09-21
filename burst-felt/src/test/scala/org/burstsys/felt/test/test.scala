/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt

import org.apache.logging.log4j.Logger
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.vitals.logging.VitalsLog
import org.burstsys.vitals.logging.VitalsLogger
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Suite


package object test extends VitalsLogger {


  abstract class FeltAbstractSpecSupport extends AnyFlatSpec with Suite with Matchers with BeforeAndAfterAll {

    VitalsLog.configureLogging("felt", true)

    lazy val schema: BrioSchema = BrioSchema("quo")

    final
    val log: Logger = VitalsLog.getJavaLogger(getClass)

    override protected
    def beforeAll(): Unit = {
      FeltService.start
    }

    override protected
    def afterAll(): Unit = {
      FeltService.stop
    }


  }


}
