/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.generate.splice.reference

import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.cube.generate.FeltStaticCubeSpace
import org.burstsys.felt.model.sweep.FeltSweepGenerator
import org.burstsys.felt.model.sweep.splice.FeltSpliceGenerator
import org.burstsys.felt.model.sweep.symbols.{controlRelationScopeValue, feltSchemaSym}
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltNoCode, I}

/**
 * We are visiting the scalar reference that is the root of the object tree.
 * <ul>
 * <li>'''FeltTraverseCommencePlace:''' set '''currentInstanceCube''' to the '''rootCube''' </li>
 * <li>'''FeltTraverseCompletePlace:'''  set '''rootCube''' to the '''currentInstanceCube'''  </li>
 * </ul>
 */
private[splice]
trait FeltCubeRootSpliceOps extends FeltSweepGenerator {

  /**
   * FeltTraverseCommencePlace
   */
  final protected
  def cubeRootCommenceSplice(implicit cube: FeltCubeDecl): FeltSpliceGenerator =
    implicit cursor => {

      // nothing to do at non root
      if (!cursor.isRoot) FeltNoCode

      else {
        val ns = FeltStaticCubeSpace(global, cube.cubeName)
        val rootInstance = ns.rootCube
        val currentInstance = ns.currentInstCube

        s"""|
            |$I$currentInstance = $rootInstance; """.stripMargin
      }
    }

  /**
   * FeltTraverseCompletePlace
   */
  final protected
  def cubeRootCompleteSplice(implicit cube: FeltCubeDecl): FeltSpliceGenerator =
    implicit cursor => {

      // nothing to do at non root
      if (!cursor.isRoot) FeltNoCode

      else {
        val zcs = FeltStaticCubeSpace(global, cube.cubeName)
        val rootInstance = zcs.rootCube
        val currentInstance = zcs.currentInstCube

        val controlAbortCode: FeltCode = if (!cube.global.features.ctrlVerbs) true.toString
        else
          s"""|
              |${C(s"at the 'root' we would need to abort the root ''instance'' cube since it is the root ''relation''")}
              |${I}if ( $feltSchemaSym.visitInScope( path, ${controlRelationScopeValue(cube.cubeName)} ) ) $currentInstance.clear(); """.stripMargin

        s"""|$controlAbortCode
            |$I$rootInstance = $currentInstance; """.stripMargin
      }
    }

}
