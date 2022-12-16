/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test.synthetic

import org.burstsys.fabric.configuration.burstHttpPortProperty
import org.burstsys.fabric.net.FabricNetworkConfig
import org.burstsys.fabric.wave.execution.model.result.FabricExecuteResult
import org.burstsys.fabric.wave.execution.model.result.set.FabricResultSet
import org.burstsys.samplestore.store.container.supervisor.{SampleStoreFabricSupervisorContainer, SampleStoreFabricSupervisorContainerContext}
import org.burstsys.samplestore.store.container.worker.{SampleStoreFabricWorkerContainer, SampleStoreFabricWorkerContainerContext}
import org.burstsys.system.test.support.{BurstCoreSystemTestSupport, TopologyWatcher}
import org.burstsys.vitals.errors.VitalsException

import java.util.concurrent.TimeUnit
import scala.language.postfixOps

trait SyntheticSampleStoreTestSpecSupport
  extends BurstCoreSystemTestSupport {
  protected val testFabricNetworkServerConfig: FabricNetworkConfig = FabricNetworkConfig(netSupervisorPort = 33061)

  protected var syntheticSupervisorContainer: SampleStoreFabricSupervisorContainer = {
    new SampleStoreFabricSupervisorContainerContext(testFabricNetworkServerConfig)
  }

  protected var syntheticWorkerContainer: SampleStoreFabricWorkerContainer = {
    // we mix supervisor and worker in the same JVM so move the health port
    val port = burstHttpPortProperty.get
    burstHttpPortProperty.set(port + 1)
    new SampleStoreFabricWorkerContainerContext(testFabricNetworkServerConfig)
  }

  val syntheticTopoWatcher: TopologyWatcher = TopologyWatcher()

  override protected
  def beforeAll(): Unit = {
    super.beforeAll()
    // burstAgentApiTimeoutMsProperty.set((60 minutes).toMillis)
    syntheticSupervisorContainer.topology.talksTo(syntheticTopoWatcher)
    syntheticSupervisorContainer.start
    syntheticWorkerContainer.start
    syntheticTopoWatcher.workerGainGate.await(30, TimeUnit.SECONDS)
  }

  override protected
  def afterAll(): Unit = {
    super.afterAll()
    syntheticSupervisorContainer.stop
    syntheticWorkerContainer.stop
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
