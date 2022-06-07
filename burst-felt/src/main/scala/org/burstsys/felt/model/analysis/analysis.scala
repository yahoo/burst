/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model

import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.collectors.decl.FeltCollectorDecl
import org.burstsys.felt.model.sweep._
import org.burstsys.felt.model.tree.{FeltNode, FeltTreeRules}
import org.burstsys.felt.model.visits.decl.FeltStaticVisitDecl

/**
 * =analysis=
 * The [[FeltAnalysisDecl]] is the root of a FELT analysis  which is
 * '''code generated''' into a [[FeltSweep]]. Each [[FeltAnalysisDecl]] contains
 * exactly one [[FeltCollectorDecl]] instance
 * and zero or more [[FeltStaticVisitDecl]] instance(s)
 */
package object analysis {

  /**
   * attach ''rules'' to a felt analysis tree node
   *
   * @param node
   */
  final implicit
  class FeltAnalysisRules(node: FeltNode) extends FeltTreeRules {

  }

}
