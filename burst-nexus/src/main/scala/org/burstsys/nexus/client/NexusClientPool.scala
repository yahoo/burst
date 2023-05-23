/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.client

import org.burstsys.nexus.configuration.{burstNexusClientCacheTenderDuration, burstNexusClientStaleDuration}
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort}

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.{BlockingQueue, ConcurrentHashMap, LinkedBlockingQueue}
import scala.language.postfixOps

/**
 * We cache clients based on [[NexusEndpoint]] instances. They are key'ed by a (host, port) tuple and contain
 * a queue of active clients that can be grab'ed and release'd. There is one client per (host, port) connection to a
 * remote nexus server. That means we have active clients X max active streams concurrent clients in our cache at
 * any one pt. We keep track of activity and release/stop endpoints as they become stale (not recently used)
 */
trait NexusClientPool extends AnyRef {

  ////////////////////////////////////////////////////////////////////////////////////
  // internal state
  ////////////////////////////////////////////////////////////////////////////////////

  private[this] final
  val _clientCache = new ConcurrentHashMap[(VitalsHostAddress, VitalsHostPort), NexusEndpoint]()

  ////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////

  final def shutdownPool: this.type = {
    _clientCache.forEach((_, endpoint) => endpoint.stopAll())
    _nexusClientCacheTenderFunction.stopIfNotAlreadyStopped
    this
  }

  final def grabClientFromPool(serverHost: VitalsHostAddress, serverPort: VitalsHostPort): NexusClient = {
    _nexusClientCacheTenderFunction.startIfNotAlreadyStarted
    _clientCache.computeIfAbsent(
      (serverHost, serverPort),
      key => NexusEndpoint(serverHost = key._1, serverPort = key._2)
    ).grabClient
  }

  final def releaseClientToPool(client: NexusClient): Unit = {
    lazy val tag = s"NexusClientPool.releaseClientToPool($client)"
    _clientCache.get((client.serverHost, client.serverPort)) match {
      case null =>
        log error s"NEXUS_CLIENT_NOT_FOUND!! endpoint(serverHost=${client.serverHost}, serverPort=${client.serverPort}) $tag"
        client.stopIfNotAlreadyStopped

      case endpoint =>
        endpoint.releaseClient(client)
    }
  }

  private[this] final
  lazy val _nexusClientCacheTenderFunction = new VitalsBackgroundFunction(
    "nexus-client-pool-tender", burstNexusClientCacheTenderDuration, burstNexusClientCacheTenderDuration,
    try {
      _clientCache forEach { (hostPort, endpoint) =>
        if (endpoint.lastUseMillis < (System.currentTimeMillis - burstNexusClientStaleDuration.toMillis)) {
          // inactive since a long time
          log info s"NexusPool STALE endpoint($endpoint) staleDuration=$burstNexusClientStaleDuration"
          _clientCache.remove(hostPort) match {
            case null =>
              log error s"NexusPool REMOVE NOT FOUND!! endpoint(serverHost=${hostPort._1}, serverPort=${hostPort._2})"
            case endpoint =>
              endpoint.stopAll()
          }
        }
      }
    } catch safely { // catch here to prevent the background function from dying
      case t: Throwable =>
        log error(burstStdMsg(t), t)
    }
  )

}
