/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test.support

import org.burstsys._
import org.burstsys.nexus.newNexusUid
import org.burstsys.nexus.server.NexusServer
import org.burstsys.nexus.server.NexusStreamFeeder
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplestore.api.BurstSampleStoreDataSource
import org.burstsys.samplestore.api.SampleStoreApiServerDelegate
import org.burstsys.samplestore.api.SampleStoreDataLocus
import org.burstsys.samplestore.api.SampleStoreGeneration
import org.burstsys.samplestore.api.SampleStoreSourceNameProperty
import org.burstsys.samplestore.api.SampleStoreSourceVersionProperty
import org.burstsys.samplestore.api.server.SampleStoreApiServer
import org.burstsys.tesla.parcel
import org.burstsys.vitals.net.getPublicHostAddress
import org.burstsys.vitals.net.getPublicHostName
import org.burstsys.vitals.properties._

import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.duration._
import scala.language.postfixOps

trait BurstSystemTestSpecSupport extends BurstCoreSystemTestSupport
  with NexusStreamFeeder with SampleStoreApiServerDelegate {

  final lazy
  val mockNexusServer: NexusServer = nexus.grabServer(getPublicHostAddress) fedBy this

  // override the time period to shorten the test
  vitals.configuration.burstVitalsHealthCheckPeriodMsProperty.set((5 seconds).toMillis)

  final val apiServer: SampleStoreApiServer = SampleStoreApiServer(this)

  override protected
  def beforeAll(): Unit = {
    apiServer.start

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
