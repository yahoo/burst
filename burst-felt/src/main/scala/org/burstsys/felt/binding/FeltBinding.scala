/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.binding

import org.burstsys.felt.model.collectors.FeltCollectorProviders
import org.burstsys.felt.model.mutables.FeltMutableProviders
import org.burstsys.felt.model.runtime.FeltRuntime
import org.burstsys.felt.model.sweep.FeltSweep

/**
 * This must be provided by the concrete binding based on Felt. Currently this is only the '''Hydra''' language.
 * The Felt system is only designed to support creation of code generating tree forms that model the basic
 * Burst style parallel scanning analytics framework. It is agnostic to the exact end user model for specifying
 * the tree semantics however.
 */
trait FeltBinding extends Any {

  /**
   * the ''human friendly'' name for this binding.
   *
   * @return
   */
  def name: String

  /**
   * the subtype of [[FeltSweep]] that is required/provided by this concrete binding
   *
   * @return
   */
  def sweepClass: Class[_ <: FeltSweep]

  /**
   * the subtype of [[FeltRuntime]] that is required/provided by this concrete binding
   *
   * @return
   */
  def sweepRuntimeClass: Class[_ <: FeltRuntime]

  def mutables: FeltMutableProviders

  def collectors: FeltCollectorProviders

}
