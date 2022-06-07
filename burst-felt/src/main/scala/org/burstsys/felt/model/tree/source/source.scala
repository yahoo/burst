/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.tree

import org.burstsys.vitals.strings._

package object source {

  final val columnWidth = 100

  final def S3(implicit index: Int): String = sindent(index + 2)

  final def S2(implicit index: Int): String = sindent(index + 1)

  final def S1(implicit index: Int): String = sindent(index)

  final def S(implicit index: Int): String = sindent

  final def SL[SG <: FeltSource](members: Array[SG])(implicit index: Int): String =
    if (members.isEmpty) "" else s"\n${members.map(_.normalizedSource(index + 1).singleLineEnd).stringify.trimAtEnd}"

  /**
   * support for pretty print indenting
   *
   * @param index
   * @return
   */
  final def sindent(implicit index: Int): String = (for (i <- 0 until index) yield "\t").stringify

  final
  def generateStringArray(strings: Array[String])(implicit index: Int): String = {
    s"${sindent}scala.Array[String](${
      strings.map {
        f => f.quote.withCommaSpace
      }.stringify.stripCommaSpace
    })"
  }


  final
  def generateIntArray(values: Array[Int])(implicit index: Int): String = {
    s"${sindent}scala.Array[Int](${
      values.map {
        f => f.toString.withCommaSpace
      }.stringify.stripCommaSpace
    })"
  }


  final
  def generateLongArray(values: Array[Long])(implicit index: Int): String = {
    s"${sindent}scala.Array[Long](${
      values.map {
        f => f.toString.withCommaSpace
      }.stringify.stripCommaSpace
    })"
  }


  final
  def generateDoubleArray(values: Array[Double])(implicit index: Int): String = {
    s"${sindent}scala.Array[Double](${
      values.map {
        f => f.toString.withCommaSpace
      }.stringify.stripCommaSpace
    })"
  }


  final
  def generateGeneratorArray[T <: FeltSource](generators: Array[T])(implicit m: Manifest[T], index: Int): String = {
    s"""${sindent}scala.Array[${m.runtimeClass.getName}](
       |${
      generators.map {
        f =>
          if (f == null)
            s"$S null".withCommaSpace.withLineEnd
          else
            f.normalizedSource(index + 1).withCommaSpace.withLineEnd
      }.stringify.stripLineEnd.stripCommaSpace
    }
       |$sindent)""".stripMargin
  }

}
