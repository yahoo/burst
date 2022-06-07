/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.logical;

public interface BinaryBooleanExpression extends BooleanExpression {
    BinaryBooleanOperatorType getOp();

    BooleanExpression getLeft();

    BooleanExpression getRight();
}
