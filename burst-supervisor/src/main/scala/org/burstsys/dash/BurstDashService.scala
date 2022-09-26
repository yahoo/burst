/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash

import org.burstsys.agent.AgentService
import org.burstsys.catalog.CatalogService
import org.burstsys.dash.application.websocket.BurstWebSocketService
import org.burstsys.dash.application.BurstDashApplication
import org.burstsys.dash.application.BurstDashSsl
import org.burstsys.dash.application._
import org.burstsys.dash.provider.torcher.BurstDashTorcherService
import org.burstsys.dash.service.profiler.BurstRestProfiler
import org.burstsys.dash.service.thrift
import org.burstsys.dash.websocket.BurstRestThriftRelay
import org.burstsys.dash.websocket.BurstRestExecutionRelay
import org.burstsys.dash.websocket.BurstRestFabricTopologyRelay
import org.burstsys.dash.websocket.BurstRestProfilerRelay
import org.burstsys.dash.websocket.BurstRestTorcherRelay
import org.burstsys.fabric
import org.burstsys.fabric.container.supervisor.FabricSupervisorContainer
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsServiceModality
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.net.VitalsHostPort
import org.burstsys.vitals.net.VitalsUrl
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.http.server.NetworkListener
import org.glassfish.grizzly.ssl.SSLEngineConfigurator
import org.glassfish.grizzly.websockets.WebSocketAddOn
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory

import jakarta.ws.rs.core.UriBuilder
import scala.jdk.CollectionConverters._

/**
 * The REST ui/api service
 */
trait BurstDashService extends VitalsService {

  /**
   * hostname for the REST api
   */
  final def hostname: VitalsHostName = configuration.burstRestHostProperty.getOrThrow

  /**
   * port for the REST api
   */
  final def port: VitalsHostPort = configuration.burstRestPortProperty.getOrThrow

  /**
   * if the webservice should use https
   */
  final def useHttps: Boolean = configuration.burstRestUsesHttpsProperty.getOrThrow

  final def scheme: String = if (useHttps) "https" else "http"
  /**
   * URL for the REST api
   */
  final def url: VitalsUrl = s"$scheme://$hostname:$port/"

}

object BurstDashService {

  def apply(
             modality: VitalsServiceModality,
             agent: AgentService,
             catalog: CatalogService,
             supervisor: FabricSupervisorContainer,
             torcher: BurstDashTorcherService
           ): BurstDashService = RestServiceContext(modality, agent, catalog, supervisor, torcher)

}

private final case
class RestServiceContext(
                          modality: VitalsServiceModality,
                          agent: AgentService,
                          catalog: CatalogService,
                          supervisor: FabricSupervisorContainer,
                          torcher: BurstDashTorcherService
                        ) extends VitalsService with BurstDashSsl with BurstDashService {

  override def serviceName: String = s"rest($url)"

  private var server: HttpServer = _

  var webSocketService: BurstWebSocketService = _

  private val profiler = BurstRestProfiler(agent, catalog)
  private var torcherRelay: BurstRestTorcherRelay = _
  private var executionRelay: BurstRestExecutionRelay = _
  private var fabricRelay: BurstRestFabricTopologyRelay = _
  private var profilerRelay: BurstRestProfilerRelay = _
  private var thriftRelay: BurstRestThriftRelay = _


  def startWebsocketServices(): Unit = {
    fabricRelay = BurstRestFabricTopologyRelay(supervisor.topology, webSocketService)
    supervisor.topology talksTo fabricRelay

    executionRelay = BurstRestExecutionRelay(webSocketService)
    fabric.execution.model.pipeline.addPipelineSubscriber(executionRelay)

    torcherRelay = BurstRestTorcherRelay(torcher, webSocketService)
    torcher.talksTo(torcherRelay)

    profilerRelay = BurstRestProfilerRelay(profiler, webSocketService)
    profiler.talksTo(profilerRelay)

    thriftRelay = BurstRestThriftRelay(webSocketService)
    thrift.requestLog.talksTo(thriftRelay)
  }

  def start: this.type = {
    ensureNotRunning
    log info startingMessage
    val uri = UriBuilder.fromPath(url).build()
    val config = new BurstDashApplication(agent, catalog, supervisor, profiler, torcher)
    val sslConfig = new SSLEngineConfigurator(restSslContext).setClientMode(false)
    log info startingMessage
    log info s"Configured TLS protocols:     ${Option(sslConfig.getEnabledProtocols).map(_.mkString("Array(", ", ", ")")).getOrElse("None")}"
    log info s"Configured TLS cipher suites: ${Option(sslConfig.getEnabledCipherSuites).map(_.mkString("Array(", ", ", ")")).getOrElse("None")}"
    server = GrizzlyHttpServerFactory.createHttpServer(uri, config, useHttps, sslConfig, false)
    val addOn: WebSocketAddOn = new WebSocketAddOn()
    for (nl: NetworkListener <- server.getListeners.asScala) {
      nl.registerAddOn(addOn)
    }
    server.start()
    webSocketService = BurstWebSocketService().start
    startWebsocketServices()
    markRunning
    this
  }

  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    webSocketService.stop
    server.shutdownNow()
    markNotRunning
    this
  }
}
