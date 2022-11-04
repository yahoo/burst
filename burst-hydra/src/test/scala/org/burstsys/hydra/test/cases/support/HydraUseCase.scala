/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.support

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.model.result.set.FabricResultSet
import org.burstsys.fabric.wave.metadata.model.{FabricDomainKey, FabricViewKey}
import org.burstsys.hydra.sweep.HydraSweep
import org.burstsys.vitals.errors.VitalsException
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.language.postfixOps

abstract class HydraUseCase(val domain: FabricDomainKey, val view: FabricViewKey, val schemaName: String,
                            val executionCount: Int = 1, val parameters: String = null
                           ) extends Matchers {

  lazy val analysisName: String = this.getClass.getSimpleName.stripSuffix("$")

  lazy val frameName: String = s"myFrame"

  lazy val schema: BrioSchema = BrioSchema(schemaName)

  def expectsException: Boolean = false

  def frameSource: String = ""

  def analysisSource: String =
    s"""|hydra $analysisName() {
        |   schema $schemaName
        |$frameSource
        |}""".stripMargin

  def sweep: HydraSweep = null

  def serializeTraversal: Boolean = false

  def timeout: Duration = 1 minutes

  def validate(implicit result: FabricResultGroup): Unit

  def assertLimits(r: FabricResultSet): Unit = {
    if (r.metrics.overflowed)
      throw VitalsException(s"frame $frameName overflowed")
    if (r.metrics.limited)
      throw VitalsException(s"frame $frameName row-limited")
  }


}

