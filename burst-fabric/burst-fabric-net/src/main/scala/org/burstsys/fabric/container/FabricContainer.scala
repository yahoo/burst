/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container

import org.burstsys.fabric.configuration
import org.burstsys.fabric.container.http.endpoints.{FabricHttpHealthCheckEndpoint, FabricHttpSystemInfoEndpoint}
import org.burstsys.fabric.container.http.{FabricHttpBinder, FabricHttpResourceConfig, FabricHttpSSL, FabricWebSocketService}
import org.burstsys.tesla.part.factory.TeslaFactoryBoss
import org.burstsys.vitals.VitalsService.{VitalsContainer, VitalsServiceModality}
import org.burstsys.vitals.configuration.burstCellNameProperty
import org.burstsys.vitals.errors._
import org.burstsys.vitals.healthcheck.VitalsSystemHealthService
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.{VitalsHostName, VitalsHostPort}
import org.burstsys.vitals.properties.VitalsPropertyRegistry
import org.burstsys.vitals.sysinfo.{SystemInfo, SystemInfoService}
import org.burstsys.vitals.{VitalsService, git, reporter}
import org.glassfish.grizzly.http.server.{HttpServer, NetworkListener}
import org.glassfish.grizzly.ssl.SSLEngineConfigurator
import org.glassfish.grizzly.websockets.WebSocketAddOn
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.glassfish.jersey.server.ResourceConfig

import java.net.URI
import scala.jdk.CollectionConverters.CollectionHasAsScala

/**
 * A top level JVM singleton representing the Burst node process - supervisor or worker.
 * All of the functionality in common across both.
 */
trait FabricContainer extends VitalsService {

  /**
   * @return unique key for each container
   */
  def containerId: Option[FabricContainerId]

  final def containerIdGetOrThrow: FabricContainerId = containerId.getOrElse(throw VitalsException(s"no container id"))

  def containerId_=(id: FabricContainerId): Unit

  /**
   * The modality that this container had at boot
   */
  def bootModality: VitalsServiceModality

  /**
   * the health check service
   */
  def health: VitalsSystemHealthService

  def httpPort: VitalsHostPort

  def httpPort_=(port: VitalsHostPort): Unit

  def httpConfig: ResourceConfig

  def httpBinder: AbstractBinder

  def httpResources: Array[Class[_]]

  def webSocketService: FabricWebSocketService

  /**
   * suspend main thread into background until container is finished
   */
  final def run: this.type = {
    Thread.currentThread.join()
    this
  }

  def log4JPropertiesFileName: String

}

abstract class FabricContainerContext extends FabricContainer with FabricHttpSSL {

  final override val modality: VitalsServiceModality = VitalsContainer

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final private[this]
  var _containerId: Option[FabricContainerId] = None

  final private[this]
  val _healthCheck: VitalsSystemHealthService = VitalsSystemHealthService(serviceName)

  private var _port: Int = configuration.burstHttpPortProperty.get

  private var _server: HttpServer = _

  private var _webSocketService: FabricWebSocketService = _

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override def containerId: Option[FabricContainerId] = _containerId

  final override def containerId_=(id: FabricContainerId): Unit = _containerId = Some(id)

  final override def health: VitalsSystemHealthService = _healthCheck

  override def httpPort: VitalsHostPort = _port

  override def httpPort_=(port: VitalsHostPort): Unit = _port = port

  override def httpConfig: ResourceConfig = new FabricHttpResourceConfig(this)

  override def httpBinder: AbstractBinder = new FabricHttpBinder(this)

  override def httpResources: Array[Class[_]] = Array(
    classOf[FabricHttpHealthCheckEndpoint],
    classOf[FabricHttpSystemInfoEndpoint],
  )

  override def webSocketService: FabricWebSocketService = _webSocketService

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def start: this.type = {
    try {
      synchronized {
        ensureNotRunning
        _healthCheck.start
        SystemInfoService.startIfNotAlreadyStarted

        VitalsLog.configureLogging(log4JPropertiesFileName)
        VitalsPropertyRegistry.logReport

        log info s"FABRIC_CELL_NAME: '${burstCellNameProperty.get}'"
        log info s"FABRIC_GIT_BRANCH: '${git.branch}'   FABRIC_GIT_COMMIT: '${git.commitId}'"
        TeslaFactoryBoss.startIfNotAlreadyStarted

        var webServerStartTries = 10
        while (webServerStartTries > 0) {
          try {
            startWebServer()
            webServerStartTries = 0
          } catch safely {
            case b: java.net.BindException =>
              log warn burstStdMsg(s"waiting ($webServerStartTries) to bind to web server port ${configuration.burstHttpPortProperty.asOption}")
              webServerStartTries = webServerStartTries - 1
              if (webServerStartTries > 0)
                Thread.sleep(100)
              else {
                log error burstStdMsg(s"Unable to bind web server port ${configuration.burstHttpPortProperty.asOption}")
                throw b
              }
            case e =>
              throw e
          }

        }

        reporter.startReporterSystem()

        markRunning
      }
      log info s"FABRIC_CONTAINER_BOOT containerId=$containerId"
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    }
    this
  }

  private def startWebServer(): Unit = {
    val useHttps: Boolean = configuration.burstUseHttpsProperty.get
    val hostname: VitalsHostName = configuration.burstHttpHostProperty.get
    val scheme: String = if (useHttps) "https" else "http"
    val uri = new URI(s"$scheme://$hostname:$httpPort/")

    val application = httpConfig
    val sslConfig = new SSLEngineConfigurator(httpSSLContext).setClientMode(false)
    log info s"Starting Grizzly server on $uri"
    log info s"Configured TLS protocols:     ${Option(sslConfig.getEnabledProtocols).map(_.mkString("Array(", ", ", ")")).getOrElse("None")}"
    log info s"Configured TLS cipher suites: ${Option(sslConfig.getEnabledCipherSuites).map(_.mkString("Array(", ", ", ")")).getOrElse("None")}"
    _server = GrizzlyHttpServerFactory.createHttpServer(uri, application, useHttps, sslConfig, false)
    val websockets: WebSocketAddOn = new WebSocketAddOn()
    for (nl: NetworkListener <- _server.getListeners.asScala) {
      nl.registerAddOn(websockets)
    }
    _server.start()
    _webSocketService = FabricWebSocketService().start
  }

  override def stop: this.type = {
    synchronized {
      ensureRunning
      TeslaFactoryBoss.stopIfNotAlreadyStopped
      _healthCheck.stop
      SystemInfoService.stopIfNotAlreadyStopped
      _webSocketService.stop
      _server.shutdownNow()
      markNotRunning
    }
    this
  }

}
