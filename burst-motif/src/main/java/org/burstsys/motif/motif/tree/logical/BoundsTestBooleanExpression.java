/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.logical;

import org.burstsys.motif.motif.tree.values.ValueExpression;

public interface BoundsTestBooleanExpression extends BooleanExpression {

    BoundsTestOperatorType getOp();

    ValueExpression getLeft();

    ValueExpression getLower();

    ValueExpression getUpper();
}
