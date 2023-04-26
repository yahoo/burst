/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.client

import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort}

import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue, TimeUnit}

case class NexusEndpoint(
                          serverHost: VitalsHostAddress,
                          serverPort: VitalsHostPort
                        ) {

  lazy val name = s"NexusEndpoint(serverHost=$serverHost, serverPort=$serverPort)"

  private val clientQ: LinkedBlockingQueue[NexusClient] = new LinkedBlockingQueue[NexusClient]

  var lastUseMillis: Long = -1L

  private def touch: this.type = {
    lastUseMillis = System.currentTimeMillis
    this
  }

  @scala.annotation.tailrec
  final def grabClient: NexusClient = {
    touch
    clientQ.poll() match {
      case null =>
        val client = NexusClient(serverHost, serverPort).start
        log debug s"NEXUS_GRAB_CLIENT new client id=${client.clientId} $name"
        client

      case client =>
        log debug s"NEXUS_GRAB_CLIENT existing client id=${client.clientId} $name running=${client.isRunning} connected=${client.isConnected}"
        if (client.isRunning && client.isConnected) {
          client
        } else {
          client.stopIfNotAlreadyStopped
          grabClient
        }
    }
  }

  def stopAll(): Unit = {
    val tag = s"NexusEndpoint.stopAll($name)"
    log debug s"$tag Stopping clients for $name count=${clientQ.size}"
    while (!clientQ.isEmpty) {
      clientQ.poll() match {
        case null =>
          log debug s"$tag client == null"
        case client =>
          client.stopIfNotAlreadyStopped
      }
    }
  }

  def releaseClient(client: NexusClient): Unit = {
    lazy val tag = s"NexusClientPool.releaseClient($name)"
    touch
    log debug s"NEXUS_RELEASE_CLIENT id=${client.clientId}, running=${client.isRunning}, connected=${client.isConnected} $tag"
    if (client.isActive) {
      log error s"NEXUS_RELEASE_ACTIVE_CLIENT -- release of an active client id=${client.clientId} stopping and discarding client $tag"
      client.stopIfNotAlreadyStopped
    } else if (client.isRunning && client.isConnected) {
      clientQ put client
    } else {
      log warn s"NEXUS_RELEASE_DEAD_CLIENT -- release of a dead client $tag"
    }
  }

  override def toString: String = s"serverHost=$serverHost, serverPort=$serverPort"
}
