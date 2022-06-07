/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.blob

/**
 * Brio scans take place against loaded brio slices
 */
trait BrioSlice extends Any {

  /**
   * the slice index identifier
   *
   * @return
   */
  def sliceKey: Int

}
