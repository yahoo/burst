/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.extended.lookup

import org.burstsys.tesla.TeslaTypes._

/**
  * This is the pressed static binary encoded form for a lookup store.
  * ===LOOKUPS STRUCTURE===
  * {{{
  * ------------------------------------------------------
  * | INTEGER         | count of lookup tables
  * | ARRAY[INTEGER]  | lookup table offsets
  * | ARRAY[LONG]     | lookup table slots
  * ------------------------------------------------------
  * }}}
  *
  * @param startOffset
  */
final case
class BrioStaticLookupTable(startOffset: TeslaMemoryOffset = TeslaNullOffset) extends AnyVal with BrioLookupTable {

}
