/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.literals

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.literals.mutable.FeltMutableLit
import org.burstsys.felt.model.literals.primitive.FeltPrimitive

/**
 * A literal is a data value that is created internal to a Felt Model specification such as from a parsed Hydra source
 * specification. Literals
 * are of type [[FeltPrimitive]] i.e. the simple scalar value literals '''boolean, short, integer, long, double''',
 * or '''string''', or
 * of type [[FeltMutableLit]] i.e. the value container literals '''map''' or '''vector'''.
 *
 * ===State===
 * The state associated with a literal is managed at runtime and is not
 * available external to the sweep except in so much as it's value is written to a collector
 * [[org.burstsys.felt.model.reference.FeltRefDecl]] (defined in external libraries).
 *
 * ===Nulls===
 * Literals always track nullity in a FELT conformation manner.
 *
 */
trait FeltLiteral extends FeltExpression {

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def reduceStatics: FeltLiteral = this

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = true

}
