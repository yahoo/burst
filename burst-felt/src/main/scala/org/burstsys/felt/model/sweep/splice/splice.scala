/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.sweep

import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, FeltNoCode}

package object splice {

  /**
   * these are ''splices'' -- code generating closures that get executed
   * at particular parts in a traversal to generate code that is then executed
   * at runtime in a specific part of the traversal. '''NOTE:''' the cursor that is
   * passed to this is the cursor that is active __during__ code generation, __not__
   * the cursor when the splice is collected and placed into a ''spliceset''.
   */
  type FeltSpliceGenerator = FeltCodeCursor => FeltCode

  /**
   * a marker for no splice provided
   */
  final val FeltEmptySpliceGenerator: FeltSpliceGenerator = implicit cursor => {
    FeltNoCode
  }

  /**
   * a marker for no splice provided
   */
  final def FeltCommentSpliceGenerator(msg: String, indent: Int = 0): FeltSpliceGenerator = implicit cursor => {
    s"""|
        |${C(msg)(cursor indentRight indent)}""".stripMargin
  }

  type FeltPlacementKey = Int

  case class FeltPlacement(key: FeltPlacementKey, isDynamic: Boolean = false) {
    override def toString: String = s"${getClass.getSimpleName.stripPrefix("Felt").stripSuffix("Place").stripSuffix("$")}"

    def fullClassName: String = getClass.getName.replace("package$", "").stripSuffix("$")
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Scalar Visits
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * This is code generation (presumably directly from query semantics) spliced in ''before'' children of a
   * reference-scalar are processed. It is always
   * matched by a subsequence [[FeltInstancePostPlace]]
   */
  object FeltInstancePrePlace extends FeltPlacement(1)

  /**
   * This is code generation (presumably directly from query semantics) spliced in ''after'' children of a
   * reference-scalar are ''merged'' but before they are ''joined''. It is always
   * matched by a preceding [[FeltInstancePrePlace]]
   */
  object FeltInstancePostPlace extends FeltPlacement(2)

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Vector Visits
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * place generated code ''before'' vector members are iterated
   */
  object FeltVectorBeforePlace extends FeltPlacement(3)

  /**
   * place generated code ''after'' vector members are iterated
   */
  object FeltVectorAfterPlace extends FeltPlacement(4)

  /**
   * This is code generation (presumably directly from query semantics) spliced in ''as each'' member of any
   * vector is processed.
   */
  object FeltVectorMemberSituPlace extends FeltPlacement(5)

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Scalar Lifecycle
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * place generated code where an instance is being processed to allow for resource allocation
   */
  object FeltInstanceAllocPlace extends FeltPlacement(6)

  /**
   * place generated code where an instance is being processed to allow for resource free'ing
   */
  object FeltInstanceFreePlace extends FeltPlacement(7)

  /**
   * place generated code where a child is ''merged'' into a parent.
   * This happens ''before'' the ''FeltInstancePostPlace''
   */
  object FeltChildMergePlace extends FeltPlacement(8)

  /**
   * place generated code where a child is ''joined'' into a parent. This happens
   * ''after'' the ''FeltInstancePostPlace''
   */
  object FeltChildJoinPlace extends FeltPlacement(9)

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Vector Lifecycle
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * place generated code where an vector relation is being processed to allow for resource allocation
   */
  object FeltVectorAllocPlace extends FeltPlacement(10)

  /**
   * place generated code where a vector relation is being processed to allow for resource free'ing
   */
  object FeltVectorFreePlace extends FeltPlacement(11)

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Vector Member Lifecycle
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * place generated code where each member of a vector relation is being processed to allow for resource allocation
   */
  object FeltVectorMemberAllocPlace extends FeltPlacement(12)

  /**
   * place generated code where each member of a vector relation is being processed to allow for resource free'ing
   */
  object FeltVectorMemberFreePlace extends FeltPlacement(13)

  /**
   * place generated code where each member of a vector relation is being processed to allow for ''merging''
   */
  object FeltVectorMemberMergePlace extends FeltPlacement(14)

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // COMMENCE/COMPLETE - traversal scope
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * This is where traversal processing starts and any traversal scoped resource
   * management and setup code is performed. It is always
   * matched by a subsequent [[ FeltTraverseCompletePlace]]
   */
  object FeltTraverseCommencePlace extends FeltPlacement(15)

  /**
   * This is where traversal processing ends and traversal resource cleanup and final
   * result processing occurs. It is always
   * matched by a subsequent [[ FeltTraverseCommencePlace]]
   */
  object FeltTraverseCompletePlace extends FeltPlacement(16)

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Dynamic Paths Overlay
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * this is a placement for visiting dynamic (analysis defined) (non static/schema defined) placements
   * Happens ''BEFORE'' post actions
   */
  object FeltDynamicVisitPlace extends FeltPlacement(17, isDynamic = true)

  /**
   * this is a placement for joining dynamic (analysis defined) (non static/schema defined) placements
   * Happens ''AFTER'' post actions
   */
  object FeltDynamicJoinPlace extends FeltPlacement(18, isDynamic = true)

  object FeltDynamicCleanupPlace extends FeltPlacement(19, isDynamic = true)

}
