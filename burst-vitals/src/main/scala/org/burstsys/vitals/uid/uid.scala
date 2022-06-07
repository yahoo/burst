/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import java.security.MessageDigest
import java.util.UUID

package object uid {
  type VitalsUid = String

  /**
   * This a massaged [[UUID.randomUUID]]
   *
   * @return new uid
   */
  final
  def newBurstUid: VitalsUid =
    s"B${UUID.randomUUID().toString.toUpperCase.replaceAll("-", "")}"

  /**
   * Compute the md5 hash of the source string
   * @param source the content to hash
   * @return the hash
   */
  def md5(source: String): VitalsUid = {
    MessageDigest.getInstance("MD5").digest(source.getBytes).map(0xFF & _).map("%02x".format(_)).foldLeft("")(_ + _)
  }

}
