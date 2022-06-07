/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.generate.splice.visitor

import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.cube.generate.FeltStaticCubeSpace
import org.burstsys.felt.model.collectors.cube.generate.calculus.FeltCubeCalculus
import org.burstsys.felt.model.collectors.cube.generate.splice.surgeon
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.sweep.splice.{FeltCommentSpliceGenerator, FeltEmptySpliceGenerator, FeltSplice, FeltSpliceGenerator}
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{C, C0, C1, C2, FeltNoCode, I, I1, I2}
import org.burstsys.felt.model.visits.decl.FeltVisitorSplicer

import scala.language.postfixOps

final case
class FeltCubeVisitorSplicer(refName: FeltPathExpr) extends FeltVisitorSplicer {

  final val forceMerge = true

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _global: FeltGlobal = refName.global

  private[this]
  val _binding = _global.binding.collectors.cubes

  private[this]
  def _cubeDecl: FeltCubeDecl = _global.linker.lookupDeclFromAbsoluteOrThrow[FeltCubeDecl](refName.absolutePathSansRoot)

  private[this]
  def _calculus: FeltCubeCalculus = _cubeDecl.calculus

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////


  override def generateVisitorRtDecls(visitTag: String): FeltSpliceGenerator = FeltEmptySpliceGenerator

  override def visitorIterationPrepare(visitTag: String): FeltSpliceGenerator = {

    implicit cursor => {
      val space = FeltStaticCubeSpace(_global, _cubeDecl.cubeName)
      val builder = space.cubeBuilderVar
      val currentRelation = space.currentRelCube
      val pathKey = cursor.pathKey

      // for joins we want a separate cursor, for merges we want to inherit the parent cursor
      val mergeInheritCode = if (space.isRoot) {
        s"""|
            |$I// since this is bound to schema root we don't inherit the cursor from a parent""".stripMargin
      } else {
        if (_calculus.isChildMergeAt(pathKey)) {
          val parentInstance = space.parentInstCube
          s"""|
              |$I// since this is going to be a merge we inherit the cursor from our parent
              |$I$currentRelation.inheritCursor( $builder, $currentRelation, $parentInstance ); """.stripMargin
        } else if (_calculus.isChildJoinAt(pathKey)) {
          s"""|
              |$I// since this is going to be a join we don't inherit the cursor from our parent""".stripMargin
        } else
          throw FeltException(refName, s"neither a merge nor a join!")
      }

      s"""|
          |${C("prepare the visitor relation (cube)) for iteration")}
          |$I$currentRelation = $grabCubeMethod( $builder ).asInstanceOf[ $collectorClass ]; $mergeInheritCode""".stripMargin

    }
  }

  override def visitorBeforeActions(visitTag: String, actionSplices: Array[FeltSplice]): FeltSpliceGenerator = {
    if (actionSplices.isEmpty) return FeltCommentSpliceGenerator("no visitor before actions")
    implicit cursor => {
      val space = FeltStaticCubeSpace(_global, _cubeDecl.cubeName)
      val currentInstance = space.currentInstCube
      val currentRelation = space.currentRelCube
      s"""|
          |${C("visitor before actions ")}
          |${I}val tmpStaticInstance_before = $currentInstance;
          |$I$currentInstance = $currentRelation; // use relation cube for before actions...
          |${getMethodCalls(actionSplices)}
          |$I$currentInstance = tmpStaticInstance_before; // clean up afterwards...
          |""".stripMargin
    }
  }

  override def visitorMemberPrepare(visitTag: String): FeltSpliceGenerator =
    implicit cursor => {

      val space = FeltStaticCubeSpace(_global, _cubeDecl.cubeName)
      val currentInstance = space.currentInstCube
      val builder = space.cubeBuilderVar

      // for joins we want a separate cursor, for merges we want to inherit the parent cursor
      val mergeInheritCode = if (space.isRoot) {
        s"""|
            |$I// since this is bound to schema root we don't inherit the cursor from our parent""".stripMargin
      } else if (_calculus.isChildMergeAt(cursor.pathKey)) {
        val parentInstance = space.parentInstCube
        s"""|
            |$I2// since this is going to be a merge we inherit the cursor from our parent
            |$I2$currentInstance.inheritCursor( $builder, $currentInstance, $parentInstance ); """.stripMargin
      } else {
        s"""|
            |$I2// since this is going to be a join we don't inherit the cursor from our parent""".stripMargin
      }

      s"""|
          |${C2("prepare the visitor for a new or first member")}
          |${I2}if( $currentInstance == null) { // alloc only once and re-use for all members
          |$I2$currentInstance = $grabCubeMethod( $builder ).asInstanceOf[ $collectorClass ];
          |${I2}}  $mergeInheritCode """.stripMargin

    }

