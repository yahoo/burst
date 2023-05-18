/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.healthcheck

import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsPojo
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.configuration.burstVitalsHealthCheckPeriodDuration
import org.burstsys.vitals.logging._
import org.burstsys.vitals.time._

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

final case class VitalsSystemHealth(
                                     status: VitalsHealthStatus,
                                     message: String,
                                     components: Map[String, VitalsComponentHealth]
                                   )

trait VitalsSystemHealthService extends VitalsService {

  /**
   * @return the name of the system this healthcheck monitors
   */
  def system: String

  def systemStatus: VitalsSystemHealth

  /**
   * Include any {@link VitalsService} in the health check if they implement {@link VitalsHealthMonitoredComponent}
   *
   * @param toRegister the service(s) to register
   */
  def registerService(toRegister: VitalsService*): Unit

  /**
   * Un-register previously registered services, mainly for testing.
   *
   * @param toDeregister the service(s) to un-register
   */
  def deregisterService(toDeregister: VitalsService*): Unit

  /**
   * Include the provided components in the health check computation
   *
   * @param toRegister the service(s) to include
   */
  def registerComponent(toRegister: VitalsHealthMonitoredComponent*): Unit

  /**
   * Un-register previously registered services, mainly for testing.
   *
   * @param toDeregister the service(s) to un-register
   */
  def deregisterComponent(toDeregister: VitalsHealthMonitoredComponent*): Unit

}

object VitalsSystemHealthService {
  def apply(system: String): VitalsSystemHealthService = VitalsSystemHealthServiceContext(system: String)
}

private final case
class VitalsSystemHealthServiceContext(system: String)
  extends VitalsService with VitalsHealthStatusDelegate with VitalsSystemHealthService {

  override val serviceName: String = s"health-check-service($system)"

  override val modality: VitalsServiceModality = VitalsPojo

  ///////////////////////////////////////////////////////////
  // Private State
  ///////////////////////////////////////////////////////////

  private[this]
  val _components = new ConcurrentHashMap[String, VitalsHealthMonitoredComponent]()

  private[this]
  var _lastCheckValues = new ConcurrentHashMap[String, VitalsComponentHealth]()

  private[this]
  val _period = 30 seconds

  private[this]
  val healthStatusDelegate: VitalsHealthStatusDelegate = this

  private[this]
  lazy val parameters = s"system=$system"

  def printComponents: String = _components.asScala.keys.mkString("{'", "', '", "'}")

  override def toString: String = parameters

  ///////////////////////////////////////////////////////////
  // Lifecycle
  ///////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    lazy val tag = s"VitalsHealthCheckService.start($parameters)"
    _tender = HealthCheckTender().start
    log info s"VITALS_HEALTH_CHECK_STARTED $tag"
    markRunning
    this
  }

  override
  def stop: this.type = {
    lazy val tag = s"VitalsHealthCheckService.stop($parameters)"
    ensureRunning
    _tender.stop
    _tender = null
    _components.clear()
    _lastCheckValues.clear()
    log info s"VITALS_HEALTH_CHECK_STOPPED $tag"
    markNotRunning
    this
  }

  ///////////////////////////////////////////////////////////
  // Public API
  ///////////////////////////////////////////////////////////

  override def registerComponent(toRegister: VitalsHealthMonitoredComponent*): Unit = {
    toRegister foreach { c => if (c != null) _components.put(c.componentName, c) }
  }

  override def deregisterComponent(toUnregister: VitalsHealthMonitoredComponent*): Unit = {
    toUnregister foreach { c =>
      if (c != null) {
        log info s"VITALS_HEALTH_CHECK removing ${c.componentName}=$c"
        if (!_components.remove(c.componentName, c))
          log warn s"VITALS_HEALTH_CHECK $c not removed as ${c.componentName} is not registered under that name"
      }
    }
  }

  override def registerService(toRegister: VitalsService*): Unit = {
    toRegister foreach {
      case ms: VitalsHealthMonitoredService => _components.put(ms.componentName, ms)
      case s => if (s != null) log info s"VITALS_HEALTH_CHECK action=register ${s.serviceName} is not a health monitored service, ignoring"
    }
  }

  override def deregisterService(toUnregister: VitalsService*): Unit = {
    toUnregister foreach {
      case ms: VitalsHealthMonitoredService =>
        if (!_components.remove(ms.componentName, ms))
          log warn s"VITALS_HEALTH_CHECK action=deregister $ms not removed as ${ms.serviceName} is not registered under that name"
      case s => if (s != null) log warn s"VITALS_HEALTH_CHECK action=deregister ${s.serviceName} is not a health monitored service, ignoring"
    }
  }

  override def systemStatus: VitalsSystemHealth = {
    val overallStatus = healthStatusDelegate.overallHealth(_lastCheckValues)
    VitalsSystemHealth(overallStatus.health, overallStatus.message, _lastCheckValues.asScala.toMap)
  }

  ///////////////////////////////////////////////////////////
  // Background
  ///////////////////////////////////////////////////////////

  private[this]
  var _tender: HealthCheckTender = _

  private case
  class HealthCheckTender() extends VitalsBackgroundFunction("health-check-tender", 1 seconds, burstVitalsHealthCheckPeriodDuration, {
    val checkValues = new ConcurrentHashMap[String, VitalsComponentHealth]()
    val iter = _components.values.iterator

    //    log info s"HEALTH_CHECK_TEND components=$printComponents}"
    val start = System.nanoTime()
    val limit = (_period * .75).toNanos
    while (iter.hasNext) {
      val component = iter.next
      val componentStart = System.nanoTime()
      checkValues.put(component.componentName, component.componentHealth)
      if (component.componentHealth.health.notHealthy)
        log info s"HEALTH_CHECK_NOT_HEALTHY component=${component.componentName}} $parameters"
      val componentElapsed = System.nanoTime() - componentStart
      val componentLimit = limit / _components.size()
      // make sure the component health check isn't taking too long
      if (componentElapsed > componentLimit)
        log warn burstStdMsg(s"component '${component.componentName}' health check is slow " +
          s"(elapsed=${nsToSec(componentElapsed)}s > ${nsToSec(componentLimit)}s")
    }
    val elapsed = System.nanoTime() - start
    // make sure the entire health check isn't taking too long
    if (elapsed > limit)
      log warn burstStdMsg(s"health checks are slow (elapsed=${nsToSec(elapsed)}s > ${nsToSec(limit)}s")

    _lastCheckValues = checkValues
  })

}
