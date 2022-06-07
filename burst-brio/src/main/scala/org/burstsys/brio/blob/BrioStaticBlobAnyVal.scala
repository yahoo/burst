/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.blob

import org.burstsys.tesla.TeslaTypes._

/**
 * A Blob based on a static mem buffer. The Static term means the associated data structures are
 * immutable. This is presumed to be allocated from an mmap region loaded from the ache.
 *
 * @param ptr
 */
final case
class BrioStaticBlobAnyVal(ptr: TeslaMemoryPtr) extends AnyVal with BrioAbstractBlob {

  @inline override
  def dataPtr: TeslaMemoryPtr = ptr

  override def close: BrioBlob = {
    // static blobs are not closed per se - the mmap region they are part of gets closed
    this
  }

}

