/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.expression;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.paths.Path;

/**
 * The core type in motif filter expressions
 *
 * @see org.burstsys.motif.motif.tree.values.ValueExpression
 * @see org.burstsys.motif.motif.tree.logical.BooleanExpression
 * @see Constant
 */
public interface Expression extends Evaluation, Parent {


    /**
     * True if this value expression can resolve to a compile time constant value
     *
     * @return
     */
    Boolean canReduceToConstant();

    /**
     * if this is reduceable, then return the constant result of that expression
     *
     * @return
     */
    Constant reduceToConstant();

    /**
     * return the datatype of this expression
     *
     * @return
     */
    DataType getDtype();

    /**
     * Report the structure in the overall object where this expression must
     * be evaluated.  If there is no such place,  it is off-axis for example,
     * then this will return a null
     * @return
     */
    Path getLowestEvaluationPoint();

}
