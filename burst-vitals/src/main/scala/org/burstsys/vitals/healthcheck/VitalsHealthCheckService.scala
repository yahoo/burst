/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.healthcheck

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import org.burstsys.vitals
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsPojo
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.configuration.burstVitalsHealthCheckPathsProperty
import org.burstsys.vitals.configuration.burstVitalsHealthCheckPeriodDuration
import org.burstsys.vitals.configuration.burstVitalsHealthCheckPortProperty
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.time._

import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

trait VitalsHealthCheckService extends VitalsService {

  /**
   * TODO
   *
   * @return
   */
  def system: String

  /**
   * TODO
   *
   * @param toRegister
   */
  def registerService(toRegister: VitalsService*): Unit

  /**
   * Mainly for testing
   */
  def deregisterService(toDeregister: VitalsService*): Unit

  /**
   * TODO
   *
   * @param toRegister
   */
  def registerComponent(toRegister: VitalsHealthMonitoredComponent*): Unit

  /**
   * Mainly for testing
   */
  def deregisterComponent(toDeregister: VitalsHealthMonitoredComponent*): Unit

  /**
   * The public HTTP listen port
   *
   * @return
   */
  def healthCheckPort: Int

  /**
   * The public HTTP listen port
   *
   * @param port
   */
  def healthCheckPort_=(port: Int): Unit

}

object VitalsHealthCheckService {
  def apply(system: String): VitalsHealthCheckService = VitalsHealthCheckServiceContext(system: String)
}

private final case
class VitalsHealthCheckServiceContext(system: String) extends VitalsService
  with HttpHandler with VitalsHealthStatusDelegate with VitalsHealthCheckService {

  override val serviceName: String = s"health-check-service($system)"

  override val modality: VitalsServiceModality = VitalsPojo

  ///////////////////////////////////////////////////////////
  // Private State
  ///////////////////////////////////////////////////////////

  private[this]
  var _healthCheckPort: Option[Int] = burstVitalsHealthCheckPortProperty.asOption.orElse(Some(0))

  private[this] final
  val TCP_BACKLOG = 0 // use system default

  private[this]
  var _server: HttpServer = _

  private[this]
  val _components = new ConcurrentHashMap[String, VitalsHealthMonitoredComponent]()

  private[this]
  var _lastCheckValues = new ConcurrentHashMap[String, VitalsComponentHealth]()

  private[this]
  val _period = 30 seconds

  private[this]
  val healthStatusDelegate: VitalsHealthStatusDelegate = this

  private[this]
  var _netAddress: InetSocketAddress = _

  private[this]
  lazy val _paths: Array[String] = burstVitalsHealthCheckPathsProperty.get.split(",")

  private[this]
  lazy val parameters = s"system=$system, binding=${_netAddress}, paths=${_paths.mkString("{", ", ", "}")}"

  def printComponents: String = _components.asScala.keys.mkString("{'", "', '", "'}")

  override def toString: String = parameters

  ///////////////////////////////////////////////////////////
  // Lifecycle
  ///////////////////////////////////////////////////////////

  override
  def start: this.type = {
    ensureNotRunning
    _netAddress = new InetSocketAddress(_healthCheckPort.get)
    lazy val tag = s"VitalsHealthCheckService.start($parameters)"
    try {
      log info s"VITALS_HEALTH_CHECK_STARTING $tag"
      _server = HttpServer.create()
      _paths foreach { path => _server.createContext(path, this) }
      _server.bind(_netAddress, TCP_BACKLOG)
      _server.setExecutor(executor)
      _server.start()
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(s"VITALS_HEALTH_CHECK_FAIL unable to bind to health check path $t $tag", t)
    }
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
    _server.stop(0)
    _server = null
    _components.clear()
    _lastCheckValues.clear()
    log info s"VITALS_HEALTH_CHECK_STOPPED $tag"
    markNotRunning
    this
  }

  ///////////////////////////////////////////////////////////
  // Public API
  ///////////////////////////////////////////////////////////

  override
  def healthCheckPort: Int = _healthCheckPort.getOrElse(throw VitalsException(s"VITALS_HEALTH_CHECK_PORT_NOT_SET!"))

  override
  def healthCheckPort_=(port: Int): Unit = {
    _healthCheckPort = Some(port)
  }

  override
  def registerComponent(toRegister: VitalsHealthMonitoredComponent*): Unit = {
    toRegister foreach { c => if (c != null) _components.put(c.componentName, c) }
  }

  override
  def deregisterComponent(toUnregister: VitalsHealthMonitoredComponent*): Unit = {
    toUnregister foreach { c =>
      if (c != null) {
        log info s"VITALS_HEALTH_CHECK removing ${c.componentName}=$c"
        if (!_components.remove(c.componentName, c))
          log warn s"VITALS_HEALTH_CHECK $c not removed as ${c.componentName} is not registered under that name"
      }
    }
  }

  override
  def registerService(toRegister: VitalsService*): Unit = {
    toRegister foreach {
      case ms: VitalsHealthMonitoredService => _components.put(ms.componentName, ms)
      case s => if (s != null) log info s"VITALS_HEALTH_CHECK action=register ${s.serviceName} is not a health monitored service, ignoring"
    }
  }

  override
  def deregisterService(toUnregister: VitalsService*): Unit = {
    toUnregister foreach {
      case ms: VitalsHealthMonitoredService =>
        if (!_components.remove(ms.componentName, ms))
          log warn s"VITALS_HEALTH_CHECK action=deregister $ms not removed as ${ms.serviceName} is not registered under that name"
      case s => if (s != null) log warn s"VITALS_HEALTH_CHECK action=deregister ${s.serviceName} is not a health monitored service, ignoring"
    }
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
      if (component.componentHealth.status.notHealthy)
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

  ///////////////////////////////////////////////////////////
  // Http handler
  ///////////////////////////////////////////////////////////

  private val mapper = vitals.json.buildJsonMapper

  override def handle(httpExchange: HttpExchange): Unit = {
    httpExchange.getRequestMethod.toUpperCase match {
      case "GET" =>
        val path = httpExchange.getRequestURI.getPath

        val overallStatus = healthStatusDelegate.overallHealth(_lastCheckValues)
        var response = Map[String, Any](
          ("system", system),
          ("health", overallStatus.status),
          ("message", overallStatus.message),
        )
        val iter = _lastCheckValues.entrySet.iterator
        while (iter.hasNext) {
          val entry = iter.next
          response += (entry.getKey -> entry.getValue.asJson)
        }
        val statusCode = overallStatus.status.statusCode
        val body = mapper.writeValueAsString(response)
        log info s"responding to request on $path: $statusCode - $body"

        val bytes = body.getBytes("UTF-8")
        httpExchange.getResponseHeaders.add("Content-Type", "application/json")
        httpExchange.sendResponseHeaders(statusCode, bytes.length)
        httpExchange.getResponseBody.write(bytes)

      case _ =>
        httpExchange.sendResponseHeaders(405, -1)
    }
    httpExchange.close()
  }
}
