/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.generate.splice.reference

import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.cube.generate.FeltStaticCubeSpace
import org.burstsys.felt.model.collectors.cube.generate.splice.surgeon._
import org.burstsys.felt.model.sweep.FeltSweepGenerator
import org.burstsys.felt.model.sweep.splice.FeltSpliceGenerator
import org.burstsys.felt.model.sweep.symbols.grabCubeMethod
import org.burstsys.felt.model.tree.code._

/**
 * We are visiting a single scalar reference object that is in a __1:1__ relation within a parent object. Note this includes
 * the '''root''' object which is always a scalar reference.
 * <ul>
 * <li>'''FeltInstanceAllocPlace:''' allocate a new ''currentInstanceCube'' for the scalar reference </li>
 * <li>'''FeltChildMergePlace:'''  if this scalar relation is to be __ merged__  into its parent object ''parentInstanceCube'' (it won't be joined).
 * Free ''currentRelationCube'' afterwards</li>
 * <li>'''FeltChildJoinPlace:'''   if this scalar relation is to be __joined__ into its parent object ''parentInstanceCube'' (it won't be merged).
 * Free ''currentRelationCube'' afterwards </li>
 * </ul>
 */
private[splice]
trait FeltCubeRefScalRelationSpliceOps extends FeltSweepGenerator {

  /**
   * FeltInstanceAllocPlace
   */
  final protected
  def cubeRefScalRelAllocSplice(implicit cube: FeltCubeDecl): FeltSpliceGenerator =
    implicit cursor => {

      // nothing to do at root
      if (cursor.isRoot) FeltNoCode

      else {
        val collectorClass = binding.collectors.cubes.collectorClassName
        val ns = FeltStaticCubeSpace(global, cube.cubeName)
        val builder = ns.cubeBuilderVar
        val currentInstance = ns.currentInstCube
        val parentInstance = ns.parentInstCube
        val currentRelation = ns.currentRelCube

        // for joins we want a separate cursor, for merges we want to inherit the parent cursor
        val mergeInheritCode = if (cube.calculus.isChildMergeAt(cursor.pathKey)) FeltNoCode
        else {
          s"""|
              |$I$currentInstance.inheritCursor( $builder, $currentInstance, $parentInstance ); """.stripMargin
        }
        s"""|
            |$I$currentInstance  = $grabCubeMethod( $builder ).asInstanceOf[ $collectorClass ]; $mergeInheritCode
            |$I$currentRelation = $currentInstance; """.stripMargin
      }
    }

  /**
   * FeltChildMergePlace
   */
  final protected
  def cubeRefScalRelMergeSplice(implicit cube: FeltCubeDecl): FeltSpliceGenerator =
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
  def cubeRefScalRelJoinSplice(implicit cube: FeltCubeDecl): FeltSpliceGenerator =
    implicit cursor => {

      // nothing to do at root
      if (cursor.isRoot) FeltNoCode

      // only do this is the calculus indicates we are joining at this path
      else if (cube.calculus.isChildJoinAt(cursor.pathKey)) genStatChildJoin(cube)

      else FeltNoCode
    }

}
