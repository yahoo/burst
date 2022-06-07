/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.extended.elastic

import org.burstsys.tesla.TeslaTypes._

/**
  * ===OFFSETS STRUCTURE===
  * Each offset is a a LONG value for a given relation.
  * Note this is a fixed number of offsets for a given schema i.e. the size of this structure is the same
  * for each pressed [[org.burstsys.brio.blob.BrioBlob]].
  * {{{
  * ------------------------------------------------------
  * | INTEGER         | count of offset values
  * | ARRAY[LONG]     | OFFSET VALUES
  * ------------------------------------------------------
  * }}}
  *
  * @param startOffset
  */
final case
class BrioStaticOffsetTable(startOffset: TeslaMemoryOffset = TeslaNullOffset) extends AnyVal with BrioOffsetTable {

}