  override def visitorPreActions(visitTag: String, actionSplices: Array[FeltSplice]): FeltSpliceGenerator = {
    if (actionSplices.isEmpty) return FeltCommentSpliceGenerator("no visitor pre actions", 1)
    implicit cursor => {
      s"""|
          |${C2("visitor pre actions ")}
          |${getMethodCalls(actionSplices)(cursor indentRight)}
          |""".stripMargin
    }
  }

  override def visitorSituActions(visitTag: String, actionSplices: Array[FeltSplice]): FeltSpliceGenerator = {
    if (actionSplices.isEmpty) return FeltCommentSpliceGenerator("no visitor situ actions", 1)
    implicit cursor => {
      s"""|
          |${C2("visitor situ actions")}
          |${getMethodCalls(actionSplices)(cursor indentRight)}
          |""".stripMargin
    }
  }

  override def visitorPostActions(visitTag: String, actionSplices: Array[FeltSplice]): FeltSpliceGenerator = {
    if (actionSplices.isEmpty) return FeltCommentSpliceGenerator("no visitor post actions", 1)
    implicit cursor => {
      s"""|
          |${C2("visitor (cube) post actions")}
          |${getMethodCalls(actionSplices)(cursor indentRight)}
          |""".stripMargin
    }
  }

  override def visitorMemberCleanup(visitTag: String): FeltSpliceGenerator =
    implicit cursor => {
      val merge = surgeon.genDynInstMerge(_cubeDecl)(cursor indentRight)
      s"""|
          |${C2("merge the visitor member (cube) into the visitor relation (cube) after every member")}
          |$merge""".stripMargin
    }

  override def visitorAfterActions(visitTag: String, actionSplices: Array[FeltSplice]): FeltSpliceGenerator = {
    if (actionSplices.isEmpty) return FeltCommentSpliceGenerator("no visitor after actions")
    implicit cursor => {
      val space = FeltStaticCubeSpace(_global, _cubeDecl.cubeName)
      val currentInstance = space.currentInstCube
      val dynamicRelation = space.currentRelCube
      s"""|
          |${C("visitor (cube) after actions")}
          |${I}val tmpStaticInstance_after = $currentInstance;
          |$I$currentInstance = $dynamicRelation; // use relation cube for after actions...
          |${getMethodCalls(actionSplices)}
          |$I$currentInstance = tmpStaticInstance_after; // clean up afterwards...
          |""".stripMargin
    }
  }

  override def visitorIterationCleanup(visitTag: String): FeltSpliceGenerator =
    implicit cursor => {
      val pathKey = cursor.pathKey

      /**
       * here we decide if we want to merge or join the iteration relation into the parent instance
       */
      if (_calculus.isChildMergeAt(pathKey)) {
        val op = surgeon.genDynRelMerge(_cubeDecl)
        s"""|
            |${C0("we are done with the iteration - merge our relation (cube) into parent instance")}$op
            |""".stripMargin
      } else if (_calculus.isChildJoinAt(pathKey)) {
        s"""|
            |${C0("we are done with the iteration - we will join our relation (cube) into parent instance later")}
            |""".stripMargin
      } else throw FeltException(refName, s"neither a merge nor a join!!")
    }

  override def visitorJoinOperations(visitTag: String): FeltSpliceGenerator =
    implicit cursor => {
      val pathKey = cursor.pathKey
      if (_calculus.isChildJoinAt(pathKey)) {
        val op = surgeon.genDynRelJoin(_cubeDecl)
        s"""|
            |${C0("merge or join our relation (cube) into our parent instance ")}$op
            |""".stripMargin
      } else FeltNoCode
    }

  override def visitorCleanupOperations(visitTag: String): FeltSpliceGenerator =
    implicit cursor => {
      val space = FeltStaticCubeSpace(_global, _cubeDecl.cubeName)
      val currentInstance = space.currentInstCube
      s"""|
          |${C0("DYNAMIC CLEANUP SPLICES")}
          |${C0("release our instance cube if we used one...")}
          |${I1}if ( $currentInstance != null ) {
          |${I2}$releaseCubeMethod( $currentInstance )
          |${I2}$currentInstance = null;
          |${I1}};
          |""".stripMargin
    }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNALS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private val collectorClass: String = _binding.collectorClassName

}
