/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.mutables

import org.burstsys.felt.binding.FeltBinding
import org.burstsys.felt.model.collectors.FeltCollectorProviders
import org.burstsys.felt.model.collectors.cube.FeltCubeProvider
import org.burstsys.felt.model.collectors.route.FeltRouteProvider
import org.burstsys.felt.model.collectors.shrub.FeltShrubProvider
import org.burstsys.felt.model.collectors.tablet.FeltTabletProvider
import org.burstsys.felt.model.mutables.valarr.FeltMutableValArrProv
import org.burstsys.felt.model.mutables.valmap.FeltMutableValMapProv
import org.burstsys.felt.model.mutables.valset.FeltMutableValSetProv
import org.burstsys.felt.model.sweep.runtime.FeltRuntimeComponent

/**
 * a helper class to allow indirection
 * to a [[FeltBinding]] for a [[FeltMutableProvider]]
 */
trait FeltBindingRuntime extends Any with FeltRuntimeComponent
  with FeltMutableProviders with FeltCollectorProviders {

  /**
   * the finding to delegate to
   *
   * @return
   */
  def binding: FeltBinding

  final override
  def valarr: FeltMutableValArrProv = binding.mutables.valarr

  final override
  def valset: FeltMutableValSetProv = binding.mutables.valset

  final override
  def valmap: FeltMutableValMapProv = binding.mutables.valmap

  final override
  def cubes: FeltCubeProvider = binding.collectors.cubes

  final override
  def routes: FeltRouteProvider = binding.collectors.routes

  final override
  def tablets: FeltTabletProvider = binding.collectors.tablets

  final override
  def shrubs: FeltShrubProvider = binding.collectors.shrubs

}
