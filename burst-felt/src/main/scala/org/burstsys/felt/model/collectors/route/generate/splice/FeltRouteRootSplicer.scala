/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.generate.splice

import org.burstsys.felt.model.collectors.route.FeltRouteProvider
import org.burstsys.felt.model.collectors.route.decl.FeltRouteDecl
import org.burstsys.felt.model.sweep.splice.{FeltGenSplice, FeltSpliceGenerator, FeltSpliceStore, _}
import org.burstsys.felt.model.sweep.symbols.sweepRuntimeSym
import org.burstsys.felt.model.tree.code.{FeltCode, FeltNoCode, I}

final case
class FeltRouteRootSplicer(route: FeltRouteDecl) extends FeltSpliceStore {

  def binding: FeltRouteProvider = route.global.binding.collectors.routes

  /**
   * ROOT REFERENCE SCALAR
   *
   */
  def collect: this.type = {
    val treeNode = route.global.feltSchema.rootNode
    val spliceTag = s"route_${route.routeName}"
    if (routeRootCommenceSplice != FeltEmptySpliceGenerator)
      this += FeltGenSplice(route.global, route.location, spliceTag, treeNode.pathName, FeltTraverseCommencePlace, routeRootCommenceSplice)
    if (routeRootCompleteSplice != FeltEmptySpliceGenerator)
      this += FeltGenSplice(route.global, route.location, spliceTag, treeNode.pathName, FeltTraverseCompletePlace, routeRootCompleteSplice)
    this
  }

  /**
   * FeltTraverseCommencePlace
   */
  private
  def routeRootCommenceSplice: FeltSpliceGenerator =
    implicit cursor => {

      // nothing to do at non root
      if (!cursor.isRoot) FeltNoCode

      else {
        val reference = route.reference
        val root = reference.rootVariable
        val instanceVariable = reference.instanceVariable

        val controlAbortCode: FeltCode = if (!route.global.features.ctrlVerbs) true.toString
        else FeltNoCode

        s"""|$controlAbortCode
            |$I$sweepRuntimeSym.$instanceVariable = $sweepRuntimeSym.$root; """.stripMargin
      }
    }

  /**
   * FeltTraverseCompletePlace
   */
  private
  def routeRootCompleteSplice: FeltSpliceGenerator = FeltEmptySpliceGenerator

}
