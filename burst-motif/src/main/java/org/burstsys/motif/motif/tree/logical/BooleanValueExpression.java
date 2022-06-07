/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.logical;

import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.values.ValueExpression;

/**
 * Mark a general expression as a boolean expression
 */
public interface BooleanValueExpression extends BooleanExpression, ValueExpression {
    Expression getExpr();
}
