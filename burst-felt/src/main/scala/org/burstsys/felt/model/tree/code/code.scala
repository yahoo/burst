/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.tree

import org.burstsys.felt.model.FeltDeclaration
import org.burstsys.vitals.strings._

import scala.language.{implicitConversions, postfixOps}
import scala.reflect.ClassTag

package object code {

  final val FeltColumnWidth = 80

  final val FeltNoCode = ""

  type FeltCode = String

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // pretty print
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  implicit
  def cleanClassName[C](clazz: Class[C]): String = {
    val name = clazz.getName
    val clean = name.replaceAllLiterally(".package$", ".")
    clean
  }

  implicit class FeltOptionString(strings: Array[Option[String]]) {

    def optionStrings: String = {
      strings.foldRight("")(_.getOrElse("") + _)
    }

  }

  implicit class FeltCodeString(text: String) {

    def asBanner(node: FeltNode, suffix: String = null): String = {
      s"${node.nodeName}${if (suffix == null) "" else s"-$suffix"} ['${node.printSource}' -> ${node.feltType.valueTypeAsFelt}]"
    }

  }

  implicit class FeltNodeString(node: FeltNode) {
    def asBanner: String = {
      s"${node.nodeName} ['${node.printSource}' -> ${node.feltType.valueTypeAsFelt}]".asBanner(FeltColumnWidth)
    }

    def asBanner(suffix: String): String = {
      s"${node.nodeName}-$suffix ['${node.printSource}' -> ${node.feltType.valueTypeAsFelt}]".asBanner(FeltColumnWidth)
    }

  }

  /**
   * insert an indent left boundary
   *
   * @param cursor
   * @return
   */
  final def I(implicit cursor: FeltCodeCursor): FeltCode = (for (i <- 0 until cursor.indent) yield "\t").flatten.mkString

  final def I1(implicit cursor: FeltCodeCursor): FeltCode = I

  final def I2(implicit cursor: FeltCodeCursor): FeltCode = I(cursor indentRight)

  final def I3(implicit cursor: FeltCodeCursor): FeltCode = I(cursor indentRight 2)

  final def I4(implicit cursor: FeltCodeCursor): FeltCode = I(cursor indentRight 3)

  final def I5(implicit cursor: FeltCodeCursor): FeltCode = I(cursor indentRight 4)

  final def T(node: FeltNode, suffix: String = null)(implicit cursor: FeltCodeCursor): FeltCode =
    C(M(node, suffix))

  final def M(node: FeltNode, suffix: String = null)(implicit cursor: FeltCodeCursor): FeltCode =
    s"CALLER(${cursor.callScope.scopeName}) ${node.nodeName}${if (suffix == null) "" else s"-$suffix"} [${node.printSource} -> ${node.feltType.valueTypeAsFelt}]"

  final def className[T: ClassTag](implicit t: ClassTag[T]): String = {
    val name = t.runtimeClass.getName.replace("$", "").replace("package", "")
    name
  }

  final def className[T <: Any : ClassTag](o: T)(implicit t: ClassTag[T]): String = {
    val name = t.runtimeClass.getName.replace("$", "").replace("package", "")
    name
  }

  final def className(o: Class[_]): String = {
    val name = o.getName.replace("$", "").replace("package", "")
    name
  }

  /**
   * insert an indented line comment
   *
   * @param text
   * @param cursor
   * @return
   */
  final def C(text: String)(implicit cursor: FeltCodeCursor): FeltCode = glcomment(text)
  final def C0(text: String)(implicit cursor: FeltCodeCursor): FeltCode = glcomment(text)

  final def C(implicit cursor: FeltCodeCursor): FeltCode = glcomment("")

  final def CC(implicit cursor: FeltCodeCursor): FeltCode = glcomment1("")

  final def CC(text: String = "")(implicit cursor: FeltCodeCursor): FeltCode = glcomment1(text)

  final def C1(text: String = "")(implicit cursor: FeltCodeCursor): FeltCode = glcomment(text)

  final def C2(text: String = "")(implicit cursor: FeltCodeCursor): FeltCode = glcomment1(text)

  final def C3(text: String = "")(implicit cursor: FeltCodeCursor): FeltCode = glcomment2(text)

  final def C4(text: String = "")(implicit cursor: FeltCodeCursor): FeltCode = glcomment3(text)

  /**
   * managed comment in generated code
   */
  private
  final def glcomment(text: String)(implicit cursor: FeltCodeCursor): FeltCode = {
    val sep = (for (i <- 0 until (FeltColumnWidth - text.length)) yield "-").flatten.mkString
    s"""$I// -------- $text $sep""".stripMargin
  }

  /**
   * managed comment in generated code
   */
  private
  final def glcomment1(text: String)(implicit cursor: FeltCodeCursor): FeltCode = C(text)(cursor indentRight)

  private
  final def glcomment2(text: String)(implicit cursor: FeltCodeCursor): FeltCode = C(text)(cursor indentRight 2)

  private
  final def glcomment3(text: String)(implicit cursor: FeltCodeCursor): FeltCode = C(text)(cursor indentRight 3)

  /**
   * scala compliant string array
   *
   * @param strings
   * @param cursor
   * @return
   */
  final
  def generateStringArrayCode(strings: Array[String])(implicit cursor: FeltCodeCursor): FeltCode = {
    def valueCode(implicit cursor: FeltCodeCursor): FeltCode = strings.map {
      f =>
        s"""|
            |$I${f.quote}""".stripMargin
    }.mkString(", ")

    s"""|
        |${I}scala.Array[String](${valueCode(cursor indentRight)}
        |$I)""".stripMargin
  }

  /**
   * scala compliant boolean array
   *
   * @param values
   * @param cursor
   * @return
   */
  final
  def generateBooleanArrayCode(values: Array[Boolean])(implicit cursor: FeltCodeCursor): FeltCode = {
    def valueCode(implicit cursor: FeltCodeCursor): FeltCode = values.map {
      f =>
        s"""|
            |$I2$f""".stripMargin
    }.mkString(", ")

    s"""|
        |${I}scala.Array[Boolean](${valueCode(cursor indentRight)}
        |$I)""".stripMargin
  }

  /**
   * scala compliant byte array
   *
   * @param values
   * @param cursor
   * @return
   */
  final
  def generateByteArrayCode(values: Array[Byte])(implicit cursor: FeltCodeCursor): FeltCode = {
    def valueCode(implicit cursor: FeltCodeCursor): FeltCode = values.map {
      f =>
        s"""|
            |$I2$f""".stripMargin
    }.mkString(", ")

    s"""|
        |${I}scala.Array[Byte](${valueCode(cursor indentRight)}
        |$I)""".stripMargin
  }

  /**
   * scala compliant short array
   *
   * @param values
   * @param cursor
   * @return
   */
  final
  def generateShortArrayCode(values: Array[Short])(implicit cursor: FeltCodeCursor): FeltCode = {
    def valueCode(implicit cursor: FeltCodeCursor): FeltCode = values.map {
      f =>
        s"""|
            |$I2$f""".stripMargin
    }.mkString(", ")

    s"""|
        |${I}scala.Array[Short](${valueCode(cursor indentRight)}
        |$I)""".stripMargin
  }

  /**
   * scala compliant int array
   *
   * @param values
   * @param cursor
   * @return
   */
  final
  def generateIntArrayCode(values: Array[Int])(implicit cursor: FeltCodeCursor): FeltCode = {
    def valueCode(implicit cursor: FeltCodeCursor): FeltCode = values.map {
      f =>
        s"""|
            |$I$f""".stripMargin
    }.mkString(", ")

    s"""|
        |${I}scala.Array[Int](${valueCode(cursor indentRight)}
        |$I)""".stripMargin
  }

  /**
   * scala compliant long array
   *
   * @param values
   * @param cursor
   * @return
   */
  final
  def generateLongArrayCode(values: Array[Long])(implicit cursor: FeltCodeCursor): FeltCode = {
    def valueCode(implicit cursor: FeltCodeCursor): FeltCode = values.map {
      f =>
          s"""|
            |$I2$f${if (f > Int.MaxValue || f < Int.MinValue) "L" else ""}""".stripMargin
    }.mkString(", ")

    s"""|
        |${I}scala.Array[Long](${valueCode(cursor indentRight)}
        |$I)""".stripMargin
  }

  /**
   * scala compliant double array
   *
   * @param values
   * @param cursor
   * @return
   */
  final
  def generateDoubleArrayCode(values: Array[Double])(implicit cursor: FeltCodeCursor): FeltCode = {
    def valueCode(implicit cursor: FeltCodeCursor): FeltCode = values.map {
      f =>
        s"""|
            |$I2$f""".stripMargin
    }.mkString(", ")

    s"""|
        |${I}scala.Array[Double](${valueCode(cursor indentRight)}
        |$I)""".stripMargin
  }

  final
  def generateDeclarationsArrayCode[T <: FeltDeclaration](generators: Array[T])(implicit m: Manifest[T], cursor: FeltCodeCursor): String = {
    def generatorCode(implicit cursor: FeltCodeCursor): FeltCode = generators.map {
      f =>
        if (f == null)
          s"""|
              |${I}null""".stripMargin
        else f.generateDeclaration(cursor indentRight)
    }.mkString(", ")

    s"""|
        |${I}scala.Array[${m.runtimeClass.getName}](${generatorCode(cursor indentRight)}
        |$I)""".stripMargin
  }


}
