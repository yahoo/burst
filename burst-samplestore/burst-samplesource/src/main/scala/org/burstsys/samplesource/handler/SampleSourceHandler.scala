/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.handler

import org.burstsys.samplesource.SampleSourceId
import org.burstsys.samplesource.service.SampleSourceService
import org.burstsys.vitals.VitalsService.VitalsSingleton
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging._
import org.burstsys.vitals.{VitalsService, reflection}

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

object SampleSourceHandler extends VitalsService {

  final override def serviceName: String = s"sample-source-handler"

  final override def modality: VitalsService.VitalsServiceModality = VitalsSingleton

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _handlerNameMap = new ConcurrentHashMap[SampleSourceId, SampleSourceService]

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def sampleSourceHandler(id: SampleSourceId): SampleSourceService = {
    ensureRunning
    _handlerNameMap.get(id) match {
      case null =>
        val tag = s"No sample source handler found for $id, valid ids are ${_handlerNameMap.keys.asScala.mkString("(",",",")")}"
        log warn burstStdMsg(tag)
        throw VitalsException(tag)
      case handler =>
        log debug burstStdMsg(s"Found handler $handler")
        handler
    }
  }

  final
  def handlerMap: ConcurrentHashMap[SampleSourceId, SampleSourceService] = {
    startIfNotAlreadyStarted
    _handlerNameMap
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    log info s"$serviceName scanning for handler(s)"
    _handlerNameMap.clear()
    scanForSampleSources()
    markRunning
    this
  }


  final override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    for (ss <- _handlerNameMap.values.asScala) ss.stop
    _handlerNameMap.clear()
    markNotRunning
    this
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // Internals
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private def scanForSampleSources(): Unit = {
    // use reflection to find sample source implementations
    val scannedClasses = reflection.getSubTypesOf(classOf[SampleSourceService])
    log info burstStdMsg(f"found ${scannedClasses.size}%,d sources(s)")
    scannedClasses.foreach {
      klass =>
        val i = klass.getDeclaredConstructor().newInstance()
        log info burstStdMsg(s"loading handler for schema '${i.id}'")

        if (_handlerNameMap.containsKey(i.id)) {
          VitalsException(s"duplicate handler name '${i.id}' in connector '${i.getClass.getName}'")
        } else {
          i.start
          _handlerNameMap.put(i.id, i)
        }
    }
  }

}
