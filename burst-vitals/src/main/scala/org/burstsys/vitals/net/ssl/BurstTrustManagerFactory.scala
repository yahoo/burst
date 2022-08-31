/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.net.ssl

import java.io.File
import java.net.Socket
import java.security.cert.{CertificateException, X509Certificate}
import java.security.{KeyStore, MessageDigest, Provider}
import java.util.UUID

import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.errors._
import javax.net.ssl._

import scala.concurrent.duration._
import scala.language.postfixOps

object BurstTrustManagerFactory {
  private val PROVIDER: Provider = new Provider("", "0.0", "") {}

  def apply(caChain: File): BurstTrustManagerFactory = {
    new BurstTrustManagerFactory(new BurstTrustManagerFactorySpi(caChain), PROVIDER)
  }
}

/**
 * A trust manager factory that delegates trust to a BurstCompositeTrustManager instance
 */
class BurstTrustManagerFactory(spi: BurstTrustManagerFactorySpi, provider: Provider)
  extends TrustManagerFactory(spi, provider, "")

class BurstTrustManagerFactorySpi(caChain: File) extends TrustManagerFactorySpi {

  private val compositeTrustManager = new BurstCompositeTrustManager(
    caManagers = x509TrustManagersFor(buildKeystore(parseX509Certificates(caChain))),
    systemManagers = x509TrustManagersFor(null)
  )

  private val md = MessageDigest.getInstance("MD5")
  private val checksum = new Array[Byte](md.getDigestLength)
  private val refresher = new VitalsBackgroundFunction("ssl-refresher", 1 hour, 1 hour, {
    if (fileChecksumChanged(caChain.getAbsolutePath, md, checksum)) {
      log info "Reloading truststore ca cert"
      compositeTrustManager.setCaManagers(
        x509TrustManagersFor(buildKeystore(parseX509Certificates(caChain)))
      )
    }
  }).start

  private def buildKeystore(certs: Seq[X509Certificate]): KeyStore = {
    val keyStore = KeyStore.getInstance("JKS")
    keyStore.load(null)
    certs.foreach(keyStore.setCertificateEntry(UUID.randomUUID().toString, _))
    keyStore
  }

  private def x509TrustManagersFor(keystore: KeyStore): Array[X509TrustManager] = {
    val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
    factory.init(keystore)
    factory.getTrustManagers
      .filter(_.isInstanceOf[X509TrustManager])
      .map(_.asInstanceOf[X509TrustManager])
  }

  override def engineInit(keyStore: KeyStore): Unit = {}

  override def engineInit(managerFactoryParameters: ManagerFactoryParameters): Unit = {}

  override def engineGetTrustManagers(): Array[TrustManager] = Array(compositeTrustManager)
}

/**
 * Represents an ordered list of `X509TrustManager`s with additive trust. If any one of the composed managers
 * trusts a certificate chain, then it is trusted by the composite manager.
 * <br/>
 * This is necessary because of the fine-print on `SSLContext#init`: Only the first instance of a particular key
 * and/or trust manager implementation type in the array is used. (For example, only the first
 * javax.net.ssl.X509KeyManager in the array will be used.)
 * <br/>
 * Adapted from https://gist.github.com/HughJeffner/6eac419b18c6001aeadb
 * We must extend `X509ExtendedTrustManager` otherwise the SSL engine will wrap this trust manager
 * and we die in the wrapper's extended validation.
 *
 * @param caManagers     trust managers that trust certs issued by the CA chain with no extended validation
 * @param systemManagers default trust managers from the JVM
 */
class BurstCompositeTrustManager(var caManagers: Array[X509TrustManager], systemManagers: Array[X509TrustManager]) extends X509ExtendedTrustManager {
  type StandardValidation = X509TrustManager => Unit
  type ExtendedValidation = X509ExtendedTrustManager => Unit

  private var acceptedIssuers = caManagers.flatMap(_.getAcceptedIssuers)
  private val logCN = true

  def setCaManagers(managers: Array[X509TrustManager]): Unit = {
    caManagers = managers
    acceptedIssuers = caManagers.flatMap(_.getAcceptedIssuers)
  }

  // This is the whole point of this trust manager.
  // we loop through all of the trust managers that are configured and check to see if any of them accept the provided credentials
  // We know that the credentials are accepted because the validation throws an CertificateException if they are not accepted
  private def checkTrustManagers(managers: Array[X509TrustManager], simple: StandardValidation, extended: Option[ExtendedValidation]): Boolean = {
    for (manager <- managers) {
      try {
        (manager, extended) match {
          case (extendedManager: X509ExtendedTrustManager, Some(validation)) => validation(extendedManager)
          case _ => simple(manager)
        }
        return true
      } catch safely {
        case _: CertificateException => // ignore this and try another manager
      }
    }
    false
  }

  // Our trust manager is configured to perform the simplest possible validation to check and see if the credentials
  // are trusted by a trust manager for the provided internal CA. You should be very sure that you want to implicitly
  // trust credentials issued by this CA
  private def ensureConnectionTrust(simple: StandardValidation, extendedValidation: Option[ExtendedValidation]): Unit = {
    val trustedByCa = checkTrustManagers(caManagers, simple, None)
    if (trustedByCa) {
      return
    }
    val isTrusted = checkTrustManagers(systemManagers, simple, extendedValidation)
    if (!isTrusted) {
      throw new CertificateException("Chain does not contain a trusted cert")
    }
  }

  override def checkClientTrusted(chain: Array[X509Certificate], authType: String): Unit = {
    ensureConnectionTrust(_.checkClientTrusted(chain, authType), None)
  }

  override def checkClientTrusted(chain: Array[X509Certificate], authType: String, socket: Socket): Unit = {
    ensureConnectionTrust(_.checkClientTrusted(chain, authType), Some(_.checkClientTrusted(chain, authType, socket)))
  }

  override def checkClientTrusted(chain: Array[X509Certificate], authType: String, sslEngine: SSLEngine): Unit = {
    ensureConnectionTrust(_.checkClientTrusted(chain, authType), Some(_.checkClientTrusted(chain, authType, sslEngine)))
  }

  override def checkServerTrusted(chain: Array[X509Certificate], authType: String): Unit = {
    ensureConnectionTrust(_.checkServerTrusted(chain, authType), None)
  }

  override def checkServerTrusted(chain: Array[X509Certificate], authType: String, socket: Socket): Unit = {
    ensureConnectionTrust(_.checkClientTrusted(chain, authType), Some(_.checkServerTrusted(chain, authType, socket)))
  }

  override def checkServerTrusted(chain: Array[X509Certificate], authType: String, sslEngine: SSLEngine): Unit = {
    ensureConnectionTrust(_.checkClientTrusted(chain, authType), Some(_.checkServerTrusted(chain, authType, sslEngine)))
  }

  override def getAcceptedIssuers: Array[X509Certificate] = acceptedIssuers
}
