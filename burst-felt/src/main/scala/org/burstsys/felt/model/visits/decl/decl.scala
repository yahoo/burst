/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.visits

import org.burstsys.felt.model.collectors.FeltCollectorRef
import org.burstsys.felt.model.sweep.splice.{FeltSplice, FeltSpliceGenerator, FeltSpliceStore}
import org.burstsys.felt.model.tree.code.FeltCodeCursor

package object decl {

  /**
   * a collector that supports being a '''visitable'''
   */
  trait FeltVisitableRef extends FeltCollectorRef {

    def visitableSplicer: FeltVisitableSplicer

    def visitableTag: String = nameSpace.absoluteNameSansRoot.stripPrefix(global.analysisName).stripPrefix(".")

  }

  /**
   * a collector that supports being a '''visitor'''
   */
  trait FeltVisitorRef extends FeltCollectorRef {

    def visitorSplicer: FeltVisitorSplicer

    def visitorTag: String = nameSpace.absoluteNameSansRoot.stripPrefix(global.analysisName).stripPrefix(".")

  }

  /**
   * this API is used to create the splices generators
   * that will serve the needs of the collector being
   * visited
   */
  trait FeltVisitableSplicer extends FeltSpliceStore {

    /**
     * a visitable may have felt code it wants generated into sweep runtime
     *
     * @param visitTag
     * @param cursor
     * @return
     */
    def generateVisitableRtDecls(visitTag: String): FeltSpliceGenerator

    /**
     * here is where the visitable does any pre iteration initialization
     *
     * @param visitTag a unique identifier for this visit
     * @return
     */
    def visitablePrepare(visitTag: String): FeltSpliceGenerator

    /**
     * here is where the visitable begins the iteration
     *
     * @param visitTag a unique identifier for this visit
     * @return
     */
    def visitableStartLoop(visitorTag: String): FeltSpliceGenerator

    /**
     * here is where the visitable does any pre member initialization
     *
     * @param visitTag a unique identifier for this visit
     * @return
     */
    def visitableMemberPrepare(visitTag: String): FeltSpliceGenerator

    /**
     * here is where the visitable does iteration of a secondary
     * (nested) loop
     *
     * @param visitTag a unique identifier for this visit
     * @return
     */
    def visitableNestedIteration(visitTag: String): FeltSpliceGenerator

    /**
     * here is where the visitable does any post member initialization
     *
     * @param visitTag a unique identifier for this visit
     * @return
     */
    def visitableMemberCleanup(visitTag: String): FeltSpliceGenerator

    /**
     * here is where the visitable loops back the iteration
     *
     * @param visitTag a unique identifier for this visit
     * @return
     */
    def visitableEndLoop(visitTag: String): FeltSpliceGenerator

    /**
     * here is where the visitable does any post iteration cleanup
     *
     * @param visitTag a unique identifier for this visit
     * @return
     */
    def visitableCleanup(visitTag: String): FeltSpliceGenerator

  }

  /**
   * this API is used to create the splices generators
   * that will serve the needs of the collector doing
   * the visiting
   */
  trait FeltVisitorSplicer extends FeltSpliceStore {

    /**
     * a visitor may have felt code it wants generated into sweep runtime
     * generally these are sweep runtime 'variables' that can be accessed
     * and shared across splice bodies.
     *
     * @param visitTag
     * @param cursor
     * @return
     */
    def generateVisitorRtDecls(visitTag: String): FeltSpliceGenerator

    /**
     * here is where the visitor does any pre iteration initialization
     *
     * @param visitTag a unique identifier for this visit
     * @return
     */
    def visitorIterationPrepare(visitTag: String): FeltSpliceGenerator

    /**
     * actions added pre the entire iteration
     *
     * @param visitTag a unique identifier for this visit
     * @param actionSplices
     * @return
     */
    def visitorBeforeActions(visitTag: String, actionSplices: Array[FeltSplice]): FeltSpliceGenerator

    /**
     * here is where the visitor does any pre member initialization
     *
     * @param visitTag a unique identifier for this visit
     * @return
     */
    def visitorMemberPrepare(visitTag: String): FeltSpliceGenerator

    /**
     * this is where the visitor does any specified pre member actions
     *
     * @param visitTag a unique identifier for this visit
     * @param actionSplices
     * @return
     */
    def visitorPreActions(visitTag: String, actionSplices: Array[FeltSplice]): FeltSpliceGenerator

    /**
     * this is where the visitor does any specified per member actions
     *
     * @param visitTag a unique identifier for this visit
     * @param actionSplices
     * @return
     */
    def visitorSituActions(visitTag: String, actionSplices: Array[FeltSplice]): FeltSpliceGenerator

    /**
     * this is where the visitor does any specified post member actions
     *
     * @param visitTag a unique identifier for this visit
     * @param actionSplices
     * @return
     */
    def visitorPostActions(visitTag: String, actionSplices: Array[FeltSplice]): FeltSpliceGenerator

    /**
     * here is where the visitor does any post member cleanup
     *
     * @param visitTag a unique identifier for this visit
     * @return
     */
    def visitorMemberCleanup(visitTag: String): FeltSpliceGenerator

    /**
     * actions executed after all the members in the iteration (if any) have been processed)
     *
     * @param visitTag a unique identifier for this visit
     * @param actionSplices
     * @return
     */
    def visitorAfterActions(visitTag: String, actionSplices: Array[FeltSplice]): FeltSpliceGenerator

    /**
     * cleanup by the visitor executed after all the members in the iteration (if any) have been processed)
     *
     * @param visitTag a unique identifier for this visit
     * @return
     */
    def visitorIterationCleanup(visitTag: String): FeltSpliceGenerator

    /**
     * after iteration and after post actions, we do any needed joins
     * @param visitTag
     * @return
     */
    def visitorJoinOperations(visitTag: String): FeltSpliceGenerator

    def visitorCleanupOperations(visitTag: String): FeltSpliceGenerator

    final
    def getMethodCalls(actionSplices: Array[FeltSplice])(implicit cursor: FeltCodeCursor): String = {
      actionSplices.map(s => s"${s.generateSpliceMethodCall}").mkString("/n")
    }

  }

}
