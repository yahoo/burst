/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http

import org.apache.commons.io.IOUtils
import org.burstsys.fabric.configuration
import org.burstsys.vitals.errors.safely
import org.glassfish.jersey.SslConfigurator

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.KeyStore
import javax.net.ssl.SSLContext

/**
 * You don't need to specify a truststore, because there's a default value for it (it's bundled with the JRE),
 * usually in $JAVA_HOME/lib/security/cacerts.
 */
trait FabricHttpSSL {

  final val SSL_KEYSTORE_FILE = "/.keystore"

  def httpSSLContext: SSLContext = {
    val keystorePath = configuration.burstHttpSslKeystorePath.asOption.getOrElse("")
    val keyStore = keystorePath match {
      case path if path != null && path.nonEmpty => new FileInputStream(new File(path))
      case _ => defaultInsecureKeystore
    }
    val keystoreBytes = IOUtils.toByteArray(keyStore)
    val keystorePassword = configuration.burstHttpSslKeystorePassword.get

    val sslConfigurator: SslConfigurator = SslConfigurator.newInstance()
      .keyStoreBytes(keystoreBytes)
      .keyStorePassword(keystorePassword)

    val passwordLength = if (keystorePassword == null) -1 else keystorePassword.length
    try {
      log debug s"Attempting to list keystore of assumed type '${KeyStore.getDefaultType}'"
      log debug s"path=$keystorePath"
      log debug s"password length=$passwordLength"
      val ks = KeyStore.getInstance(KeyStore.getDefaultType)
      ks.load(new ByteArrayInputStream(keystoreBytes), keystorePassword.toCharArray)
      log debug s"Found ${ks.size()} entries in keystore"
    } catch safely {
      case t: Throwable =>
        log warn(s"Failed to list keystore entries for path=$keystorePath defaultType=${KeyStore.getDefaultType} passwordLength=$passwordLength", t)
    }

    sslConfigurator.createSSLContext()
  }

  private def defaultInsecureKeystore: InputStream = classOf[FabricHttpSSL].getResourceAsStream(SSL_KEYSTORE_FILE)

}
