/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.shrub

import org.burstsys.felt.model.collectors.decl.FeltCollectorPlan
import org.burstsys.felt.model.collectors.shrub.decl.{FeltShrubDecl, FeltShrubRef}

abstract class FeltShrubPlan extends AnyRef
  with FeltCollectorPlan[FeltShrubRef, FeltShrubBuilder] {

  def decl: FeltShrubDecl

  def binding: FeltShrubProvider

  override lazy val initialize: FeltShrubPlan = {
    this
  }

}
