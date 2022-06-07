/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.extended.lookup

import org.burstsys.brio.blob.BrioBlob
import org.burstsys.brio.model.schema.encoding.BrioValueEncoding
import org.burstsys.brio.types.BrioTypes.{Elastic, Lookup}
import org.burstsys.tesla.TeslaTypes._

import scala.language.implicitConversions

/**
  * zero cost wrapper for lookup type data
  *
  * @param startOffset
  */
final case
class BrioLookup(startOffset: TeslaMemoryOffset = TeslaNullOffset) extends AnyVal {

  def encode(encoding: BrioValueEncoding, blob: BrioBlob, value: Elastic): Unit = {
    ???
  }

  def decode(encoding: BrioValueEncoding): Lookup = {
    ???
  }

}
