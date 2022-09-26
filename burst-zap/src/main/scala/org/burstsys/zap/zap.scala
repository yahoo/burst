/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys

import org.burstsys.felt.model.collectors.FeltCollectorProviders
import org.burstsys.felt.model.collectors.cube.FeltCubeProvider
import org.burstsys.felt.model.collectors.shrub.FeltShrubProvider
import org.burstsys.felt.model.collectors.tablet.FeltTabletProvider
import org.burstsys.felt.model.mutables.FeltMutableProviders
import org.burstsys.felt.model.mutables.valarr.FeltMutableValArrProv
import org.burstsys.felt.model.mutables.valmap.FeltMutableValMapProv
import org.burstsys.felt.model.mutables.valset.FeltMutableValSetProv
import org.burstsys.vitals.reporter.{VitalsReporter, VitalsReporterSource}
import org.burstsys.zap.cube.ZapCubeReporter
import org.burstsys.zap.cube2.ZapCube2Provider
import org.burstsys.zap.mutable.valarray.ZapValArrProvider
import org.burstsys.zap.mutable.valmap.ZapValMapProvider
import org.burstsys.zap.mutable.valset.ZapValSetProvider
import org.burstsys.zap.route.{ZapRouteProvider, ZapRouteReporter}
import org.burstsys.zap.shrub.ZapShrubProvider
import org.burstsys.zap.tablet.{ZapTabletProvider, ZapTabletReporter}

package object zap extends VitalsReporterSource {

  override
  def reporters: Array[VitalsReporter] = Array(
    ZapCubeReporter,
    ZapRouteReporter,
    ZapTabletReporter
  )

  /**
   * bring the ZAP support for FELT all together in these bindings
   */
  object ZapCollectorProviders extends FeltCollectorProviders {

    override def cubes: FeltCubeProvider = ZapCube2Provider()

    override def routes: ZapRouteProvider = ZapRouteProvider()

    override def tablets: FeltTabletProvider = ZapTabletProvider()

    override def shrubs: FeltShrubProvider = ZapShrubProvider()
  }

  /**
   * provide a set of zap mutables to a FELT language instance. This API allows the
   * implementation to be in a higher level supervisor than FELT.
   */
  object ZapMutableProviders extends FeltMutableProviders {
    override def valarr: FeltMutableValArrProv = ZapValArrProvider()

    override def valset: FeltMutableValSetProv = ZapValSetProvider()

    override def valmap: FeltMutableValMapProv = ZapValMapProvider()
  }
}
