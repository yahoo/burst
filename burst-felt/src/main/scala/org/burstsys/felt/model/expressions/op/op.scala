/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions

package object op {

  /**
   * base type for any expression that uses a [[FeltOperator]]
   */
  trait FeltOpExpr extends FeltExpression {

    /**
     * the operator for the operator expression
     *
     * @return
     */
    def op: FeltOperator

  }

}
