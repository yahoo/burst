/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test.support

import org.burstsys._
import org.burstsys.brio.provider.loadBrioSchemaProviders
import org.burstsys.nexus.newNexusUid
import org.burstsys.nexus.server.NexusServer
import org.burstsys.nexus.server.NexusStreamFeeder
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplestore.api.BurstSampleStoreDataSource
import org.burstsys.samplestore.api.SampleStoreApiListener
import org.burstsys.samplestore.api.SampleStoreApiService
import org.burstsys.samplestore.api.SampleStoreDataLocus
import org.burstsys.samplestore.api.SampleStoreGenerator
import org.burstsys.samplestore.api.SampleStoreSourceNameProperty
import org.burstsys.samplestore.api.SampleStoreSourceVersionProperty
import org.burstsys.tesla.parcel
import org.burstsys.vitals.VitalsService.VitalsStandardServer
import org.burstsys.vitals.net.getPublicHostAddress
import org.burstsys.vitals.net.getPublicHostName
import org.burstsys.vitals.properties._

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.language.postfixOps

trait BurstSystemTestSpecSupport extends BurstCoreSystemTestSupport
  with NexusStreamFeeder with SampleStoreApiListener {

  final lazy
  val mockNexusServer: NexusServer = nexus.grabServer(getPublicHostAddress) fedBy this

  // override the time period to shorten the test
  vitals.configuration.burstVitalsHealthCheckPeriodMsProperty.set((5 seconds).toMillis)

  final
  var apiServer: SampleStoreApiService = _

  override protected
  def beforeAll(): Unit = {
    apiServer = SampleStoreApiService(VitalsStandardServer) talksTo this start

    super.beforeAll()
  }

  override protected
  def afterAll(): Unit = {
    apiServer.stop
    nexus.releaseServer(mockNexusServer)
    super.afterAll()
  }

  final
  val partitionProperties: VitalsPropertyMap = Map()

  /**
   * this is the data for the mock sample store server
   *
   * @return
   */
  override
  def getViewGenerator(guid: String,
                       dataSource: BurstSampleStoreDataSource): Future[SampleStoreGenerator] = {
    val promise = Promise[SampleStoreGenerator]()
    dataSource.view.storeProperties.getValueOrThrow[String](SampleStoreSourceNameProperty) should equal("mocksource")
    dataSource.view.storeProperties.getValueOrThrow[String](SampleStoreSourceVersionProperty) should equal("0.1")
    val generator =
      SampleStoreGenerator(
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
