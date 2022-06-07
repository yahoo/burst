/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.values;

import org.burstsys.motif.motif.tree.expression.Expression;

public interface AggregationValueExpression extends ValueExpression {

    /**
     * the aggregation source path
     */
    Expression getExpr();

    /**
     * The aggregate operator
     */
    AggregationOperatorType getOp();

    /**
     * conditional on aggregate,  null if no restriction
     */
    Expression getWhere();

    /**
     * the scope of this aggregate
     */
    Expression getScope();

    /**
     * is this just a default scope aggregation suitable for top level aggregations
     */
    boolean atRootScope();

    /**
     * for a rolling scope the number of slots
     */
    Expression getQuanta();

    /**
     * is this just a default quanta aggregation suitable for top level aggregations
     */
    boolean hasDefaultQuanta();

    /**
     * the number of items the aggregate can hold...usually 1 for most, but TOP has a size
     */
    Integer getSize();

}
