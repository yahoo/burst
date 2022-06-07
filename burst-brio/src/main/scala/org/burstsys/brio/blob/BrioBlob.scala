/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.blob

import org.burstsys.brio.dictionary.BrioDictionary
import org.burstsys.brio.lattice.BrioLatticeReference
import org.burstsys.brio.extended.BrioBlobV3
import org.burstsys.tesla.buffer.TeslaBufferReader

// TODO EXTENDED TYPES
/**
 * ==BLOB ENCODING FORMATS==
 * This is all the information needed by the system to store the data for a pressed brio object tree
 * avec dictionary. There is a special binary encoding for these that can evolve through versioning
 *
 * ===V1 BLOB ENCODING FORMAT===
 * obsolete encoding that had original brio dictionaries based on serialized hashmaps (yuck!)
 *
 * ===V2 BLOB ENCODING FORMAT===
 * {{{
 * ------------------------------------------------------
 * | INTEGER     | blob encoding version (2)
 * | INTEGER     | root object schema version
 * | INTEGER     | size in bytes of dictionary header
 * | ARRAY[BYTE] | dictionary content
 * | INTEGER     | size in bytes of pressed object tree
 * | ARRAY[BYTE] | pressed root object tree
 * ------------------------------------------------------
 * }}}
 *
 * BE SURE THIS IS A UNIVERSAL TRAIT (extends ANY)
 **/
trait BrioBlob extends Any with BrioBlobV3 {

  /**
   * The brio encoded object tree data
   */
  def data: TeslaBufferReader

  /**
   * The string dictionary for this blob
   */
  def dictionary: BrioDictionary

  /**
   * The root reference
   */
  def reference: BrioLatticeReference

  /**
   * The byte size of this blob
   */
  def size: Long

  /**
   * Indicates if this blob is an empty blob
   */
  @inline
  def isEmpty: Boolean = false

  /**
   * operation to release resources for this blob
   */
  def close: BrioBlob
}


object BrioBlob {

  // a contiguous memory region dedicated to a single coil/thread
  type BrioRegionIterator = Iterator[BrioBlob]

  sealed abstract class SingleIterator[T] extends Iterator[T] {
    def result: T

    @transient var done = false

    override def hasNext: Boolean = !done

    override def next(): T = {
      done = true
      result
    }
  }

  final
  def emptyRegionIterator(sliceKey: Int): BrioRegionIterator = new SingleIterator[BrioBlob] {
    override def result: BrioBlob = BrioEmptyBlob(sliceKey)
  }

}
