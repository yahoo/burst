/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap

import org.burstsys.vitals.logging.{VitalsLog, VitalsLogger}
import org.burstsys.vitals.text.VitalsTextCodec
import org.apache.logging.log4j.Logger
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

package object test extends VitalsLogger {

  trait ZapSpecLog {

    VitalsLog.configureLogging("zap", true)

    def log: Logger
  }

  trait ZapAbstractSpec extends AnyFlatSpec with Matchers with ZapSpecLog {

    final def log: Logger = test.log

    implicit val text: VitalsTextCodec = VitalsTextCodec() // OK

  }


}
