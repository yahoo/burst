/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api

import org.burstsys.samplestore.api.client.SampleStoreApiClient
import org.burstsys.samplestore.api.configuration.SampleStoreApiProperties
import org.burstsys.samplestore.api.server.SampleStoreApiServer
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsStandardClient}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import scala.concurrent.{Future, Promise}

/**
  * implementation of a Thrift Sample Store API
  */
trait SampleStoreApiService extends VitalsService with SampleStoreApiProperties {

  override val serviceName: String = "samplestore"

  def listener: SampleStoreApiListener

  /**
    * generate a sample source ''view''
    * @param guid
    * @param dataSource
    * @return
    */
  def getViewGenerator(guid: String, dataSource: BurstSampleStoreDataSource): Future[BurstSampleStoreApiViewGenerator]

  /**
    * attach a single API event listener
    * @param l
    * @return
    */
  def talksTo(l: SampleStoreApiListener): this.type


}

object SampleStoreApiService {
  def apply(): SampleStoreApiService =
    SampleStoreApiServiceContext(VitalsStandardClient)

  def apply(mode: VitalsServiceModality): SampleStoreApiService =
    SampleStoreApiServiceContext(mode: VitalsServiceModality)
}

private[samplestore] final case
class SampleStoreApiServiceContext(modality: VitalsServiceModality) extends SampleStoreApiService {

  //////////////////////////////////////////////////////////////////////////////////////
  // private state
  //////////////////////////////////////////////////////////////////////////////////////
  private[this]
  val _apiClient: SampleStoreApiClient = SampleStoreApiClient(this)

  private[this]
  val _apiServer: SampleStoreApiServer = SampleStoreApiServer(this)

  //////////////////////////////////////////////////////////////////////////////////////
  // delegation
  //////////////////////////////////////////////////////////////////////////////////////

  override
  def listener: SampleStoreApiListener = {
    ensureRunning
    _apiServer.listener
  }

  override
  def talksTo(l: SampleStoreApiListener): this.type = {
    ensureNotRunning
    _apiServer talksTo l
    this
  }

  //////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  //////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    if (modality.isServer) {
      _apiServer.start
    } else {
      _apiClient.start
    }
    markRunning
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    if (modality.isServer) {
      _apiServer.stop
    } else {
      _apiClient.stop
    }
    markNotRunning
    this
  }

  //////////////////////////////////////////////////////////////////////////////////////
  // implementation
  //////////////////////////////////////////////////////////////////////////////////////

  override
  def getViewGenerator(
                        guid: String,
                        dataSource: BurstSampleStoreDataSource
                      ): Future[BurstSampleStoreApiViewGenerator] = {
    val promise = Promise[BurstSampleStoreApiViewGenerator]()
    try {
     val result = if (modality.isServer) {
        _apiServer.getViewGenerator(
          guid = guid,
          dataSource = dataSource
        )
      } else {
        _apiClient.getViewGenerator(
          guid = guid,
          dataSource = dataSource
        )
      }
      result onSuccess {
        r => promise.success(r)
      }
      result onFailure {
        t => promise.failure(t)
      }
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        promise.failure(t)
    }
    promise.future
  }

}
