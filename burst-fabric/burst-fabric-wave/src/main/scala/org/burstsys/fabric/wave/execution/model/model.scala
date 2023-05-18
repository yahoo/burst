/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution

package object model {


  /**
   * there are three merge steps in the merge hierarchy (region, slice, wave) the first two are on the worker,
   * the last one on the supervisor.
   * @param code
   */
  sealed case class FabricMergeLevel(code: Int) {
    override def toString: String = this.getClass.getSimpleName.stripSuffix("$")
  }

  object FabricRegionMergeLevel extends FabricMergeLevel(1)

  object FabricSliceMergeLevel extends FabricMergeLevel(2)

  object FabricWaveMergeLevel extends FabricMergeLevel(3)

}
