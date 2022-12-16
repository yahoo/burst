/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http.endpoints.params

import scala.language.implicitConversions

case class IntParam(override val raw: String, override val value: Option[Int]) extends GenericParam[Int](raw, value)

object IntParam {
  implicit def toIntParam(p: GenericParam[Int]): IntParam = IntParam(p.raw, p.value)

  def valueOf(param: String): IntParam = {
    GenericParam.parse(param, _.toInt)
  }
}
