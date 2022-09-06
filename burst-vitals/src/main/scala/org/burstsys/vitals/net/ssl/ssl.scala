/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.net

import java.io.{File, FileInputStream, IOException}
import java.security.cert.{CertificateFactory, X509Certificate}
import java.security.{DigestInputStream, MessageDigest}

import org.burstsys.vitals.logging._
import org.burstsys.vitals.errors._

import scala.jdk.CollectionConverters._

package object ssl extends VitalsLogger {

  def fileChecksumChanged(absolutePath: String, digest: MessageDigest, lastChecksum: Array[Byte]): Boolean = {
    try {
      val digestStream = new DigestInputStream(new FileInputStream(new File(absolutePath)), digest)
      while (digestStream.read != -1) {
      }
    } catch safely {
      case ex: IOException =>
        log warn s"IOException found when attempting to compute a checksum for $absolutePath. (${ex.getLocalizedMessage})"
    }

    val newChecksum = digest.digest()
    val fileChanged = !lastChecksum.sameElements(newChecksum)
    if (fileChanged) {
      newChecksum.copyToArray(lastChecksum)
    }
    fileChanged
  }

  def parseX509Certificates(chain: File): Seq[X509Certificate] = {
    val certFactory = CertificateFactory.getInstance("X.509")
    val certs = certFactory.generateCertificates(new FileInputStream(chain)).asScala
      .map(_.asInstanceOf[X509Certificate])
      .toSeq
    certs
  }

}
