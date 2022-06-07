/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client

import org.burstsys.client.server.async.BurstAsyncHttpServer
import org.burstsys.client.server.sync.BurstSyncHttpServer
import org.burstsys.gen.thrift.api.client.BTBurstService
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.JavaConverters._

class BaseClientTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll{

  var syncHttpServer: BurstSyncHttpServer = _
  var asyncHttpServer: BurstAsyncHttpServer = _
  var jettyServer: Server = _

  val emptyStringMap: java.util.Map[String, String] = Map.empty[String, String].asJava

  def port: Int = 4080

  def syncHander: Option[BTBurstService.Iface] = Option.empty

  def asyncHandler: Option[BTBurstService.AsyncIface] = Option.empty

  override def beforeAll(): Unit = {
    syncHander.foreach(handler => syncHttpServer = new BurstSyncHttpServer(handler))
    asyncHandler.foreach(handler => asyncHttpServer = new BurstAsyncHttpServer(handler))

    val thriftHolder = new ServletHolder
    if (syncHttpServer != null) {
      thriftHolder.setServlet(syncHttpServer.getServlet)
    } else if (asyncHttpServer != null) {
      thriftHolder.setServlet(asyncHttpServer.getServlet)
    }

    val handler = new ServletContextHandler
    handler.setContextPath("/thrift")
    handler.addServlet(thriftHolder, "/client")
    val server = new Server(port)
    server.setHandler(handler)
    server.start()
    jettyServer = server
  }

  override def afterAll(): Unit = {
    jettyServer.stop()
  }
}
