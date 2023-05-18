/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.errors

import org.burstsys.vitals.logging.burstStdMsg

import scala.language.implicitConversions

/**
 *
 */
object VitalsException {

  def apply()(implicit site: sourcecode.Enclosing, pkg: sourcecode.Pkg, file: sourcecode.FileName, line: sourcecode.Line): RuntimeException = {
    apply("no message")
  }

  def apply(s: String)(implicit site: sourcecode.Enclosing, pkg: sourcecode.Pkg, file: sourcecode.FileName, line: sourcecode.Line): RuntimeException = {
    new RuntimeException(burstStdMsg(s))
  }

  def apply(s: String, t: Throwable)(implicit site: sourcecode.Enclosing, pkg: sourcecode.Pkg, file: sourcecode.FileName, line: sourcecode.Line): RuntimeException = {
    new RuntimeException(burstStdMsg(s, t), t)
  }

  def apply(t: Throwable)(implicit site: sourcecode.Enclosing, pkg: sourcecode.Pkg, file: sourcecode.FileName, line: sourcecode.Line): RuntimeException = {
    new RuntimeException(burstStdMsg(t), t)
  }

}

abstract class VitalsException(error: VitalsError, msg: String) extends RuntimeException(s"${error.description}: $msg")
