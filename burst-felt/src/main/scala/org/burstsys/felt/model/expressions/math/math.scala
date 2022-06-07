/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.expressions

import org.burstsys.felt.model.brio.FeltReachValidator

package object math {

  trait FeltMathExpr extends FeltExpression with FeltReachValidator

}
