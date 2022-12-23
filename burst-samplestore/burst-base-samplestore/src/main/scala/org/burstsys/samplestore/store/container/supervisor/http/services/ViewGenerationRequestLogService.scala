/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.container.supervisor.http.services

import org.burstsys.samplestore.api.BurstSampleStoreApiViewGenerator
import org.burstsys.samplestore.api.BurstSampleStoreDataSource
import org.burstsys.samplestore.api.SampleStoreApiListener
import org.burstsys.samplestore.configuration.sampleStoreViewRequestLogSize
import org.jctools.queues.MpmcArrayQueue

import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

trait ViewGenerationRequestLog {

  case class ViewGenerationRequest(
                                    guid: String,
                                    datasource: BurstSampleStoreDataSource,
                                    now: Date = new Date(),
                                    var response: Option[BurstSampleStoreApiViewGenerator] = None
                                  )

  def requests: Array[ViewGenerationRequest]

  def responseFor(guid: String): Option[ViewGenerationRequest]
}

class ViewGenerationRequestLogService() extends ViewGenerationRequestLog with SampleStoreApiListener {

  private val _requests = new MpmcArrayQueue[ViewGenerationRequest](sampleStoreViewRequestLogSize.get)

  private val _responses = new ConcurrentHashMap[String, ViewGenerationRequest]()

  override def requests: Array[ViewGenerationRequest] = _requests.asScala.toArray

  override def responseFor(guid: String): Option[ViewGenerationRequest] = Option(_responses.get(guid))

  override def onViewGenerationRequest(guid: String, dataSource: BurstSampleStoreDataSource): Unit = {
    val req = ViewGenerationRequest(guid, dataSource)

    while (!_requests.offer(req)) {
      // while we are unable to put this request on the queue, pull the first one out
      val removed = _requests.poll()
      _responses.remove(removed.guid)
    }
    _responses.put(guid, req)
  }

  override def onViewGeneration(guid: String, dataSource: BurstSampleStoreDataSource, result: BurstSampleStoreApiViewGenerator): Unit = {
    if (_responses.containsKey(guid)) {
      _responses.get(guid).response = Some(result)
    }
  }
}
