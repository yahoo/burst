/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.generate.splice.value

import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.cube.generate.FeltStaticCubeSpace
import org.burstsys.felt.model.collectors.cube.generate.splice.surgeon._
import org.burstsys.felt.model.sweep.FeltSweepGenerator
import org.burstsys.felt.model.sweep.splice.FeltSpliceGenerator
import org.burstsys.felt.model.sweep.symbols.grabCubeMethod
import org.burstsys.felt.model.tree.code._

/**
 * We are visiting a value map that is in a __1:n__ relation within its parent object.
 * <ul>
 * <li>'''FeltVectorAllocPlace:'''  allocate a new ''currentRelationCube'' to hold the overall value map iterated visit</li>
 * <li>'''FeltChildMergePlace:'''  for each member of the map, after each visit, __merge__  the current value into the overall ''currentRelationCube''
 * (a join __will not__ happen)</li>
 * <li>'''FeltChildJoinPlace:'''  for each member of the map, after each visit, __join__  the current value into the overall ''currentRelationCube''
 * (a merge __will not__ happen)</li>
 * </ul>
 * '''NOTE:''' we do not allocate a cube per value in the vector for now - we may need to later
 */
private[splice]
trait FeltCubeValMapRelationSpliceOps extends FeltSweepGenerator {

  /**
   * FeltVectorAllocPlace
   */
  final
  def cubeValMapAllocSplice(implicit cube: FeltCubeDecl): FeltSpliceGenerator =
    implicit cursor => {

      // nothing to do at root
      if (cursor.isRoot) FeltNoCode

      else {
        val collectorClass = binding.collectors.cubes.collectorClassName
        val ns = FeltStaticCubeSpace(global, cube.cubeName)
        val parentInstance = ns.parentInstCube
        val currentRelation = ns.currentRelCube
        val currentInstance = ns.currentInstCube
        val builder = ns.cubeBuilderVar

        // always inherit cursor in value collections
        s"""|
            |$I$currentRelation = $grabCubeMethod( $builder ).asInstanceOf[ $collectorClass ];
            |$I$currentRelation.inheritCursor( $builder, $currentRelation, $parentInstance );
            |$I$currentInstance = $currentRelation; """.stripMargin
      }
    }

  /**
   * FeltChildMergePlace
   */
  final
  def cubeValMapRelMergeSplice(implicit cube: FeltCubeDecl): FeltSpliceGenerator =
    implicit cursor => {

      // nothing to do at root
      if (cursor.isRoot) FeltNoCode

      // only do this is the calculus indicates we are merging at this path
      else if (cube.calculus.isChildMergeAt(cursor.pathKey)) genStatChildMerge(cube)

      else FeltNoCode
    }

  /**
   * FeltChildJoinPlace
   *
   * @return
   */
  final
  def cubeValMapRelJoinSplice(implicit cube: FeltCubeDecl): FeltSpliceGenerator =
    implicit cursor => {

      // nothing to do at root
      if (cursor.isRoot) FeltNoCode

      // only do this is the calculus indicates we are joining at this path
      else if (cube.calculus.isChildJoinAt(cursor.pathKey)) genStatChildJoin(cube)

      else FeltNoCode
    }

}
