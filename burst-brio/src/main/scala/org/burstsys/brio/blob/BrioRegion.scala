/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.blob

import org.burstsys.brio.blob.BrioBlob.BrioRegionIterator

/**
 * a set of contiguous brio blobs that can be scanned
 */
trait BrioRegion extends Any {

  /**
   * The final readable sequence of Brio Blobs
   *
   * @return
   */
  def iterator: BrioRegionIterator

}
