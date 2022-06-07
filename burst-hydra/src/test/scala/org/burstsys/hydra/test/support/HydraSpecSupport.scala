/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.support

import org.burstsys.brio
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.felt.FeltService
import org.burstsys.hydra.parser.HydraParser
import org.burstsys.vitals.logging._
import org.burstsys.vitals.metrics.VitalsMetricsRegistry
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, Suite}

abstract class HydraSpecSupport extends AnyFlatSpec with Suite with Matchers with BeforeAndAfterAll {


  VitalsLog.configureLogging("hydra", consoleOnly = true)
  VitalsMetricsRegistry.disable()

  def parallelism: Int = 1

  val analysisName = "myAnalysis"
  val frameName = "myFrame"


  implicit val parser: HydraParser = HydraParser()
  implicit lazy val schema: BrioSchema = BrioSchema("unity")
  lazy val unitySchema: BrioSchema = schema

  override protected
  def beforeAll(): Unit = {
    log info s"Starting Felt"
    // these must be lazy so they don't start logging too soon
    brio.provider.loadBrioSchemaProviders()
    FeltService.start

  }

  override protected
  def afterAll(): Unit = {
    log info s"Stopping Felt"
    FeltService.stop
  }

  def wrap(implicit source: String): String =
    s"""|hydra myAnalysis() {
        | schema ${schema.name}
        | frame $frameName {
        |    cube user {
        |      limit = 1
        |      aggregates {
        |        a0:sum[long]
        |      }
        |      dimensions {
        |        d0:verbatim[long]
        |      }
        |    }
        |    user.sessions.events.parameters ⇒ {
        |      situ ⇒ {
        |        $source
        |      }
        |    }
        | }
        |}""".stripMargin

}
