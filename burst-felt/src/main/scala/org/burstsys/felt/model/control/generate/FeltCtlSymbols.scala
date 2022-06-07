/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.control.generate

import org.burstsys.felt.model.sweep.symbols.{FeltSweepVar, FeltVar, sweepRuntimeSym}

/**
 * ==Control Verbs==
 * These variables need to be present in instances of [[org.burstsys.felt.model.sweep.FeltSweep]] that use control verbs.
 * These are duplicated for each sub query in the analysis.
 * <ul>
 * <li>'''controlRelationScope''': set to the ''relation path key level'' [[org.burstsys.brio.types.BrioPath.BrioPathKey]] if a traversal is
 * in early ''relation'' exit mode or [[org.burstsys.brio.types.BrioPath.BrioPathKeyNotFound]] if not</li>
 * <li>'''controlMemberScope''': set to the ''member path key level'' [[org.burstsys.brio.types.BrioPath.BrioPathKey]] if a traversal is
 * in early ''member'' exit mode or [[org.burstsys.brio.types.BrioPath.BrioPathKeyNotFound]] if not</li>
 * <li>'''controlDiscardScope''': set to true if data is to be discarded within an active control scope</li>
 * </ul>
 * === Design Notes ===
 * <ul>
 * <li>early ''member'' traversal exit mode requires that '''ALL''' subqueries are in the same mode with the same
 * scope in order to fully benefit from traversal short cuts. The user splices are not executed however, for those
 * sub-queries that do not require them.</li>
 * </ul>
 *
 */
trait FeltCtlSymbols extends Any {

  /////////////////////////////////////////////////////////////////////////////////////////////
  // controlRelationScope
  /////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * the current relation path (in this frame) that is activated in a control verb
   * @param frameName
   * @return
   */
  final
  def controlRelationScopeName(frameName: String): String = s"control_${frameName}_relation_scope"

  /**
   *
   * @param frameName
   * @return
   */
  final
  def controlRelationScopeVarDecl(frameName: String): FeltVar = s"var ${controlRelationScopeName(frameName)} : Int = -1; // inactive"

  /**
   *
   * @param frameName
   * @return
   */
  final
  def controlRelationScopeValue(frameName: String): FeltSweepVar = s"$sweepRuntimeSym.${controlRelationScopeName(frameName)}"

  /////////////////////////////////////////////////////////////////////////////////////////////
  // controlMemberScope
  /////////////////////////////////////////////////////////////////////////////////////////////

  /**
   *
   * @param frameName
   * @return
   */
  final
  def controlMemberScopeName(frameName: String): String = s"control_${frameName}_member_scope"

  final
  def controlMemberScopeVarDecl(frameName: String): FeltVar = s"var ${controlMemberScopeName(frameName)} : Int = -1; // inactive"

  final
  def controlMemberScopeValue(frameName: String): FeltSweepVar = s"$sweepRuntimeSym.${controlMemberScopeName(frameName)}"

  /////////////////////////////////////////////////////////////////////////////////////////////
  // controlMemberScope
  /////////////////////////////////////////////////////////////////////////////////////////////

  final
  def controlDiscardScopeName(frameName: String): String = s"control_${frameName}_discard_scope"

  final
  def controlDiscardScopeVarDecl(frameName: String): FeltVar = s"var ${controlDiscardScopeName(frameName)} : Boolean = false;"

  final
  def controlDiscardScopeValue(frameName: String): FeltSweepVar = s"$sweepRuntimeSym.${controlDiscardScopeName(frameName)}"

}
