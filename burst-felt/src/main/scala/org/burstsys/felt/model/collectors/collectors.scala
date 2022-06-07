/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model

import org.burstsys.felt.model.collectors.cube.FeltCubeProvider
import org.burstsys.felt.model.collectors.cube.generate.FeltCubeSymbols
import org.burstsys.felt.model.collectors.cube.runtime.{FeltCubeRuntime, FeltCubeSweep}
import org.burstsys.felt.model.collectors.dictionary.{FeltDictRuntime, FeltDictSweep}
import org.burstsys.felt.model.collectors.route.FeltRouteProvider
import org.burstsys.felt.model.collectors.route.generate.FeltRouteSweep
import org.burstsys.felt.model.collectors.route.runtime.FeltRouteRuntime
import org.burstsys.felt.model.collectors.runtime.{FeltCollector, FeltCollectorBuilder}
import org.burstsys.felt.model.collectors.shrub.FeltShrubProvider
import org.burstsys.felt.model.collectors.tablet.FeltTabletProvider
import org.burstsys.felt.model.collectors.tablet.generate.FeltTabletSweep
import org.burstsys.felt.model.collectors.tablet.runtime.FeltTabletRuntime
import org.burstsys.felt.model.reference.FeltReference
import org.burstsys.felt.model.sweep.FeltSweepComponent
import org.burstsys.felt.model.sweep.runtime.FeltRuntimeComponent
import org.burstsys.felt.model.tree.code._

/**
 * =collectors=
 * collectors are used to 'collect' data during traversals. Generally speaking the Felt tree orchestrates the movement
 * of data from the 'lattice' or the input dataset and collectors which are used to determine the results of the
 * Felt analysis.
 */
package object collectors {

  trait FeltCollectorSymbols extends AnyRef with FeltCubeSymbols {
    val collectorBuilderClass: Class[FeltCollectorBuilder] = classOf[FeltCollectorBuilder]
    val collectorBuilderClassName: String = collectorBuilderClass

    val collectorClass: Class[FeltCollector] = classOf[FeltCollector]
    val collectorClassName: String = classOf[FeltCollector]
  }

  trait FeltCollectorRef extends FeltReference {
    def collectorTag: String = nameSpace.absoluteNameSansRoot.stripPrefix(global.analysis.analysisName).stripPrefix(".")
  }

  /**
   * Sweep runtime code generated routines for collectors
   */
  trait FeltCollectorRuntime extends Any with FeltRuntimeComponent
    with FeltDictRuntime with FeltCubeRuntime with FeltRouteRuntime with FeltTabletRuntime {

    /**
     * write a collector into a frame/plane at runtime
     *
     * @param frameId
     * @param collector
     */
    def frameCollector(frameId: Int, collector: FeltCollector): Unit

    /**
     * access a collector from a frame/plane at runtime
     *
     * @param frameId
     * @return
     */
    def frameCollector(frameId: Int): FeltCollector

  }

  /**
   * Sweep code generated routines for collectors
   */
  trait FeltCollectorSweep extends Any with FeltSweepComponent
    with FeltDictSweep with FeltCubeSweep with FeltRouteSweep with FeltTabletSweep

  /**
   * the runtime library that supports Felt collectors
   */
  trait FeltCollectorProviders extends Any {

    def cubes: FeltCubeProvider

    def routes: FeltRouteProvider

    def tablets: FeltTabletProvider

    def shrubs: FeltShrubProvider

  }

}
