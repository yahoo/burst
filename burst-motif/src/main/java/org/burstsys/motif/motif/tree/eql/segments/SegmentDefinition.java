/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.segments;

import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;
import org.burstsys.motif.schema.model.MotifSchema;

/**
 * Placeholder for all motif statements
 */
public interface SegmentDefinition extends Evaluation {
    BooleanExpression getWhere();

    Long getName();
}
