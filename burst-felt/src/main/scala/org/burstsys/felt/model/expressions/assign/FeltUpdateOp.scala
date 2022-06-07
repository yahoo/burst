/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions.assign

import org.burstsys.felt.model.expressions.op.FeltOperator

/**
 * a special type of assignment that does an 'update' e.g. increment, decrement. Not all reference
 * types can do all types of updates
 */
trait FeltUpdateOp extends FeltOperator {

  final override val nodeName = "felt-update-op"

}

/**
 * mathematical add
 */
trait PLUS_EQ extends FeltUpdateOp {

  final val symbol = "+="

}

/**
 * mathematical subtraction
 */
trait MINUS_EQ extends FeltUpdateOp {

  final val symbol = "-="

}


