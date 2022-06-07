/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.extended.elastic

import org.burstsys.brio.model.schema.encoding.BrioValueEncoding
import org.burstsys.brio.types.BrioTypes.Elastic
import org.burstsys.tesla.TeslaTypes._

import scala.language.implicitConversions

final case
class BrioElastic(startOffset: TeslaMemoryOffset = TeslaNullOffset) extends AnyVal {
  // encoding and decoding routines

  def encode(encoding: BrioValueEncoding, value: Elastic): Unit = {
    ???
  }

  def decode(encoding: BrioValueEncoding): Elastic = {
    ???
  }

}

