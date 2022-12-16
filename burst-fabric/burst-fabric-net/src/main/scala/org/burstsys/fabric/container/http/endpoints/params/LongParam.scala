/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http.endpoints.params

import scala.language.implicitConversions

case class LongParam(override val raw: String, override val value: Option[Long]) extends GenericParam[Long](raw, value)

object LongParam {
  implicit def toLongParam(p: GenericParam[Long]): LongParam = LongParam(p.raw, p.value)

  def valueOf(param: String): LongParam = {
    GenericParam.parse(param, _.toLong)
  }
}
