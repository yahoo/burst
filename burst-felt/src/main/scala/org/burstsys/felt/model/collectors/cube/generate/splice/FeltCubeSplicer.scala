/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.generate.splice

import org.burstsys.brio.model.schema.types.{BrioReferenceScalarRelation, BrioReferenceVectorRelation, BrioValueMapRelation, BrioValueVectorRelation}
import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.cube.generate.splice.reference.{FeltCubeRefScalRelationSpliceOps, FeltCubeRefVecInstanceSpliceOps, FeltCubeRefVecRelationSpliceOps, FeltCubeRootSpliceOps}
import org.burstsys.felt.model.collectors.cube.generate.splice.value.{FeltCubeValMapRelationSpliceOps, FeltCubeValVecRelationSpliceOps}
import org.burstsys.felt.model.sweep.FeltSweepGenerator
import org.burstsys.felt.model.sweep.splice.{FeltGenSplice, FeltSplice, FeltSpliceStore, FeltSplicer, FeltTraverseCommencePlace, FeltTraverseCompletePlace, _}
import org.burstsys.felt.model.tree.{FeltGlobal, FeltNode}

import scala.language.implicitConversions

/**
 * generate splices for a [[FeltCubeDecl]]
 */
trait FeltCubeSplicer extends FeltSplicer

object FeltCubeSplicer {
  def apply(cube: FeltCubeDecl): FeltCubeSplicer =
    FeltCubeSplicerContext(cube: FeltCubeDecl)
}

/**
 * Top level cube '''splicing''' algorithm. Here we make sure that all cube splices are given a chance to insert themselves.
 */
private final case
class FeltCubeSplicerContext(cube: FeltCubeDecl) extends FeltCubeSplicer
  with FeltSweepGenerator with FeltCubeRootSpliceOps with FeltSpliceStore with FeltCubeRefScalRelationSpliceOps
  with FeltCubeRefVecRelationSpliceOps with FeltCubeValVecRelationSpliceOps with FeltCubeValMapRelationSpliceOps
  with FeltCubeRefVecInstanceSpliceOps {

  val node: FeltNode = cube
  override val global: FeltGlobal = cube.global

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * overall splices
   */
  override
  def collectSplices: Array[FeltSplice] = {
    if (!cube.isRootCube) return Array.empty
    spliceCubeRoot(cube)
    spliceCubeRefScal(cube)
    spliceCubeRefVec(cube)
    spliceCubeValVector(cube)
    spliceCubeValMap(cube)
    allSplices
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNALS
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * ROOT REFERENCE SCALAR
   *
   */
  private
  def spliceCubeRoot(implicit cube: FeltCubeDecl): Unit = {
    val treeNode = brioSchema.rootNode
    val tagName = s"cube_${cube.cubeName}"
    this += FeltGenSplice(this.global, cube.location, tagName, treeNode.pathName, FeltTraverseCommencePlace, cubeRootCommenceSplice)
    this += FeltGenSplice(this.global, cube.location, tagName, treeNode.pathName, FeltTraverseCompletePlace, cubeRootCompleteSplice)
  }


  /**
   * REFERENCE SCALAR
   */
  private
  def spliceCubeRefScal(implicit cube: FeltCubeDecl): Unit = {
    val tagName = s"cube_${cube.cubeName}"
    global.feltSchema allNodesForForms BrioReferenceScalarRelation sortBy (_.pathKey) foreach {
      treeNode =>
        this += FeltGenSplice(this.global, cube.location, tagName, treeNode.pathName, FeltInstanceAllocPlace, cubeRefScalRelAllocSplice)
        this += FeltGenSplice(this.global, cube.location, tagName, treeNode.pathName, FeltChildMergePlace, cubeRefScalRelMergeSplice)
        this += FeltGenSplice(this.global, cube.location, tagName, treeNode.pathName, FeltChildJoinPlace, cubeRefScalRelJoinSplice)
    }
  }

  /**
   * REFERENCE VECTOR
   */
  private
  def spliceCubeRefVec(implicit cube: FeltCubeDecl): Unit = {
    val tagName = s"cube_${cube.cubeName}"
    global.feltSchema allNodesForForms BrioReferenceVectorRelation sortBy (_.pathKey) foreach {
      treeNode =>
        // reference vectors contain implicit reference scalar ''instance'' splices as well - these are handled slightly differently than normal scalar references
        this += FeltGenSplice(this.global, cube.location, tagName, treeNode.pathName, FeltChildMergePlace, cubeRefVecInstMergeSplice)
        this += FeltGenSplice(this.global, cube.location, tagName, treeNode.pathName, FeltChildJoinPlace, cubeRefVecInstJoinSplice)
        // these are for normal reference vector and vector member management
        this += FeltGenSplice(this.global, cube.location, tagName, treeNode.pathName, FeltVectorAllocPlace, cubeRefVecAllocSplice)
        this += FeltGenSplice(this.global, cube.location, tagName, treeNode.pathName, FeltVectorMemberAllocPlace, cubeRefVecMembAllocSplice)
        this += FeltGenSplice(this.global, cube.location, tagName, treeNode.pathName, FeltVectorMemberMergePlace, cubeRefVecMembMergeSplice)
        this += FeltGenSplice(this.global, cube.location, tagName, treeNode.pathName, FeltVectorFreePlace, cubeRefVecFreeSplice)
    }
  }

  /**
   * VALUE VECTOR
   */
  private
  def spliceCubeValVector(implicit cube: FeltCubeDecl): Unit = {
    val tagName = s"cube_${cube.cubeName}"
    global.feltSchema allNodesForForms BrioValueVectorRelation sortBy (_.pathKey) foreach {
      treeNode =>
        //  TODO (for now we are '''not''' allocating a cube per value vector member...
        this += FeltGenSplice(this.global, cube.location, tagName, treeNode.pathName, FeltVectorAllocPlace, cubeValVecAllocSplice)
        this += FeltGenSplice(this.global, cube.location, tagName, treeNode.pathName, FeltChildMergePlace, cubeValVecRelMergeSplice)
        this += FeltGenSplice(this.global, cube.location, tagName, treeNode.pathName, FeltChildJoinPlace, cubeValVecRelJoinSplice)
    }
  }

  /**
   * VALUE MAP
   */
  private
  def spliceCubeValMap(implicit cube: FeltCubeDecl): Unit = {
    val tagName = s"cube_${cube.cubeName}"
    global.feltSchema allNodesForForms BrioValueMapRelation sortBy (_.pathKey) foreach {
      treeNode =>
        // TODO (for now we are '''not''' allocating a cube per value map member...
        this += FeltGenSplice(this.global, cube.location, tagName, treeNode.pathName, FeltVectorAllocPlace, cubeValMapAllocSplice)
        this += FeltGenSplice(this.global, cube.location, tagName, treeNode.pathName, FeltChildMergePlace, cubeValMapRelMergeSplice)
        this += FeltGenSplice(this.global, cube.location, tagName, treeNode.pathName, FeltChildJoinPlace, cubeValMapRelJoinSplice)
    }
  }

}
