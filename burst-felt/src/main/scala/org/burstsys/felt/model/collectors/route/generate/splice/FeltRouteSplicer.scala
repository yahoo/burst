/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.generate.splice

import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.collectors.route.decl.FeltRouteDecl
import org.burstsys.felt.model.sweep.splice.{FeltSplice, FeltSpliceStore}

/**
 * generate splices for a [[FeltRouteDecl]]
 */
final case
class FeltRouteSplicer(analysis: FeltAnalysisDecl) extends FeltSpliceStore {

  def collectSplices: Array[FeltSplice] = {

    // basic per route globals/lifecycle
    this ++= analysis.routes flatMap (FeltRouteRootSplicer(_).collect.allSplices)

    // results
    allSplices

  }

}
