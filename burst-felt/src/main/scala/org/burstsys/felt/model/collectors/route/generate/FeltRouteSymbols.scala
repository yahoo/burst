/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.generate

/**
 *
 */
trait FeltRouteSymbols {

  final private val feltRouteBinding = "feltBinding.collectors.routes"

  final lazy val grabRouteMethod: String = s"$feltRouteBinding.grabCollector"

  final lazy val releaseRouteMethod: String = s"$feltRouteBinding.releaseCollector"

  final
  def routeBuilderVariable(frameName: String) = s"route_${frameName}_builder"

  final
  def routeRootVariable(frameName: String) = s"route_${frameName}_root"

  final
  def routeInstanceVariable(frameName: String) = s"route_${frameName}_instance"

  final
  def routeVisitPathOrdinalVariable(frameName: String) = s"route_${frameName}_path_ordinal"

  final
  def routeVisitStepOrdinalVariable(frameName: String) = s"route_${frameName}_step_ordinal"

  final
  def routeVisitStepKeyVariable(frameName: String) = s"route_${frameName}_step_key"

  final
  def routeVisitStepTagVariable(frameName: String) = s"route_${frameName}_step_tag"

  final
  def routeVisitStepTimeVariable(frameName: String) = s"route_${frameName}_step_time"

  final
  def routeVisitStepIsFirstVariable(frameName: String) = s"route_${frameName}_step_is_first"

  final
  def routeVisitStepIsLastVariable(frameName: String) = s"route_${frameName}_step_is_last"

  final
  def routeVisitPathIsFirstVariable(frameName: String) = s"route_${frameName}_path_is_first"

  final
  def routeVisitPathIsLastVariable(frameName: String) = s"route_${frameName}_path_is_last"

}
