/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.values;

import org.burstsys.motif.common.DataType;

public interface BinaryValueExpression extends ValueExpression {
    BinaryValueOperatorType getOp();

    ValueExpression getLeft();

    ValueExpression getRight();

    DataType getDtype();
}
