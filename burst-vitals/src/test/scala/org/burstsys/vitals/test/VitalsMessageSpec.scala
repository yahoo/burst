/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test

import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._

class VitalsMessageSpec extends VitalsAbstractSpec {

  private def messageFormatFor(file: String): String = """BURST_VITALS_TEST: .+ at """ + file + """.scala:(\d+)"""

  private val messageFormat = messageFormatFor("VitalsMessageSpec")

  private val testPackageMessageFormat = messageFormatFor("test")

  behavior of "burstStdMsg"

  it should "provide a file and line number" in {
    val ex = LocatedValue(VitalsException("tests stuff"))
    ex.value.getMessage should fullyMatch regex (messageFormat withGroup s"${ex.line}")

    foobar()

    val t = new RuntimeException("goodbye").fillInStackTrace()

    val msg = LocatedValue(burstStdMsg("hello", t))
    msg.value should startWith regex (messageFormat withGroup s"${msg.line}")
  }


  private def foobar(): Unit = {
    val foo = LocatedValue(burstStdMsg("hello"))
    foo.value should fullyMatch regex (messageFormat withGroup s"${foo.line}")
  }

  it should "find correct trace" in {
    def foo(): Unit = {
      val s1 = LocatedValue(burstStdMsg("hello"))
      s1.value should startWith regex (messageFormat withGroup s"${s1.line}")
      s1.value should include("hello")

      val s2 = LocatedValue(burstStdMsg("hello", new RuntimeException("goodbye").fillInStackTrace()))
      s2.value should startWith regex (messageFormat withGroup s"${s2.line}")
      s2.value should include("hello: goodbye")

      val s3 = LocatedValue(burstStdMsg(new RuntimeException("goodbye").fillInStackTrace()))
      s3.value should startWith regex (messageFormat withGroup s"${s3.line}")
      s3.value should include("goodbye")
    }

    foo()

    val s1 = LocatedValue(burstStdMsg("hello"))
    s1.value should fullyMatch regex (messageFormat withGroup s"${s1.line}")
    s1.value should include("hello")

    val msg = messageWithLine()
    msg should fullyMatch regex testPackageMessageFormat
    msg should include("Message line")

    var ex: Throwable = null
    try {
      exceptionLocation()
      fail("Expected exception")
    } catch safely {
      case t =>
        ex = t
    }
    ex.getMessage should fullyMatch regex testPackageMessageFormat
  }

  def message(): String = burstLocMsg("From here")

  case class InteriorCaseClass() {
    def message(): String = burstLocMsg("Interior case class")
  }

  it should "provide the location when requested" in {
    val msg = message()
    msg should fullyMatch regex messageFormat
    msg should include("From here")
    msg should include(" VitalsMessageSpec.message ")

    val int = InteriorCaseClass().message()
    int should fullyMatch regex messageFormat
    int should include("Interior case class")
    int should include("InteriorCaseClass.message")

    val ext = ExteriorCaseClass().message()
    ext should fullyMatch regex messageFormat
    ext should include("case class")
    ext should include("ExteriorCaseClass.message")

    val pkg = messageWithLocation()
    pkg should fullyMatch regex testPackageMessageFormat
    pkg should include("Message with location")
    pkg should include("test.package.messageWithLocation")

    val pkgCls = PackageCaseClass().message()
    pkgCls should fullyMatch regex testPackageMessageFormat
    pkgCls should include("case class")
    pkgCls should include("test.package.PackageCaseClass.message")
  }
}

case class ExteriorCaseClass() {
  def message(): String = burstLocMsg("Exterior case class")
}

case class LocatedValue[T](value: T)(implicit val ln: sourcecode.Line) {
  def line: Int = ln.value
}
