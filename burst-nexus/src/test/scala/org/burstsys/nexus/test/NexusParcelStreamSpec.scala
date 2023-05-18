/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.test

import org.burstsys.nexus
import org.burstsys.nexus.{NexusUid, newNexusUid}
import org.burstsys.nexus.client.{NexusClient, NexusClientListener}
import org.burstsys.nexus.server.{NexusServer, NexusServerListener, NexusStreamFeeder}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.tesla.parcel.pipe.TeslaParcelPipe
import org.burstsys.vitals.net.{getPublicHostAddress, getPublicHostName}

abstract class NexusParcelStreamSpec extends NexusSpec {

  def feeder: NexusStreamFeeder

  def serverListener: NexusServerListener = new NexusServerListener {}

  def clientListener: NexusClientListener = new NexusClientListener {}

  private var _server: NexusServer = _

  def server: NexusServer = _server

  private var _client: NexusClient = _

  def client: NexusClient = _client

  def suid: NexusUid = newNexusUid

  private def getClient: NexusClient =
    nexus.grabClientFromPool(getPublicHostAddress, server.serverPort)
      .talksTo(clientListener)

  def withClient(work: NexusClient => Unit): Unit = {
    val client = getClient
    try {
      work(client)
    } finally nexus.releaseClientToPool(client)
  }

  def startStream(guid: NexusUid, suid: NexusUid = suid, client: NexusClient = client): NexusStream = {
    val pipe = TeslaParcelPipe(s"pipe-$guid", guid, suid).start
    client.startStream(
      guid, suid,
      properties = Map("someKey" -> "someValue"), schema = "quo", filter = Some("someMotifFilter"),
      pipe, sliceKey = 0, clientHostname = getPublicHostName, serverHostname = getPublicHostName
    )
  }

  override def beforeEach(): Unit = {
    log debug s"$beginMarker beforeEach $suiteName $beginMarker"
    _server = nexus.grabServer(getPublicHostAddress)
      .fedBy(feeder)
      .talksTo(serverListener)
    _client = getClient
    log debug s"$endMarker beforeEach $suiteName $endMarker"
  }

  override def afterEach(): Unit = {
    log debug s"$beginMarker afterEach $suiteName $beginMarker"
    log debug s"Releasing client suite=$suiteName"; nexus.releaseClientToPool(_client)
    log debug s"Releasing server suite=$suiteName"; nexus.releaseServer(_server)
    log debug s"Shutting down pool suite=$suiteName"; nexus.client.shutdownPool
    log debug s"$endMarker afterEach $suiteName $endMarker"
  }
}
