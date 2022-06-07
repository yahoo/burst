/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.runtime

import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.felt.model.collectors.runtime.FeltCollectorBuilder
import org.burstsys.felt.model.sweep.FeltSweepComponent

/**
 * The per blob traversal runtime data passed to the top level apply
 */
trait FeltCubeSweep extends Any with FeltSweepComponent with FeltCubeFactory {

  /**
   * an array of cube builders (one per frame) indexed by frame id
   * note that all non cube frames will have a null in their position
   *
   * @return
   */
  def collectorBuilders: Array[_ <: FeltCollectorBuilder]

  final override def grabCollector(builder: FeltCubeBuilder): FeltCubeCollector =
    feltBinding.collectors.cubes.grabCollector(builder)

  final override def releaseCollector(collector: FeltCubeCollector): Unit =
    feltBinding.collectors.cubes.releaseCollector(collector)
}
