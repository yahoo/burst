/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.api

import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.logging._
import com.twitter.finagle.Thrift
import com.twitter.finagle.netty4.ssl.client.Netty4ClientEngineFactory
import com.twitter.finagle.ssl._
import com.twitter.finagle.ssl.client.SslClientConfiguration
import com.twitter.finagle.thrift.ThriftService
import org.burstsys.tesla

import scala.reflect.ClassTag

abstract class BurstApiClient[S <: ThriftService : ClassTag] extends VitalsService with BurstApi {

  /////////////////////////////////////////////////////////////////////////////////////////
  // STATE
  /////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _thriftClient: S = _

  def thriftClient: S = _thriftClient

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
         |starting '$apiName' thrift client
         |  binding=$apiHost:$apiPort
         |  requestTimeout=$apiRequestTimeout
         |  workerThreads=$apiWorkerThreads
         |  enableSSL=$enableSsl
         |  compositeTrust=$enableCompositeTrust
       """.stripMargin
    )

    var clientBuilder = Thrift.client
      .withExecutionOffloaded(tesla.thread.request.teslaRequestExecutorService)
      .withSessionQualifier
      .noFailFast
      .withRequestTimeout(apiRequestTimeout)
      .withNoAttemptTTwitterUpgrade

    if (enableSsl) {
      clientBuilder = clientBuilder.withTransport.tls(
        SslClientConfiguration(
          hostname = Some(apiHost),
          keyCredentials,
          trustCredentials,
          enabledCipherSuites,
          enabledProtocols,
          ApplicationProtocols.Unspecified
        ),
        // the JDK provided SSL engine is good enough for us, and openSsl has a propensity to do weird stuff
        engineFactory = Netty4ClientEngineFactory(forceJdk = true)
      )
    }

    _thriftClient = clientBuilder.build(apiUrl)
    markRunning
    this
  }

  final override
  def stop: this.type = {
    ensureRunning
    log info stoppingMessage
    // TODO - this close method is in the twitter release pipeline
    // _thriftClient.asCloseable.close()
    _thriftClient = null.asInstanceOf[S]
    markNotRunning
    this
  }

}
