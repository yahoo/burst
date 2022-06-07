/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.net.ssl

import java.io._
import java.net.Socket
import java.security._
import java.security.cert.{Certificate, X509Certificate}
import java.security.spec.RSAPrivateCrtKeySpec

import org.burstsys.vitals.background.VitalsBackgroundFunction
import javax.net.ssl._
import org.bouncycastle.asn1.pkcs.RSAPrivateKey
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import org.bouncycastle.crypto.util.PrivateKeyFactory
import org.bouncycastle.util.io.pem.PemReader

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.reflect.ClassTag

/**
 * Largely cribbed from Netty, because it's package private code there.
 * This is a pretty straight forward key manager that just returns the provided private key.
 */
object BurstKeyManagerFactory {
  private val PROVIDER: Provider = new Provider("BurstKeyManager", 0.0, "") {}

  def apply(privateKey: File, certificate: File): BurstKeyManagerFactory =
    new BurstKeyManagerFactory(new BurstKeyManagerSpi(privateKey, certificate), PROVIDER)
}

class BurstKeyManagerFactory(spi: BurstKeyManagerSpi, provider: Provider)
  extends KeyManagerFactory(spi, provider, "")

class BurstKeyManagerSpi(privateKey: File, certificate: File) extends KeyManagerFactorySpi {

  private val keyManagerProxy = new BurstKeyManagerProxy(getKeyManagers)

  private val md = MessageDigest.getInstance("MD5")
  private val keyChecksum = new Array[Byte](md.getDigestLength)
  private val certChecksum = new Array[Byte](md.getDigestLength)
  private val refresher = new VitalsBackgroundFunction("ssl-refresher", 1 hour, 1 hour, {
    val keyUpdated = fileChecksumChanged(privateKey.getAbsolutePath, md, keyChecksum)
    val certUpdated = fileChecksumChanged(certificate.getAbsolutePath, md, certChecksum)
    if (keyUpdated || certUpdated) {
      log info s"Reloading keystore because keyUpdated=$keyUpdated certUpdated=$certUpdated"
      keyManagerProxy.keyManagers = getKeyManagers
    }
  }).start

  private def getKeyManagers: Array[X509ExtendedKeyManager] = {
    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType)
    keyStore.load(null, null)
    keyStore.setKeyEntry("key", loadPrivateKey, Array.emptyCharArray, loadCertificateChain)

    val factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
    factory.init(keyStore, Array.empty[Char])
    factory.getKeyManagers.map(_.asInstanceOf[X509ExtendedKeyManager])
  }

  private def loadPrivateKey: PrivateKey = {
    val factory = KeyFactory.getInstance("RSA")
    val pem = new PemReader(new FileReader(privateKey)).readPemObject()
    val spec = pem.getType match {
      case "RSA PRIVATE KEY" => parsePkcs1Key(pem.getContent)
      case "PRIVATE KEY" => parsePkcs8Key(pem.getContent)
      case _ => throw new IllegalStateException(s"Unknown private key type ${pem.getType}")
    }
    factory.generatePrivate(spec)
  }

  private def parsePkcs1Key(content: Array[Byte]): RSAPrivateCrtKeySpec = {
    val key = RSAPrivateKey.getInstance(content)
    new RSAPrivateCrtKeySpec(
      key.getModulus,
      key.getPublicExponent,
      key.getPrivateExponent,
      key.getPrime1,
      key.getPrime2,
      key.getExponent1,
      key.getExponent2,
      key.getCoefficient
    )
  }

  private def parsePkcs8Key(content: Array[Byte]): RSAPrivateCrtKeySpec = {
    val key = PrivateKeyFactory.createKey(content).asInstanceOf[RSAPrivateCrtKeyParameters]
    new RSAPrivateCrtKeySpec(
      key.getModulus,
      key.getPublicExponent,
      key.getExponent,
      key.getP,
      key.getQ,
      key.getDP,
      key.getDQ,
      key.getQInv
    )
  }

  private def loadCertificateChain: Array[Certificate] = parseX509Certificates(certificate).toArray

  override def engineInit(keyStore: KeyStore, chars: Array[Char]): Unit = {}

  override def engineInit(managerFactoryParameters: ManagerFactoryParameters): Unit = {}

  override def engineGetKeyManagers(): Array[KeyManager] = Array(keyManagerProxy)
}

class BurstKeyManagerProxy(var keyManagers: Array[X509ExtendedKeyManager]) extends X509ExtendedKeyManager {

  private def mergeResults[T: ClassTag](fn: X509ExtendedKeyManager => Array[T]): Array[T] = {
    keyManagers.flatMap({ km =>
      fn(km) match {
        case value if value == null => List.empty
        case value => value
      }
    }) match {
      case result if result.isEmpty => null
      case result => result
    }
  }

  private def pickFirst[T >: Null](fn: X509ExtendedKeyManager => T): T = {
    keyManagers.foreach({ km =>
      val result = fn(km)
      if (result != null) {
        return result
      }
    })
    null
  }

  override def getClientAliases(s: String, principals: Array[Principal]): Array[String] = {
    mergeResults(_.getClientAliases(s, principals))
  }

  override def chooseClientAlias(strings: Array[String], principals: Array[Principal], socket: Socket): String = {
    pickFirst(_.chooseClientAlias(strings, principals, socket))
  }

  override def getServerAliases(s: String, principals: Array[Principal]): Array[String] = {
    mergeResults(_.getServerAliases(s, principals))
  }

  override def chooseServerAlias(s: String, principals: Array[Principal], socket: Socket): String = {
    pickFirst(_.chooseServerAlias(s, principals, socket))
  }

  override def getCertificateChain(s: String): Array[X509Certificate] = {
    mergeResults(_.getCertificateChain(s))
  }

  override def getPrivateKey(s: String): PrivateKey = {
    pickFirst(_.getPrivateKey(s))
  }

  override def chooseEngineClientAlias(strings: Array[String], principals: Array[Principal], sslEngine: SSLEngine): String = {
    pickFirst(_.chooseEngineClientAlias(strings, principals, sslEngine))
  }

  override def chooseEngineServerAlias(s: String, principals: Array[Principal], sslEngine: SSLEngine): String = {
    pickFirst(_.chooseEngineServerAlias(s, principals, sslEngine))
  }
}
