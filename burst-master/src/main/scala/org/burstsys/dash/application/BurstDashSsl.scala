/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.application

import java.io.{File, FileInputStream, InputStream}
import org.burstsys.dash.{BurstDashService, configuration}
import com.google.common.io.ByteStreams
import org.apache.commons.io.IOUtils

import javax.net.ssl.SSLContext
import org.glassfish.jersey.SslConfigurator

/**
 * You don't need to specify a truststore, because there's a default value for it (it's bundled with the JRE),
 * usually in $JAVA_HOME/lib/security/cacerts.
 */
trait BurstDashSsl {
  final val KEYSTORE_SERVER_FILE = "/.keystore"

  def restSslContext: SSLContext = {
    val keyStore = configuration.burstRestSslKeystorePath.get
      .collect({ case path if path.nonEmpty => new FileInputStream(new File(path)) })
      .getOrElse(defaultInsecureKeystore)

    val sslConfigServer: SslConfigurator = SslConfigurator.newInstance()
      .keyStoreBytes(IOUtils.toByteArray(keyStore))
      .keyPassword(configuration.burstRestSslKeystorePassword.getOrThrow)

    sslConfigServer.createSSLContext()
  }

  private def defaultInsecureKeystore: InputStream = classOf[BurstDashService].getResourceAsStream(KEYSTORE_SERVER_FILE)

}
