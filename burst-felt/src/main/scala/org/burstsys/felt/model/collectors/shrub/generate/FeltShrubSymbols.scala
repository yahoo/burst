/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.shrub.generate

trait FeltShrubSymbols {

  final private val feltBinding = "feltBinding.collectors.shrubs"

  final lazy val grabMethod: String = s"$feltBinding.grabCollector"

  final lazy val releaseMethod: String = s"$feltBinding.releaseCollector"

  final
  def shrubBuilderVariable(collectorName: String) = s"shrub_${collectorName}_builder"

  final
  def shrubRootVariable(collectorName: String) = s"shrub_${collectorName}_root"

  final
  def shrubInstanceVariable(collectorName: String) = s"shrub_${collectorName}_instance"

}
