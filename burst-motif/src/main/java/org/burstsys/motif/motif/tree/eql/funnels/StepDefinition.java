/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.funnels;

import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;

public interface StepDefinition extends Evaluation {
    Long getId();

    BooleanExpression getWhen();
}
