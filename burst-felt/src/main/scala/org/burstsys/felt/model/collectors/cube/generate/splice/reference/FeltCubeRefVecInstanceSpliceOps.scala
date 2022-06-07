/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.generate.splice.reference

import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.cube.generate.splice.surgeon._
import org.burstsys.felt.model.sweep.FeltSweepGenerator
import org.burstsys.felt.model.sweep.splice.FeltSpliceGenerator
import org.burstsys.felt.model.tree.code._

/**
 * We are visiting a single reference member objects that is in a __1:n__ relation within a parent reference object.
 * <ul>
 * <li>'''FeltChildMergePlace:'''  if this relation is to be __ merged__  into its parent object ''parentInstanceCube'' (it won't be joined).
 * Free ''currentRelationCube'' afterwards</li>
 * <li>'''FeltChildJoinPlace:'''   if this relation is to be __joined__ into its parent object ''parentInstanceCube'' (it won't be merged).
 * Free ''currentRelationCube'' afterwards </li>
 * </ul>
 */
private[splice]
trait FeltCubeRefVecInstanceSpliceOps extends FeltSweepGenerator {

  /**
   * FeltChildMergePlace
   */
  final protected
  def cubeRefVecInstMergeSplice(implicit cube: FeltCubeDecl): FeltSpliceGenerator =
    implicit cursor => {

      // nothing to do at root
      if (cursor.isRoot) FeltNoCode

      // only do this is the calculus indicates we are merging at this path
      else if (cube.calculus.isChildMergeAt(cursor.pathKey)) genStatChildMerge(cube)

      else FeltNoCode
    }


  /**
   * FeltChildJoinPlace
   */
  final protected
  def cubeRefVecInstJoinSplice(implicit cube: FeltCubeDecl): FeltSpliceGenerator =
    implicit cursor => {

      // nothing to do at root
      if (cursor.isRoot) FeltNoCode

      // only do this is the calculus indicates we are joining at this path
      else if (cube.calculus.isChildJoinAt(cursor.pathKey)) genStatChildJoin(cube)

      else FeltNoCode
    }

}
