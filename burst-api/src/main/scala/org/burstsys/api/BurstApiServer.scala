/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.api

import java.net.InetSocketAddress

import org.burstsys.vitals.{VitalsService, errors}
import org.burstsys.vitals.errors.{VitalsException, messageFromException, safely}
import com.twitter.finagle.netty4.ssl.server.Netty4ServerEngineFactory
import com.twitter.finagle.ssl._
import com.twitter.finagle.ssl.server.SslServerConfiguration
import com.twitter.finagle.thrift.ThriftService
import com.twitter.finagle.{ListeningServer, Thrift}

import scala.reflect.ClassTag
import org.burstsys.vitals.logging._

abstract class BurstApiServer[S <: ThriftService : ClassTag] extends VitalsService with BurstApi {

  /////////////////////////////////////////////////////////////////////////////////////////
  // STATE
  /////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _server: ListeningServer = _

  /////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  /////////////////////////////////////////////////////////////////////////////////////////

  final override
  def start: this.type = {
    ensureNotRunning
    System.setProperty("com.twitter.finagle.netty3.numWorkers", apiWorkerThreads.toString)
    log info startingMessage
    log debug burstStdMsg(
      s"""
         |starting finagle thrift '$apiName' server
         |  binding=$apiHost:$apiPort
         |  requestTimeout=$apiRequestTimeout
         |  workerThreads=$apiWorkerThreads
       """.stripMargin
    )
    // always bind to all available addresses
    val address = new InetSocketAddress(apiPort)

    try {
      var serverBuilder = Thrift.server
        .withSession.maxLifeTime(maxConnectionLifeTime)
        .withSession.maxIdleTime(maxConnectionIdleTime)
        .withRequestTimeout(apiRequestTimeout)

      if (enableSsl) {
        serverBuilder = serverBuilder.withTransport.tls(
          config = SslServerConfiguration(
            keyCredentials,
            ClientAuth.Needed,
            trustCredentials,
            enabledCipherSuites,
            enabledProtocols,
            ApplicationProtocols.Unspecified
          ),
          // the JDK provided SSL engine is good enough for us, and openSsl has a propensity to do weird stuff
          engineFactory = Netty4ServerEngineFactory(forceJdk = true)
        )
      }
      _server = serverBuilder.serveIface(address, this)
    } catch safely {
      case t: Throwable =>
        val msg = s"'$serviceName' $apiUrl: ${messageFromException(t)}"
        log error(burstStdMsg(msg, t), t)
        throw VitalsException(msg, t)
    }
    markRunning
    this
  }

  final override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    _server.close()
    _server = null
    markNotRunning
    this
  }

}
