/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.test.support

import org.burstsys._
import org.burstsys.catalog.model.domain.CatalogDomain
import org.burstsys.catalog.model.view.CatalogView
import org.burstsys.fabric.configuration
import org.burstsys.fabric.configuration.burstHttpPortProperty
import org.burstsys.fabric.topology.FabricTopologyWorker
import org.burstsys.fabric.topology.supervisor.FabricTopologyListener
import org.burstsys.nexus.newNexusUid
import org.burstsys.nexus.server.{NexusServer, NexusStreamFeeder}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplestore.api.server.SampleStoreApiServer
import org.burstsys.samplestore.api.{BurstSampleStoreDataSource, SampleStoreApiServerDelegate, SampleStoreDataLocus, SampleStoreGeneration, SampleStoreSourceNameProperty, SampleStoreSourceVersionProperty}
import org.burstsys.supervisor.container.BurstWaveSupervisorContainer
import org.burstsys.tesla.parcel
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.logging.VitalsLog
import org.burstsys.vitals.net.{getPublicHostAddress, getPublicHostName}
import org.burstsys.vitals.properties.{VitalsPropertyMap, VitalsRichPropertyMap}
import org.burstsys.worker.BurstWaveWorkerContainer
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.CountDownLatch
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

trait BurstSupervisorSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll
  with SampleStoreApiServerDelegate with NexusStreamFeeder with FabricTopologyListener {

  VitalsLog.configureLogging("supervisor", consoleOnly = true)
  vitals.configuration.configureForUnitTests()
  tesla.configuration.configureForUnitTests()
  fabric.wave.configuration.configureForUnitTests()
  configuration.burstFabricSupervisorStandaloneProperty.set(true)
  configuration.burstFabricWorkerStandaloneProperty.set(true)


  final def domain: CatalogDomain = supervisorContainer.catalog.findDomainByMoniker("BurstSupervisorTestDomain").get
  final def views: Array[CatalogView] = supervisorContainer.catalog.allViewsForDomain(domain.pk).get

  final lazy
  val mockNexusServer: NexusServer = nexus.grabServer(getPublicHostAddress) fedBy this

  // override the time period to shorten the test
  vitals.configuration.burstVitalsHealthCheckPeriodMsProperty.set((5 seconds).toMillis)

  final
  val supervisorContainer: BurstWaveSupervisorContainer = fabric.wave.container.supervisorContainer.asInstanceOf[BurstWaveSupervisorContainer]

  final
  val workerContainer: BurstWaveWorkerContainer = {
    // we mix supervisor and worker in the same JVM so move the health port
    val port = burstHttpPortProperty.get
    burstHttpPortProperty.set(port + 1)
    fabric.wave.container.workerContainer.asInstanceOf[BurstWaveWorkerContainer]
  }

  final
  var apiServer: SampleStoreApiServer = _

  val workerGainGate = new CountDownLatch(1)

  override def onTopologyWorkerGain(worker: FabricTopologyWorker): Unit = {
    log info s"worker ${worker.nodeId} gain"
    workerGainGate.countDown()
  }

  override protected
  def beforeAll(): Unit = {
    org.burstsys.vitals.configuration.burstCellNameProperty.set("Cell1")
    apiServer = SampleStoreApiServer(this).start

    supervisorContainer.topology talksTo this

    supervisorContainer.containerId = 1
    workerContainer.containerId = 1

    supervisorContainer.start
    workerContainer.start

    // wait for the local worker to be available before trying anything
    workerGainGate.await()
    log info s"WORKER_FOUND_READY_TO_GO!"
  }

  override protected
  def afterAll(): Unit = {
    apiServer.stop
    nexus.releaseServer(mockNexusServer)
    supervisorContainer.stop
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
  def getViewGenerator(guid: String, dataSource: BurstSampleStoreDataSource): Future[SampleStoreGeneration] = {
    dataSource.view.storeProperties.getValueOrThrow[String](SampleStoreSourceNameProperty) should equal("mocksource")
    dataSource.view.storeProperties.getValueOrThrow[String](SampleStoreSourceVersionProperty) should equal("0.1")
    TeslaRequestFuture {
      SampleStoreGeneration(
        guid, "NO_HASH",
        Array(
          SampleStoreDataLocus(newNexusUid, getPublicHostAddress, getPublicHostName, mockNexusServer.serverPort, partitionProperties)
        ),
        dataSource.view.schemaName,
        Some(dataSource.view.viewMotif)
      )
    }
  }

  override def feedStream(stream: NexusStream): Unit = ???

  override def abortStream(_stream: NexusStream, status: parcel.TeslaParcelStatus): Unit = ???

}
