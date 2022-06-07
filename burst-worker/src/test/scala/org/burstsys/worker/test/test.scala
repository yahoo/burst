/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.worker

import org.burstsys.vitals.git
import org.burstsys.vitals.logging.{VitalsLog, VitalsLogger}
import org.burstsys.vitals.properties.VitalsPropertyRegistry
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

package object test extends VitalsLogger {


  trait BurstWorkerSpecSupport extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

    VitalsLog.configureLogging("worker", true)
    git.turnOffBuildValidation()
    VitalsPropertyRegistry.logReport


    override protected
    def beforeAll(): Unit = {
    }

    override protected
    def afterAll(): Unit = {
    }


  }

}
