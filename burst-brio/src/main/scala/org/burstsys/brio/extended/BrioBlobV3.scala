/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.extended

import org.burstsys.brio.extended.elastic.BrioStaticOffsetTable
import org.burstsys.brio.extended.lookup.BrioMutableLookupStore

/**
 * ===V3 BLOB ENCODING FORMAT===
 * Addition of extended type support.
 * {{{
 * ------------------------------------------------------
 * | INTEGER     | blob encoding version (3)
 * | INTEGER     | root object schema version
 *
 * | INTEGER     | size in bytes of lookup table
 * | ARRAY[BYTE] | lookup table data
 *
 * | INTEGER     | size in bytes of offset table
 * | ARRAY[BYTE] | offset table data
 *
 * | INTEGER     | size in bytes of dictionary
 * | ARRAY[BYTE] | dictionary data
 *
 * | INTEGER     | size in bytes of object tree
 * | ARRAY[BYTE] | object tree data
 *
 * }}}
 * BE SURE THIS IS A UNIVERSAL TRAIT (extends ANY)
 */
trait BrioBlobV3 extends Any {

  /**
   * Offset table used for [[org.burstsys.brio.types.BrioTypes.Elastic]] Types
   *
   * @return
   */
  def offsetTable: BrioStaticOffsetTable = ???

  /**
   * Store for Lookup tables used for [[org.burstsys.brio.types.BrioTypes.Lookup]] Types
   *
   * @return
   */
  def lookupStore: BrioMutableLookupStore = ???

}
