/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet.generate.splice

import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.collectors.tablet.decl.FeltTabletDecl
import org.burstsys.felt.model.sweep.splice._

import scala.language.implicitConversions

/**
 * generate splices for a [[FeltTabletDecl]]
 */
trait FeltTabletSplicer extends FeltSplicer with FeltSpliceStore

object FeltTabletSplicer {
  def apply(analysis: FeltAnalysisDecl): FeltTabletSplicer =
    FeltTabletSplicerContext(analysis: FeltAnalysisDecl)
}

private[splice] final case
class FeltTabletSplicerContext(analysis: FeltAnalysisDecl) extends FeltTabletSplicer {

  def collectSplices: Array[FeltSplice] = {

    // basic per route globals/lifecycle
    this ++= analysis.tablets.flatMap(FeltTabletRootSplicer(_).collect.allSplices)

    // results
    allSplices

  }

}
