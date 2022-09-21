/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.master.test.support

import org.burstsys._
import org.burstsys.catalog.model.domain.CatalogDomain
import org.burstsys.catalog.model.view.CatalogView
import org.burstsys.fabric.configuration
import org.burstsys.fabric.topology.master.FabricTopologyListener
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.master.server.container.BurstMasterContainer
import org.burstsys.nexus.newNexusUid
import org.burstsys.nexus.server.NexusServer
import org.burstsys.nexus.server.NexusStreamFeeder
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplestore.api.BurstSampleStoreDataSource
import org.burstsys.samplestore.api.SampleStoreApiListener
import org.burstsys.samplestore.api.SampleStoreApiService
import org.burstsys.samplestore.api.SampleStoreDataLocus
import org.burstsys.samplestore.api.SampleStoreGeneration
import org.burstsys.samplestore.api.SampleStoreSourceNameProperty
import org.burstsys.samplestore.api.SampleStoreSourceVersionProperty
import org.burstsys.tesla.parcel
import org.burstsys.vitals.VitalsService.VitalsStandardServer
import org.burstsys.vitals.git
import org.burstsys.vitals.logging._
import org.burstsys.vitals.metrics.VitalsMetricsRegistry
import org.burstsys.vitals.net.getPublicHostAddress
import org.burstsys.vitals.net.getPublicHostName
import org.burstsys.vitals.properties.VitalsPropertyMap
import org.burstsys.vitals.properties._
import org.burstsys.worker.BurstWorkerContainer
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.CountDownLatch
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.duration._
import scala.language.postfixOps

trait BurstMasterSpecSupport extends AnyFlatSpec with Matchers with BeforeAndAfterAll
  with SampleStoreApiListener with NexusStreamFeeder with FabricTopologyListener {
  VitalsLog.configureLogging("master", consoleOnly = true)

  final def domain: CatalogDomain = masterContainer.catalog.findDomainByMoniker("BurstMasterTestDomain").get
  final def views: Array[CatalogView] = masterContainer.catalog.allViewsForDomain(domain.pk).get

  VitalsMetricsRegistry.disable()

  vitals.configuration.configureForUnitTests()
  tesla.configuration.configureForUnitTests()
  fabric.configuration.configureForUnitTests()
  configuration.burstFabricMasterStandaloneProperty.set(true)
  configuration.burstFabricWorkerStandaloneProperty.set(true)
  git.turnOffBuildValidation()

  final lazy
  val mockNexusServer: NexusServer = nexus.grabServer(getPublicHostAddress) fedBy this

  // override the time period to shorten the test
  vitals.configuration.burstVitalsHealthCheckPeriodMsProperty.set((5 seconds).toMillis)

  final
  val masterContainer: BurstMasterContainer = fabric.container.masterContainer.asInstanceOf[BurstMasterContainer]

  final
  val workerContainer: BurstWorkerContainer = {
    // we mix master and worker in the same JVM so move the health port
    val port = vitals.configuration.burstVitalsHealthCheckPortProperty.getOrThrow
    vitals.configuration.burstVitalsHealthCheckPortProperty.set(port + 1)
    fabric.container.workerContainer.asInstanceOf[BurstWorkerContainer]
  }

  final
  var apiServer: SampleStoreApiService = _

  val workerGainGate = new CountDownLatch(1)

  override def onTopologyWorkerGain(worker: FabricWorkerNode): Unit = {
    log info s"worker ${worker.nodeId} gain"
    workerGainGate.countDown()
  }

  override protected
  def beforeAll(): Unit = {
    org.burstsys.vitals.configuration.burstCellNameProperty.set("Cell1")
    apiServer = SampleStoreApiService(VitalsStandardServer) talksTo this start

    masterContainer.topology talksTo this

    masterContainer.containerId = 1
    workerContainer.containerId = 1

    masterContainer.start
    workerContainer.start

    // wait for the local worker to be available before trying anything
    workerGainGate.await()
    log info s"WORKER_FOUND_READY_TO_GO!"
  }

  override protected
  def afterAll(): Unit = {
    apiServer.stop
    nexus.releaseServer(mockNexusServer)
    masterContainer.stop
    workerContainer.stop
  }

  final
  val partitionProperties: VitalsPropertyMap = Map()

  /**
   * this is the data for the mock sample store server
   *
   * @param guid
   * @param dataSource
   * @return
   */
  override
  def getViewGenerator(guid: String,
                       dataSource: BurstSampleStoreDataSource): Future[SampleStoreGeneration] = {
    val promise = Promise[SampleStoreGeneration]()
    dataSource.view.storeProperties.getValueOrThrow[String](SampleStoreSourceNameProperty) should equal("mocksource")
    dataSource.view.storeProperties.getValueOrThrow[String](SampleStoreSourceVersionProperty) should equal("0.1")
    val generator =
      SampleStoreGeneration(
        guid,
        "NO_HASH",
        Array(
          SampleStoreDataLocus(
            newNexusUid,
            getPublicHostAddress, getPublicHostName, mockNexusServer.serverPort, partitionProperties
          )
        ),
        dataSource.view.schemaName,
        Some(dataSource.view.viewMotif)
      )
    promise.success(generator)
    promise.future
  }

  override def feedStream(stream: NexusStream): Unit = ???

  override def abortStream(_stream: NexusStream, status: parcel.TeslaParcelStatus): Unit = ???

}
