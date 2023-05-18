/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http.endpoints.params

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

