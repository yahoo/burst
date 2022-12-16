/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http.endpoints.params

import scala.language.implicitConversions

case class LongArrayParam(override val raw: String, override val value: Option[Array[Long]]) extends GenericParam[Array[Long]](raw, value)

object LongArrayParam {
  implicit def toLongArrayParam(p: GenericParam[Array[Long]]): LongArrayParam = LongArrayParam(p.raw, p.value)

  def valueOf(param: String): LongArrayParam = {
    GenericParam.parse(param, _.split(",").map(_.toLong))
  }
}
