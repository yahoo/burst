/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model

import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.expressions.cast.FeltCastExprTreeRules
import org.burstsys.felt.model.reference.FeltReferenceTreeRules
import org.burstsys.felt.model.control.generate.FeltCtrlTreeRules
import org.burstsys.felt.model.sweep.lexicon.FeltLexiconTreeRules
import org.burstsys.felt.model.visits.FeltVisitRules
import org.burstsys.vitals.strings._

import scala.language.implicitConversions
import scala.reflect.ClassTag

/**
 * =tree=
 */
package object tree {

  final val emptyNodeArray = new Array[FeltNode](0)

  /**
   * tree rules are implicit classes that add ''rules'' to any node in a tree (generally the root)
   * this marker trait allows us to track them
   */
  trait FeltTreeRules extends Any

  final implicit
  class FeltGlobalTreeRules(node: FeltNode) extends FeltTreeRules {

    def bindGlobal(global: FeltGlobal): Unit = node.treeUpdate(a => a.global = global)

    def asArray: Array[_ <: FeltNode] = if (node == null) Array.empty else Array(node)

  }

  /**
   * convenience class for operations on optional nodes
   *
   * @param option
   */
  implicit class FeltRichNodeOption(option: Option[_ <: FeltNode]) extends FeltTree {

    def asArray: Array[FeltNode] = if (option.isDefined) Array(option.get) else Array.empty

    final override
    def canInferTypes: Boolean = {
      option match {
        case None => true
        case Some(node) => node.canInferTypes
      }
    }

    final override
    def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] = {
      option match {
        case None => Array.empty
        case Some(n) => n.treeApply(rule)
      }
    }

    final
    def resolveTypes(): Unit = {
      option match {
        case None => Array.empty
        case Some(n) => n.resolveTypes
      }
    }

  }

  /**
   * convenience class for operations on arrays of nodes
   *
   * @param nodes
   */
  implicit class FeltRichNodeArray[N <: FeltNode](nodes: Array[N]) extends FeltTree {

    final override
    def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] = {
      nodes flatMap {
        n => n.treeApply(rule)
      }
    }

    final override
    def canInferTypes: Boolean = nodes.forall(_.canInferTypes)

    final
    def resolveTypes(): Unit = nodes.foreach(_.resolveTypes)

  }

  /**
   * method applied to a [[FeltNode]] to ''activate'' or applied rules/transforms/typing/linking
   * etc. Once a complete and valid [[FeltTree]] is activated it is ready for code generation.
   *
   * @param parsedTree
   * @tparam N
   */
  implicit final
  class FeltActivator(parsedTree: FeltAnalysisDecl) {

    def activate(): FeltAnalysisDecl = {

      parsedTree.resolveTypes // resolve types where possible at this point

      parsedTree.assertFrame()
      parsedTree.assertCollectors()
      parsedTree.wireNameSpace()

      /**
       * convert all static sub expressions - this changes the topology of the tree
       */
      val activatedTree = parsedTree.reduceStatics

      /**
       * wire up some tree globals. Does not hurt to
       * call this over and over again where needed. This has to be
       * done where ever the syntax tree topology has changed or been augmented.
       */
      def bindTreeGlobals(): Unit = {
        activatedTree.linkReferences()
        activatedTree.resolveTypes
        activatedTree.bindLexiconStrings
        activatedTree.castExprRules()
        activatedTree.detectCtrlVerbs()
      }

      bindTreeGlobals()

      activatedTree.validateVisits()

      bindTreeGlobals()

      // check the brio references within the visit actions
      activatedTree.validateBrioReferences()

      bindTreeGlobals()


      activatedTree

    }

  }

  def printFeatures(features: (Boolean, String)*): String = {
    val msgs = features.map { case (state, str) => if (state) str else null }.noNulls
    if (msgs.nonEmpty) msgs.mkString("features[", ", ", "]") else ""
  }
}
