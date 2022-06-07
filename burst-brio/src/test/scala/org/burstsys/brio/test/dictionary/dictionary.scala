/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test

import org.burstsys.vitals.strings._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.text.VitalsTextCodec
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

package object dictionary extends VitalsLogger {

  val randomWordList: Array[String] = (for (i <- 0 until 1e4.toInt) yield randomWord(4, 12)).toArray

  trait BrioAbstractSpec extends AnyFlatSpec with Matchers {
    VitalsLog.configureLogging("brio", true)
    implicit val text:VitalsTextCodec = VitalsTextCodec() // OK

  }

}
