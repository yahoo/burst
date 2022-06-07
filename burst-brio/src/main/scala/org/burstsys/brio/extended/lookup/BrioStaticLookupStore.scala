/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.extended.lookup

import org.burstsys.tesla.TeslaTypes._

/**
  * This is the pressed static binary encoded form for a lookup store.
  * ===LOOKUP STORE STRUCTURE ===
  * {{{
  * ------------------------------------------------------
  * ------------------------------------------------------
  * }}}
  */
final case
class BrioStaticLookupStore(startOffset: TeslaMemoryOffset = TeslaNullOffset) extends AnyVal with BrioLookupStore {

  /** *
    *
    * @return
    */
  def lookupTable(): BrioMutableLookupTable = ???

}
