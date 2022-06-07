/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.errors

import org.burstsys.vitals.logging.{BurstModuleName, burstStdMsg}

import scala.language.implicitConversions

/**
 *
 */
object VitalsException {

  def apply(): RuntimeException = {
    new RuntimeException("no message")
  }

  def apply(s: String)(implicit burstModuleName: BurstModuleName): RuntimeException = {
    new RuntimeException(burstStdMsg(s))
  }

  def apply(s: String, t: Throwable)(implicit burstModuleName: BurstModuleName): RuntimeException = {
    new RuntimeException(burstStdMsg(s, t), t)
  }

  def apply(t: Throwable)(implicit burstModuleName: BurstModuleName): RuntimeException = {
    new RuntimeException(burstStdMsg("", t), t)
  }

}

abstract class VitalsException(error: VitalsError, msg: String)
  extends RuntimeException(s"${error.description}: $msg")
