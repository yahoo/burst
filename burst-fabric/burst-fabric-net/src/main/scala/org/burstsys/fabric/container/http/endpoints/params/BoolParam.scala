/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http.endpoints.params

import scala.language.implicitConversions

case class BoolParam(override val raw: String, override val value: Option[Boolean]) extends GenericParam[Boolean](raw, value)

object BoolParam {
  implicit def toBoolParam(p: GenericParam[Boolean]): BoolParam = BoolParam(p.raw, p.value)

  def valueOf(param: String): BoolParam = {
    GenericParam.parse(param, _.toBoolean)
  }
}
