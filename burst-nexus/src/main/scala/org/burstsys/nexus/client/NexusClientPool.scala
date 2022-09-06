/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.client

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.{BlockingQueue, ConcurrentHashMap, LinkedBlockingQueue}

import org.burstsys.nexus.configuration.{burstNexusClientCacheTenderDuration, burstNexusClientStaleDuration}
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort}

import scala.jdk.CollectionConverters._
import scala.language.postfixOps

/**
 * We cache clients based on [[NexusEndpoint]] instances. They are key'ed by a (host, port) tuple and contain
 * a queue of active clients that can be grab'ed and release'd. There is one client per (host, port) connection to a
 * remote nexus server. That means we have active clients X max active streams concurrent clients in our cache at
 * any one pt. We keep track of activity and release/stop endpoints as they become stale (not recently used)
 */
trait NexusClientPool extends AnyRef {

  final val doClientPooling = true

  case
  class NexusEndpoint(serverHost: VitalsHostAddress, serverPort: VitalsHostPort, clientQ: BlockingQueue[NexusClient] = new LinkedBlockingQueue[NexusClient]) {
    lazy val name = s"NexusEndpoint(serverHost=$serverHost, serverPort=$serverPort)"

    var lastUseMillis: Long = -1L

    def touch: this.type = {
      lastUseMillis = System.currentTimeMillis
      this
    }

    @scala.annotation.tailrec
    final def grabClient: NexusClient = {
      lazy val tag = s"NexusClientPool.grabClient($name)"
      touch
      clientQ.poll() match {
        case null =>
          val client = NexusClient(serverHost = serverHost, serverPort = serverPort).start
          log info s"NEXUS_GRAB_CLIENT -- new client id=${client.clientId} $tag"
          client

        case client =>
          log info s"NEXUS_GRAB_CLIENT -- existing client id=${client.clientId}, running=${client.isRunning}, active=${client.isConnected} $tag"
          if (client.isRunning && client.isConnected) {
            client
          } else {
            client.stopIfNotAlreadyStopped
            grabClient
          }
      }
    }

    def stopAll(): Unit = {
      lazy val tag = s"NexusClientPool.stopAll($name)"
      while (true) {
        val client = clientQ.poll()
        if (client == null) return
        log info s"NEXUS_STOP_CLIENT id=${client.clientId} $tag"
        client.stopIfNotAlreadyStopped
      }
    }

    def releaseClient(client: NexusClient): Unit = {
      lazy val tag = s"NexusClientPool.releaseClient($name)"
      touch
      log info s"NEXUS_RELEASE_CLIENT id=${client.clientId}, running=${client.isRunning}, connected=${client.isConnected} $tag"
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

  ////////////////////////////////////////////////////////////////////////////////////
  // internal state
  ////////////////////////////////////////////////////////////////////////////////////

  private[this] final
  val _clientCache = new ConcurrentHashMap[(VitalsHostAddress, VitalsHostPort), NexusEndpoint]().asScala

  private[this] final
  val _gate = new ReentrantLock

  ////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////

  final
  def shutdownPool: this.type = {
    _gate.lock()
    try {
      _clientCache.foreach(_._2.stopAll())
      _nexusClientCacheTenderFunction.stop
    } finally _gate.unlock()
    this
  }

  // TODO FIX FOR SERVER DEATH
  final
  def grabClientFromPool(serverHost: VitalsHostAddress, serverPort: VitalsHostPort): NexusClient = {
    _nexusClientCacheTenderFunction.startIfNotRunning
    _gate.lock()
    try {
      _clientCache.getOrElseUpdate(
        (serverHost, serverPort),
        NexusEndpoint(serverHost = serverHost, serverPort = serverPort)
      ).grabClient
    } finally _gate.unlock()
  }

  final
  def releaseClientToPool(client: NexusClient): Unit = {
    lazy val tag = s"NexusClientPool.releaseClientToPool($client)"
    _gate.lock()
    try {
      _clientCache.get((client.serverHost, client.serverPort)) match {
        case Some(endpoint) => endpoint.releaseClient(client)
        case None =>
          log error s"NEXUS_CLIENT_NOT_FOUND!! endpoint(serverHost=${client.serverHost}, serverPort=${client.serverPort}) $tag"
      }
    } finally _gate.unlock()
  }

  private[this] final
  lazy val _nexusClientCacheTenderFunction = new VitalsBackgroundFunction(
    "nexus-client-pool-tender", burstNexusClientCacheTenderDuration, burstNexusClientCacheTenderDuration,
    try {
      _gate.lock()
      try {
        _clientCache foreach {
          case ((hostAddress, port), endpoint) =>
            if (endpoint.lastUseMillis < (System.currentTimeMillis - burstNexusClientStaleDuration.toMillis)) {
              // inactive since a long time
              log info s"NexusPool STALE endpoint($endpoint) staleDuration=$burstNexusClientStaleDuration"
              _clientCache.remove((hostAddress, port)) match {
                case Some(ep) => ep.stopAll()
                case None =>
                  log error s"NexusPool REMOVE NOT FOUND!! endpoint(serverHost=$hostAddress, serverPort=$port)"
              }
            }
        }
      } finally _gate.unlock()
    } catch safely {
      case t: Throwable => log error burstStdMsg(t)
    }
  )

}
