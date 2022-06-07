/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.tree.code

import java.util.concurrent.atomic.AtomicLong

/**
 * much of FELT expressions breaks down into recursive function calls which
 * are defined as 'scopes'
 */
trait FeltCodeScope {

  /**
   * the string name for this scope
   *
   * @return
   */
  def scopeName: String

  /**
   * optional parent scope - if there is none, this is the root scope
   *
   * @return
   */
  def parentScope: Option[FeltCodeScope]

  /**
   * the ''return'' '''value''' for the current scope
   *
   * @return
   */
  def scopeVal: String

  /**
   * the ''return''  '''nullity''' for the current scope
   *
   * @return
   */
  def scopeNull: String

  def scopeClock: AtomicLong


  final override
  def toString: String = scopeName

}

object FeltCodeScope {


  /**
   * root call scope
   *
   * @return
   */
  def apply(): FeltCodeScope = FeltCodeScopeContext(parentScope = None)

  /**
   * call scope constructor
   *
   * @param parentScope
   * @return
   */
  def apply(parentScope: FeltCodeScope): FeltCodeScope = FeltCodeScopeContext(parentScope = Some(parentScope))

}

private final case
class FeltCodeScopeContext(parentScope: Option[FeltCodeScope]) extends FeltCodeScope {

  private[this]
  val _scopeClock: AtomicLong = new AtomicLong()

  def scopeClock: AtomicLong = if (parentScope.isEmpty) _scopeClock else parentScope.get.scopeClock

  val scopeName: String = s"s_${scopeClock.getAndIncrement}"

  val scopeVal: String = s"${scopeName}_ScpVal"

  val scopeNull: String = s"${scopeName}_ScpNull"

  lazy val parentScopeName: Object = parentScope.getOrElse("TOP")

  val parentScopeVal: String = s"${parentScopeName}_ScpVal"

  val parentScopeNull: String = s"${parentScopeName}_ScpNull"


}
