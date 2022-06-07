/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet

import org.burstsys.felt.model.collectors.decl.FeltCollectorPlan
import org.burstsys.felt.model.collectors.tablet.decl.{FeltTabletDecl, FeltTabletRef}

abstract class FeltTabletPlan extends AnyRef
  with FeltCollectorPlan[FeltTabletRef, FeltTabletBuilder] {

  def decl: FeltTabletDecl

  def binding: FeltTabletProvider

  override lazy val initialize: FeltTabletPlan = {
    this
  }

}
