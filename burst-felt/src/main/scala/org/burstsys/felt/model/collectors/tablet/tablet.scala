/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors

import org.burstsys.felt.model.collectors.decl.FeltCollectorProvider
import org.burstsys.felt.model.collectors.tablet.decl.{FeltTabletDecl, FeltTabletRef}
import org.burstsys.vitals.instrument.MB

package object tablet {

  final val FeltMaxTablets = 512

  final
  val FeltDefaultTabletSize: Int = (2 * MB).toInt

  type FeltTabletName = String

  type FeltTabletNameKey = Int

  trait FeltTabletProvider
    extends FeltCollectorProvider[FeltTabletCollector, FeltTabletBuilder, FeltTabletRef, FeltTabletDecl, FeltTabletPlan]


}
