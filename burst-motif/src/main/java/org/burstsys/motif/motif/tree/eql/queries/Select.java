/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.queries;

import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;

import java.util.List;

public interface Select extends Evaluation {

    String getName();

    BooleanExpression getWhere();

    List<Target> getTargets();

    Integer getLimit();
}
