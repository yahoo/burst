/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test.alloy

import org.burstsys.fabric.wave.execution.model.result.FabricExecuteResult
import org.burstsys.fabric.wave.execution.model.result.set.FabricResultSet
import org.burstsys.json.samplestore.JsonSampleStoreContainer
import org.burstsys.json.samplestore.configuration.JsonSamplestoreDistributedConfiguration
import org.burstsys.system.test.support.BurstCoreSystemTestSupport
import org.burstsys.vitals.VitalsService.VitalsStandardServer
import org.burstsys.vitals.errors.VitalsException

import scala.language.postfixOps

trait BurstAlloySampleStoreTestSpecSupport extends BurstCoreSystemTestSupport {
  final val alloyContainer: JsonSampleStoreContainer =
    JsonSampleStoreContainer(JsonSamplestoreDistributedConfiguration(), VitalsStandardServer)

  override protected
  def beforeAll(): Unit = {
    super.beforeAll()
    // burstAgentApiTimeoutMsProperty.set((60 minutes).toMillis)
    alloyContainer.start
  }

  override protected
  def afterAll(): Unit = {
    super.afterAll()
    alloyContainer.stop
  }

  def checkResults(result: FabricExecuteResult): FabricResultSet = {
    result.resultGroup.get.resultSets.size should equal(1)

    if (!result.resultStatus.isSuccess)
      throw VitalsException(s"execution failed: ${result.resultStatus}")

    val resultGroup= result.resultGroup.get
    if (resultGroup.groupMetrics.executionMetrics.overflowed > 0)
      throw VitalsException(s"execution overflowed")
    if (resultGroup.groupMetrics.executionMetrics.limited > 0)
      throw VitalsException(s"execution limited")

    // all the besides should return a resultGroup set
    resultGroup.resultSets.keys.size should be > 0
    // resultGroup.resultSets.size should equal(1)

    resultGroup.resultSets(0)
  }

}
