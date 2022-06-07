/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.view;

import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;
import org.burstsys.motif.motif.tree.rule.FilterRule;

public interface View extends Evaluation {

    /**
     * the rule set
     *
     */
    FilterRule[] getRules();

    String getName();

    /**
     * Create a union merge the rules of the provided view and this view.
     * @param view the incoming view
     * @return the union merged view
     */
    View unionView(View view);

    /**
     * Create a intersect merge the rules of the provided view and this view.
     * @param view the incoming view
     * @return the intersect merged view
     */
    View intersectView(View view);

    /**
     * Create a complement view merge of this view.
     * @return the complemented view
     */
    View complementView();


    /**
     * Normalize all root rules in a view into a single predicate suitable for use
     * in a single INCLUDE rule
     */
    BooleanExpression rootFilterPredicate();

    /**
     * The identifier of the time zone used in this View to evaluate expressions whose source
     * did not name a time zone.
     * @return the View's default time zone name
     * @see java.util.TimeZone#getAvailableIDs
     */
    String defaultTimeZoneName();

}
