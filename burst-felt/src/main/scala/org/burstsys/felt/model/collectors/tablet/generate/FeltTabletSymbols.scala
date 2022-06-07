/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet.generate

import org.burstsys.felt.model.collectors.tablet.FeltTabletCollector
import org.burstsys.felt.model.tree.code._

/**
 *
 */
trait FeltTabletSymbols {

  final private val feltBinding = "feltBinding.collectors.tablets"

  final lazy val grabMethod: String = s"$feltBinding.grabCollector"

  final lazy val releaseMethod: String = s"$feltBinding.releaseCollector"

  final
  def tabletBuilderVariable(collectorName: String) = s"tablet_${collectorName}_builder"

  final
  def tabletRootVariable(collectorName: String) = s"tablet_${collectorName}_root"

  final
  def tabletInstanceVariable(collectorName: String) = s"tablet_${collectorName}_instance"

  final
  def tabletMemberValueVariable(collectorName: String) = s"tablet_${collectorName}_member_value"

  final
  def tabletMemberIsFirstVariable(collectorName: String) = s"tablet_${collectorName}_member_is_first"

  final
  def tabletMemberIsLastVariable(collectorName: String) = s"tablet_${collectorName}_member_is_last"

  final
  val tabletCollectorClassName: String = classOf[FeltTabletCollector]

}
