/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt

import org.burstsys.brio
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.vitals.logging.{VitalsLog, VitalsLogger}
import org.apache.logging.log4j.Logger
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, Suite}


package object test extends VitalsLogger {


  abstract class FeltAbstractSpecSupport extends AnyFlatSpec with Suite with Matchers with BeforeAndAfterAll {

    VitalsLog.configureLogging("felt", true)

    lazy val schema = BrioSchema("quo")

    final
    val log: Logger = VitalsLog.getJavaLogger(getClass)

    override protected
    def beforeAll(): Unit = {
      brio.provider.loadBrioSchemaProviders()
      FeltService.start
    }

    override protected
    def afterAll(): Unit = {
      FeltService.stop
    }


  }


}
