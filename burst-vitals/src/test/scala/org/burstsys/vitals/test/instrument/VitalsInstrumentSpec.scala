/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test.instrument

import org.burstsys.vitals.instrument._
import org.burstsys.vitals.test.VitalsAbstractSpec


class VitalsInstrumentSpec extends VitalsAbstractSpec {

  behavior of "prettyByteSizeString"

  Array(
    ByteSizeCase(-1, "not supported/unknown"),
    ByteSizeCase(0, "0.0B"),
    ByteSizeCase(2.4 * KB, "2.4KB"),
    ByteSizeCase(56.2 * MB, "56.2MB"),
    ByteSizeCase(MB, "1.0MB"),
    ByteSizeCase(GB, "1.0GB"),
    ByteSizeCase(2.7 * GB, "2.7GB"),
    ByteSizeCase(1 * TB, "1.0TB"),
    ByteSizeCase(127.5 * TB, "127.5TB"),
    ByteSizeCase(6658 * TB, "6,658.0TB")
  ) foreach { c =>
    it should s"format ${c.bytes}" in {
      prettyByteSizeString(c.bytes.toLong) should equal(c.prettied)
      prettyByteSizeString(c.bytes) should equal(c.prettied)
    }
  }

  behavior of "prettyFixedNumber"

  Array(
    PrettyFixedCase(-1, "-1"),
    PrettyFixedCase(0, "0"),
    PrettyFixedCase(1, "1"),
    PrettyFixedCase(1E3.toLong, "1,000"),
    PrettyFixedCase(1E6.toLong, "1,000,000"),
    PrettyFixedCase(1E9.toLong, "1,000,000,000"),
    PrettyFixedCase(1E12.toLong, "1,000,000,000,000"),
  ) foreach { c =>
    it should s"format ${c.long}" in {
      prettyFixedNumber(c.long) should equal(c.prettied)
    }
  }

  behavior of "prettyFloatNumber"

  Array(
    PrettyFloatCase(-1.0, "-1.00"),
    PrettyFloatCase(0.0, "0.00"),
    PrettyFloatCase(1.0, "1.00"),
    PrettyFloatCase(1E3, "1000.00"),
    PrettyFloatCase(1E6, "1000000.00"),
    PrettyFloatCase(1E9, "1000000000.00"),
    PrettyFloatCase(1E12, "1000000000000.00"),
  ) foreach { c =>
    it should s"format ${c.double}" in {
      prettyFloatNumber(c.double) should equal(c.prettied)
    }
  }

  behavior of "prettySizeString"

  Array(
    PrettySizeCase(0, "0.0"),
    PrettySizeCase(1, "1.0"),
    PrettySizeCase(1E3, "1.0K"),
    PrettySizeCase(1E6, "1.0M"),
    PrettySizeCase(1E9, "1.0G"),
    PrettySizeCase(1E12, "1.0T"),
    PrettySizeCase(1E13, "10.0T"),
    PrettySizeCase(1E15, "1,000.0T"),
  ) foreach { c =>
    it should s"format ${c.size}" in {
      prettySizeString(c.size.toLong) should equal(c.prettied)
      prettySizeString(c.size) should equal(c.prettied)
    }
  }
}

final case class ByteSizeCase(bytes: Double, prettied: String)

final case class PrettyFixedCase(long: Long, prettied: String)

final case class PrettyFloatCase(double: Double, prettied: String)

final case class PrettySizeCase(size: Double, prettied: String)
