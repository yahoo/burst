/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.application

import org.burstsys.vitals.errors._

import scala.language.implicitConversions

class GenericParam[T](val raw: String, val value: Option[T])

object GenericParam {
  def parse[T](value: String, fn: String => T): GenericParam[T] = {
    var parsed: Option[T] = None
    try {
      parsed = Some(fn.apply(value))
    } catch safely {
      case _ => parsed = None
    }
    new GenericParam(value, parsed)
  }
}

case class LongParam(override val raw: String, override val value: Option[Long]) extends GenericParam[Long](raw, value)

object LongParam {
  implicit def toLongParam(p: GenericParam[Long]): LongParam = LongParam(p.raw, p.value)
  def valueOf(param: String): LongParam = {
    GenericParam.parse(param, _.toLong)
  }
}

case class IntParam(override val raw: String, override val value: Option[Int]) extends GenericParam[Int](raw, value)

object IntParam {
  implicit def toIntParam(p: GenericParam[Int]): IntParam = IntParam(p.raw, p.value)
  def valueOf(param: String): IntParam = {
    GenericParam.parse(param, _.toInt)
  }
}

case class BoolParam(override val raw: String, override val value: Option[Boolean]) extends GenericParam[Boolean](raw, value)

object BoolParam {
  implicit def toBoolParam(p: GenericParam[Boolean]): BoolParam = BoolParam(p.raw, p.value)
  def valueOf(param: String): BoolParam = {
    GenericParam.parse(param, _.toBoolean)
  }
}

case class LongArrayParam(override val raw: String, override val value: Option[Array[Long]]) extends GenericParam[Array[Long]](raw, value)

object LongArrayParam {
  implicit def toLongArrayParam(p: GenericParam[Array[Long]]): LongArrayParam = LongArrayParam(p.raw, p.value)
  def valueOf(param: String): LongArrayParam = {
    GenericParam.parse(param, _.split(",").map(_.toLong))
  }
}
