/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.application

import org.apache.commons.io.IOUtils
import org.burstsys.dash.{BurstDashService, configuration}
import org.burstsys.vitals.errors.safely
import org.glassfish.jersey.SslConfigurator

import java.io.{ByteArrayInputStream, File, FileInputStream, InputStream}
import java.security.KeyStore
import javax.net.ssl.SSLContext

/**
 * You don't need to specify a truststore, because there's a default value for it (it's bundled with the JRE),
 * usually in $JAVA_HOME/lib/security/cacerts.
 */
trait BurstDashSsl {
  final val KEYSTORE_SERVER_FILE = "/.keystore"

  def restSslContext: SSLContext = {
    val keystorePath = configuration.burstRestSslKeystorePath.asOption.getOrElse("")
    val keyStore = keystorePath match {
      case path if path != null && path.nonEmpty => new FileInputStream(new File(path))
      case _ => defaultInsecureKeystore
    }
    val keystoreBytes = IOUtils.toByteArray(keyStore)
    val keystorePassword = configuration.burstRestSslKeystorePassword.get

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

  private def defaultInsecureKeystore: InputStream = classOf[BurstDashService].getResourceAsStream(KEYSTORE_SERVER_FILE)

}
