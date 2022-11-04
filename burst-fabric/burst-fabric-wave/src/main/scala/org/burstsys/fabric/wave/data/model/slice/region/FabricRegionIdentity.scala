/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.slice.region

import java.nio.file.Path

import org.burstsys.fabric.wave.data.model.slice.data.FabricSliceData

/**
 * information associate with a cache region's identity
 */
trait FabricRegionIdentity extends Any {

  /**
   * The region index within the slice
   *
   * @return
   */
  def regionIndex: Int

  /**
   * The assigned region folder/spindle
   *
   * @return
   */
  def regionTag: FabricRegionTag

  /**
   * the file associated with this region
   *
   * @return
   */
  def filePath: Path

}
