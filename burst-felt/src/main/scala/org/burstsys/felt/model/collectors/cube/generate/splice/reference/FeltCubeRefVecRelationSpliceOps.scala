/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.generate.splice.reference

import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.cube.generate.FeltStaticCubeSpace
import org.burstsys.felt.model.sweep.FeltSweepGenerator
import org.burstsys.felt.model.sweep.splice.FeltSpliceGenerator
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.code._

/**
 * We are visiting a set of reference member objects that is a __1:n__ relation within a parent reference vector.
 * <ul>
 * <li>'''FeltVectorAllocPlace:''' at the beginning of the whole member iteration, we allocate a single '''currentRelationCube''' for all vector members</li>
 * <li>'''FeltVectorMemberAllocPlace:''' before processing each member, we allocate a single '''currentInstanceCube''' for each vector member</li>
 * <li>'''FeltVectorMemberMergePlace:''' after processing each member, we merge the '''currentInstanceCube''' intp the '''currentRelationCube'''<</li>
 * <li>'''FeltVectorFreePlace:''' free the '''currentInstanceCube''' if it was allocated (not null)</li>
 * </ul>
 * '''NOTE:''' ''currentRelationCube'' is left to be processed as a merge/join into the object the reference vector is a child relation for</li>
 */
private[splice]
trait FeltCubeRefVecRelationSpliceOps extends FeltSweepGenerator {

  /**
   * FeltVectorAllocPlace
   */
  final protected
  def cubeRefVecAllocSplice(implicit cube: FeltCubeDecl): FeltSpliceGenerator =
    implicit cursor => {

      // nothing to do at root
      if (cursor.isRoot) FeltNoCode

      else {
        val collectorClassName: String = binding.collectors.cubes.collectorClassName

        val ns = FeltStaticCubeSpace(global, cube.cubeName)
        val builder = ns.cubeBuilderVar
        val currentRelation = ns.currentRelCube
        val parentInstance = ns.parentInstCube

        // for joins we want a separate cursor, for merges we want to inherit the parent cursor
        val mergeInheritCode = if (cube.calculus.isChildMergeAt(cursor.pathKey)) FeltNoCode else {
          s"""|
              |$I$currentRelation.inheritCursor($builder, $currentRelation, $parentInstance);""".stripMargin
        }
        s"""|
            |$I$currentRelation  = $grabCubeMethod($builder).asInstanceOf[$collectorClassName];$mergeInheritCode""".stripMargin
      }

    }

  /**
   * FeltVectorMemberAllocPlace
   */
  final protected
  def cubeRefVecMembAllocSplice(implicit cube: FeltCubeDecl): FeltSpliceGenerator =
    implicit cursor => {

      // nothing to do at root
      if (cursor.isRoot) FeltNoCode

      else {
        val collectorClass: String = binding.collectors.cubes.collectorClassName
        val ns = FeltStaticCubeSpace(global, cube.cubeName)
        val builder = ns.cubeBuilderVar
        val currentInstance = ns.currentInstCube
        val parentInstance = ns.parentInstCube

        // TODO do we always inherit cursor in this case???
        s"""
           |$I$currentInstance = if ( $currentInstance == null ) {
           |$I2$grabCubeMethod( $builder ).asInstanceOf[ $collectorClass ]
           |$I} else {
           |$I2$currentInstance.clear();
           |$I2$currentInstance.initCursor( $builder, $currentInstance );
           |$I2$currentInstance
           |$I}
           |$I$currentInstance.inheritCursor( $builder, $currentInstance, $parentInstance );""".stripMargin
      }
    }

  /**
   * FeltVectorMemberMergePlace
   */
  final protected
  def cubeRefVecMembMergeSplice(implicit cube: FeltCubeDecl): FeltSpliceGenerator =
    implicit cursor => {

      // nothing to do at root
      if (cursor.isRoot) FeltNoCode

      else {
        val ns = FeltStaticCubeSpace(global, cube.cubeName)
        val builder = ns.cubeBuilderVar
        val dictionary = ns.cubeDictionaryVar
        val currentRelation = ns.currentRelCube
        val currentInstance = ns.currentInstCube

        val calculatedCube = cube.calculus.memberMergeAt(cursor.pathKey).get.cube
        val dimensionMask = calculatedCube.dimensionMask.data
        val aggregationMask = calculatedCube.aggregationMask.data

        s"""|
            |$I$currentRelation.intraMerge(
            |$I2$builder, $dictionary,
            |$I2$currentInstance,
            |$I2$vitalsBitMapClass( $dimensionMask ), $vitalsBitMapClass( $aggregationMask )
            |$I);""".stripMargin

      }
    }

  /**
   * FeltVectorFreePlace
   */
  final protected
  def cubeRefVecFreeSplice(implicit cube: FeltCubeDecl): FeltSpliceGenerator =
    implicit cursor => {

      // nothing to do at root
      if (cursor.isRoot) FeltNoCode

      else {
        val ns = FeltStaticCubeSpace(global, cube.cubeName)
        val currentInstance = ns.currentInstCube

        s"""
           |${I}if ( $currentInstance != null ) $releaseCubeMethod( $currentInstance );
           |$I$currentInstance = null;""".stripMargin
      }
    }

}
