/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.keystore

import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging.VitalsLog
import org.burstsys.vitals.logging.VitalsLogger

import java.security.KeyStore

object LoadKeystoreMain extends VitalsLogger {
  def main(args: Array[String]): Unit = {
    VitalsLog.configureLogging("test")
    inspectKeystore("/certs/athenz/keystore.nopass.pkcs12", "")
    inspectKeystore("/certs/athenz/keystore.stdinpass.pkcs12", "burstburstburst")
    inspectKeystore("/certs/athenz/keystore.clipass.pkcs12", "burstburstburst")
    inspectKeystore("/certs/athenz/service.keystore.pkcs12", "burstburstburst")
  }

  private def inspectKeystore(resource: String, password: String): Unit = {
    val stream = LoadKeystoreMain.getClass.getResourceAsStream(resource)
    try {
      log info s"Attempting to list keystore '$resource' assumed type '${KeyStore.getDefaultType}'"
      log info s"password length=${password.length}"
      val ks = KeyStore.getInstance(KeyStore.getDefaultType)
      ks.load(stream, password.toCharArray)
      log info s"Found ${ks.size()} entries in keystore"
    } catch safely {
      case t: Throwable =>
        log warn("Failed to list keystore entries", t)
    }
  }
}
