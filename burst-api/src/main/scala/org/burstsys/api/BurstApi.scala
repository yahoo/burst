/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.api

import java.io.File

import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.net.ssl.{BurstKeyManagerFactory, BurstTrustManagerFactory}
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort}
import com.twitter.finagle.ssl.{CipherSuites, KeyCredentials, Protocols, TrustCredentials}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  *
  * Burst Thrift API base (uses twitter finagle and NETTY)
  * <hr/>
  * [finagle tutorial](http://vkostyukov.net/posts/finagle-101/)
  */
trait BurstApi extends VitalsService {

  final override def serviceName: String = s"$apiName-api($apiUrl, SSL=$enableSsl)"

  /**
    * the name of this API client/server displayed in log messages
    */
  def apiName: String

  /**
    * port to bind to (server) or connect to (client)
    */
  def apiPort: VitalsHostPort

  /**
    * address to bind to (server) or connect to (client)
    */
  def apiHost: VitalsHostAddress

  /**
    * the maximum amount of time a given connection is allowed to be idle before it is closed
    */
  def maxConnectionIdleTime: Duration

  /**
    * the maximum amount of time a given connection is allowed to live before it is closed
    */
  def maxConnectionLifeTime: Duration

  /**
    * user friendly binding string
    */
  final def apiUrl: String = s"$apiHost:$apiPort"

  /**
    * enables TLS connections
    */
  def enableSsl: Boolean

  /**
    * path to a certificate file for TLS enable thrift
    */
  def certPath: String

  /**
    * path to private key file for TLS enabled thrift
    */
  def keyPath: String

  /**
    * path to a file containing a CA chain for TLS enabled thrift
    */
  def caPath: String

  /**
    * enable a composite trust manager what will trust certs that:
    * - are trusted by the JVM's default trust material
    * - are issued from a CA described by the certificate chain at `caPath`
    */
  def enableCompositeTrust: Boolean

  /**
    * Configures the request `timeout` of this server or client (default: unbounded).
    *
    * If the request has not completed within the given `timeout`, the pending
    * work will be interrupted via [[com.twitter.util.Future.raise]].
    *
    * == Client's Request Timeout ==
    *
    * The client request timeout is the maximum amount of time given to a single request
    * (if there are retries, they each get a fresh request timeout). The timeout is applied
    * only after a connection has been acquired. That is: it is applied to the interval
    * between the dispatch of the request and the receipt of the response.
    *
    * == Server's Request Timeout ==
    *
    *
    * The server request timeout is the maximum amount of time, a server is allowed to
    * spend handling the incoming request. Using the Finagle terminology, this is an amount
    * of time after which a non-satisfied future returned from the user-defined service
    * times out.
    *
    */
  def apiRequestTimeout: Duration = 5 minutes

  /**
    * sets "com.twitter.finagle.netty3.numWorkers"
    *
    * @return
    */
  def apiWorkerThreads: Int = Runtime.getRuntime.availableProcessors * 4

  final override def toString: String = s"${if (modality.isServer) "server" else "client"}:/$apiHost:$apiPort/$apiName"

  // force TLS 1.2 because we're not in 1999
  protected final def enabledProtocols: Protocols = {
    Protocols.Enabled(Array("TLSv1.2"))
  }

  // use any cipher available, you should disable cipher suites at a JVM level if you have specific requirements
  // for details see https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html#DisabledAlgorithms
  protected final def enabledCipherSuites: CipherSuites = {
    CipherSuites.Unspecified
  }

  protected final def keyCredentials: KeyCredentials = {
    val certificate = new File(certPath)
    val privateKey = new File(keyPath)
    KeyCredentials.KeyManagerFactory(BurstKeyManagerFactory(privateKey, certificate))
  }

  protected final def trustCredentials: TrustCredentials = {
    val caChain = new File(caPath)

    if (enableCompositeTrust)
      TrustCredentials.TrustManagerFactory(BurstTrustManagerFactory(caChain))
    else
      TrustCredentials.CertCollection(caChain)

  }
}
