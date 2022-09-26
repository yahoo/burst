/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.handler

import org.burstsys.samplesource.service.SampleSourceSupervisorService
import org.burstsys.samplesource.service.SampleSourceService
import org.burstsys.samplesource.service.SampleSourceWorkerService
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsSingleton
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reflection

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

object SampleSourceHandlerRegistry extends VitalsService {

  override def serviceName: String = s"sample-source-handler"

  override def modality: VitalsService.VitalsServiceModality = VitalsSingleton

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _services = new ConcurrentHashMap[String, SampleSourceService[_, _]]()

  private[this]
  val _workers = new ConcurrentHashMap[String, SampleSourceWorkerService]()

  private[this]
  val _supervisors = new ConcurrentHashMap[String, SampleSourceSupervisorService]()

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  def getSupervisor(name: String): SampleSourceSupervisorService = {
    ensureRunning
    _supervisors.computeIfAbsent(name,
      _ => _services.get(name).coordinatorClass.getDeclaredConstructor().newInstance().asInstanceOf[SampleSourceSupervisorService].start)
  }

  def getWorker(name: String): SampleSourceWorkerService = {
    ensureRunning
    _workers.computeIfAbsent(name,
      _ => _services.get(name).workerClass.getDeclaredConstructor().newInstance().asInstanceOf[SampleSourceWorkerService].start)
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override def start: this.type = {
    ensureNotRunning
    log info startingMessage
    log info s"$serviceName scanning for handler(s)"
    _services.clear()
    _supervisors.clear()
    _workers.clear()
    scanForSampleSources()
    markRunning
    this
  }


  override def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    for (m <- _supervisors.values.asScala) m.stop
    for (w <- _workers.values.asScala) w.stop
    _services.clear()
    _supervisors.clear()
    _workers.clear()
    markNotRunning
    this
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // Internals
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private def scanForSampleSources(): Unit = {
    // use reflection to find sample source implementations
    val scannedClasses = reflection.getSubTypesOf(classOf[SampleSourceService[_, _]])
    log info burstStdMsg(f"found ${scannedClasses.size}%,d sources(s)")
    scannedClasses.foreach { klass =>
      val i = klass.getDeclaredConstructor().newInstance()
      log info burstStdMsg(s"loading sample source '${i.name}'")

      if (_services.containsKey(i.name)) {
        VitalsException(s"duplicate handler name '${i.name}' in connector '${i.getClass.getName}'")
      } else {
        _services.put(i.name, i)
      }
    }
  }

}
