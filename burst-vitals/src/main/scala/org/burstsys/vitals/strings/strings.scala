/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import scala.collection.GenTraversable
import scala.util.Random

package object strings {

  def extractStringLiteral(str: String): String = {
    str.
      stripPrefix("\"").stripSuffix("\"").
      stripPrefix("\'").stripSuffix("\'").
      replace("\"\"", "\\\"").
      replace("\t", "\\t").
      replace("\n", "\\n").
      replace("\r", "\\r")
  }

  def stripQuotes(str: String): String = {
    str.stripPrefix("\"").stripSuffix("\"").stripPrefix("\'").stripSuffix("\'")
  }


  def spaces(width: Int): String = " " * width

  implicit class VitalsGeneratingTraversable(strings: GenTraversable[String]) {

    final def stringify: String = strings.foldRight("")(_ + _)

    final def noNulls: GenTraversable[String] = strings.filter(_ != null)

  }

  implicit class VitalsGeneratingSeq(strings: IndexedSeq[String]) {

    final def noNulls: IndexedSeq[String] = strings.filter(_ != null)

  }

  implicit class VitalsGeneratingArray(strings: Array[String]) {

    final def stringify: String = strings.foldRight("")(_ + _)

    final def noNulls: Array[String] = strings.filter(_ != null)
  }

  implicit class VitalsString(string: String) {

    final def camelCaseToUnderscore: String = {
      var first = true
      var twoInRow = false
      var lastC = '\u0000'

      def isTwoInRow(c: Char): Unit = {
        twoInRow = if (c.isUpper) true else false
        lastC = c
      }

      string.flatMap {
        c =>
          if (!first && !twoInRow && c.isUpper) {
            first = false
            isTwoInRow(c)
            s"_${c.toLower}"
          } else {
            first = false
            isTwoInRow(c)
            c.toLower.toString
          }
      }.replace(".", "_").replace("__", "_")
    }

    final def asBanner(sym: String, width: Int): String = {
      val sep1 = (for (i <- 0 until (10)) yield sym).flatten.mkString
      val sep2 = (for (i <- 0 until (width - string.length)) yield sym).flatten.mkString
      s"""$sep1 $string $sep2""".stripMargin
    }

    final def asBanner: String = asBanner("-", 40)

    final def asBanner(width: Int = 40): String = asBanner("-", width)

    final def withDoubleQuotes: String = s""""$string""""

    final def withSingleQuotes: String = s"'$string'"

    final def stripCommaSpace: String = string.stripSuffix(", ")

    final def withCommaSpace: String = s"$string, "

    final def withLineEnd: String = s"$string \n"

    final def quote: String = s""""$string""""

    final def noMultipleLineEndings: String = string.replaceAll("""(?m)\s+$""", "")

    final def trimAtBegin: String = {
      if (string.trim.isEmpty) return ""
      for (i <- 0 until string.length) {
        if (!string.charAt(i).isWhitespace)
          return string.substring(i)
      }
      string
    }

    final def trimAtEnd: String = {
      if (string.trim.isEmpty) return ""
      for (i <- string.length - 1 to 0 by -1) {
        val c = string.charAt(i)
        if (!c.isWhitespace)
          return string.substring(0, i + 1)
      }
      string
    }

    final def stripEmptyLines: String = {
      this.string.replaceAll("[\\\r\\\n]+", "\n")
    }

    final def condensed: String = {
      this.string.trim.replaceAll("[\\\r\\\n]+", " ").replaceAll(" +", " ")
    }

    final def noMultiSpace: String = {
      this.string.replaceAll("\\s+", " ")
    }

    final def padded(padding: Int): String = {
      string + (for (i <- 0 until (padding - string.length)) yield " ").flatten.mkString
    }


    final def singleLineEnd: String = string.trimAtEnd.withLineEnd

    final def doubleLineEnd: String = string.trimAtEnd.withLineEnd.withLineEnd

    def initialCase: String = {
      if (string.nonEmpty)
        string.charAt(0).toUpper + string.substring(1)
      else string
    }
  }

  final
  val letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

  def randomWord(minSize: Int, maxSize: Int): String = {
    val builder = new StringBuilder
    val size = (math.abs(new Random().nextInt() % 10000) % (maxSize - minSize)) + minSize
    for (i <- 0 to size) {
      val index = math.abs(new Random().nextInt() % 10000) % letters.length
      builder += letters(index)
    }
    builder.toString()
  }

}
