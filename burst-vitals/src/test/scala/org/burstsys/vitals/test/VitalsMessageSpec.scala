/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test

import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.logging._

class VitalsMessageSpec extends VitalsAbstractSpec {

  private val burstMessageFormat = """BURST_VITALS_TEST: .+ at VitalsMessageSpec.scala:(\d+) on .+ \(\d+(?:\.\d+){3}\)"""

  behavior of "burstStdMsg"

  it should "provide a file and line number" in {
    val be = (VitalsException("tests stuff"), implicitly[sourcecode.Line])
    val m1 = be._1.getMessage
    m1 should fullyMatch regex (burstMessageFormat withGroup s"${be._2.value}")

    foobar()

    val t = (new RuntimeException("goodbye").fillInStackTrace(), implicitly[sourcecode.Line])

    val bar = burstStdMsg("hello", t._1)
    bar should fullyMatch regex (burstMessageFormat withGroup s"${t._2.value}")
  }


  private def foobar(): Unit = {
    val foo = (burstStdMsg("hello"), implicitly[sourcecode.Line])
    foo._1 should fullyMatch regex (burstMessageFormat withGroup s"${foo._2.value}")
  }

  it should "find correct trace" in {
    def foo(): Unit = {
      val s1 = (burstStdMsg("hello"), implicitly[sourcecode.Line])
      s1._1 should fullyMatch regex (burstMessageFormat withGroup s"${s1._2.value}")
      s1._1 should include("hello")

      val s2 = (burstStdMsg("hello", new RuntimeException("goodbye").fillInStackTrace()), implicitly[sourcecode.Line])
      s2._1 should fullyMatch regex (burstMessageFormat withGroup s"${s2._2.value}")
      s2._1 should include("hello: goodbye")

      val s3 = (burstStdMsg(new RuntimeException("goodbye").fillInStackTrace()), implicitly[sourcecode.Line])
      s3._1 should fullyMatch regex (burstMessageFormat withGroup s"${s3._2.value}")
      s3._1 should include("goodbye")
    }

    foo()

    val s1 = (burstStdMsg("hello"), implicitly[sourcecode.Line])
    s1._1 should fullyMatch regex (burstMessageFormat withGroup s"${s1._2.value}")
    s1._1 should include("hello")

  }


}
