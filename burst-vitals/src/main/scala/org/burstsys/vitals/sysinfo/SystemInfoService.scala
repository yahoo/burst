/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.sysinfo

import org.burstsys.vitals
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsSingleton}
import org.burstsys.vitals.reporter.instrument.prettyTimeFromMillis

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

object SystemInfoService extends VitalsService with SystemInfo  {
  override def modality: VitalsServiceModality = VitalsSingleton

  override def serviceName: String = {
    s"system-info-service(${SystemStatus()})"
  }

  private case class SystemStatus(
                           branch: String = vitals.git.branch,
                           commit: String = vitals.git.commitId,
                           build: String = vitals.git.buildVersion,
                           uptime: String = prettyTimeFromMillis(vitals.host.uptime),
                         )

  def systemStatus(): Object = SystemStatus()

  ///////////////////////////////////////////////////////////
  // Private State
  ///////////////////////////////////////////////////////////

  private[this]
  val _components = new ConcurrentHashMap[String, SystemInfoComponent]()

  override def registerComponent(toRegister: SystemInfoComponent*): Unit = {
    toRegister foreach { c =>
      if (c != null) {
        log info s"SystemInfoComponent ${c.name} added"
        _components.put(c.name, c)
      }
    }
  }

  override def deregisterComponent(toUnregister: SystemInfoComponent*): Unit = {
    toUnregister foreach { c =>
      if (c != null) {
        if (!_components.remove(c.name, c))
          log warn s"SystemInfoComponent ${c.name} is not registered under that name"
      }
    }
  }

  override def components: Iterator[SystemInfoComponent] = _components.values().iterator().asScala

  ///////////////////////////////////////////////////////////
  // Lifecycle
  ///////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    markRunning
    log info startedMessage
    this
  }

  override
  def stop: this.type = {
    ensureRunning
    _components.clear()
    markNotRunning
    log info stoppedMessage
    this
  }
}
