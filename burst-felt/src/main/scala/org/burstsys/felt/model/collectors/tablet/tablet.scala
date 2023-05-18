/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors

import org.burstsys.felt.model.collectors.decl.FeltCollectorProvider
import org.burstsys.felt.model.collectors.tablet.decl.{FeltTabletDecl, FeltTabletRef}
import org.burstsys.vitals.reporter.instrument.MB
import org.burstsys.vitals.stats.KB

package object tablet {

  final
  val FeltDefaultTabletSize: Int = (100 * KB).toInt

  trait FeltTabletProvider
    extends FeltCollectorProvider[FeltTabletCollector, FeltTabletBuilder, FeltTabletRef, FeltTabletDecl, FeltTabletPlan]


}
