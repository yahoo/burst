/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.alloy.usecase

import java.util.concurrent.CountDownLatch

import org.burstsys.alloy.alloy.store.{AlloyJsonStoreName, AlloyView}
import org.burstsys.alloy.alloy.store.master.AlloyJsonStoreMaster
import org.burstsys.alloy.store.mini
import org.burstsys.alloy.store.mini.MiniView
import org.burstsys.alloy.store.mini.master.MiniStoreMaster
import org.burstsys.alloy.views.unity.UnityUseCaseViews
import org.burstsys.brio.provider.loadBrioSchemaProviders
import org.burstsys.fabric.container.master.MockMasterContainer
import org.burstsys.fabric.container.worker.MockWorkerContainer
import org.burstsys.fabric.data.master.store
import org.burstsys.fabric.data.worker.cache
import org.burstsys.fabric.topology.master.FabricTopologyListener
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.tesla.part.factory.TeslaFactoryBoss
import org.burstsys.vitals.configuration.burstVitalsHealthCheckPortProperty
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._
import org.burstsys.{alloy, fabric, tesla, vitals}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.language.postfixOps

abstract class AlloyJsonUseCaseRunner extends AnyFlatSpec
  with Suite with Matchers with BeforeAndAfterAll with FabricTopologyListener with AlloyUnitMetadataLookup {

  VitalsLog.configureLogging("unit", consoleOnly = true)
  vitals.configuration.configureForUnitTests()
  tesla.configuration.configureForUnitTests()
  fabric.configuration.configureForUnitTests()

  val masterContainer: MockMasterContainer = MockMasterContainer(logFile = "unit", containerId = 1)
  protected var workerContainer: MockWorkerContainer = {
    // we mix master and worker in the same JVM so move the health port
    val port = burstVitalsHealthCheckPortProperty.getOrThrow
    burstVitalsHealthCheckPortProperty.set(port + 1)
    MockWorkerContainer(logFile = "unit", containerId = 1)
  }

  val workerGainGate = new CountDownLatch(1)

  /**
   * startup services used locally to any individual unit test scenario
   */
  protected
  def localStartup(): Unit

  protected
  def localAfterStartup(): Unit = {}

  /**
   * shutdown services used locally to any individual unit test scenario
   */
  protected
  def localShutdown(): Unit

  final override protected
  def beforeAll(): Unit = {
    try {

      loadBrioSchemaProviders()

      masterContainer.metadata withLookup this
      masterContainer.topology talksTo this

      localStartup()

      masterContainer.start
      store.getMasterStore(AlloyJsonStoreName).asInstanceOf[AlloyJsonStoreMaster]
      workerContainer.start

      // wait for the local worker to be available before trying anything
      workerGainGate.await()

      localAfterStartup()
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    }
  }

  final override protected
  def afterAll(): Unit = {
    localShutdown()
    cache.instance.stop
    masterContainer.stop
    workerContainer.stop
    TeslaFactoryBoss.assertNoInUseParts()
  }

  final override def onTopologyWorkerGained(worker: FabricWorkerNode): Unit = {
    workerGainGate.countDown()
  }
}
