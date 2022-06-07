/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.expression;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.BeforeClass;
import org.junit.Test;

public class MotifAggregateSpec extends MotifAbstractParserSpec {

    @Test
    public void basicCount() {
        String expressionSource = "count(user.sessions.events)";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void basicCountWithWhere() {
        String expressionSource = "(COUNT(user.sessions.events) WHERE user.project.installTime BETWEEN 1487169134 and 1487170134) > 10";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test(expected = ParseException.class)
    public void countOverBadField() {
        String expressionSource = "COUNT(user.sessions.events.eventId)";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }


    @Test
    public void basicSum() {
        String expressionSource = "SUM(user.sessions.events.eventId)";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void basicSumWithWhere() {
        String expressionSource = "(SUM(user.sessions.events.eventId) WHERE user.project.installTime BETWEEN 1487169134 and 1487170134) > 10";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test(expected = ParseException.class)
    public void sumOverBadField() {
        String expressionSource = "SUM(user.sessions.events)";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void constantSumWithWhere() {
        String expressionSource = "(sum(1 + 2) scope user.sessions where user.sessions.startTime % 2 == 0)";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }


}
