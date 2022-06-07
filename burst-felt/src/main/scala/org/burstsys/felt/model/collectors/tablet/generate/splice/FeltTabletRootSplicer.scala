/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet.generate.splice

import org.burstsys.felt.model.collectors.route.FeltRouteProvider
import org.burstsys.felt.model.collectors.tablet.decl.FeltTabletDecl
import org.burstsys.felt.model.sweep.splice.{FeltGenSplice, FeltSpliceGenerator, FeltSpliceStore, _}
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltNoCode, I}

final case
class FeltTabletRootSplicer(tablet: FeltTabletDecl) extends FeltSpliceStore {

  def binding: FeltRouteProvider = tablet.global.binding.collectors.routes

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNALS
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * ROOT REFERENCE SCALAR
   *
   */
  def collect: this.type = {
    val treeNode = tablet.global.feltSchema.rootNode
    val tabletName = tablet.tabletName
    val spliceTag = s"tablet_${tabletName}"
    this += FeltGenSplice(
      tablet.global, tablet.location, spliceTag, treeNode.pathName, FeltTraverseCommencePlace,
      tabletRootCommenceSplice
    )
    this += FeltGenSplice(
      tablet.global, tablet.location, spliceTag, treeNode.pathName, FeltTraverseCompletePlace,
      tabletRootCompleteSplice
    )
    this
  }

  /**
   * FeltTraverseCommencePlace
   */
  private
  def tabletRootCommenceSplice: FeltSpliceGenerator =
    implicit cursor => {

      // nothing to do at non root
      if (!cursor.isRoot) FeltNoCode

      else {
        val reference = tablet.reference
        val root = reference.rootVariable
        val instanceVariable = reference.instanceVariable

        val controlAbortCode: FeltCode = if (!tablet.global.features.ctrlVerbs) true.toString
        else FeltNoCode

        s"""|$controlAbortCode
            |$I$sweepRuntimeSym.$instanceVariable = $sweepRuntimeSym.$root; """.stripMargin
      }
    }

  /**
   * FeltTraverseCompletePlace
   */
  private
  def tabletRootCompleteSplice: FeltSpliceGenerator = FeltEmptySpliceGenerator

}
