/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.expression;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Test;

public class MotifExpressionSpec extends MotifAbstractParserSpec {

    @Test
    public void math() {
        String expressionSource = "(5999 % 34) == 0";
        Expression result = runExpressionTest(expressionSource);
        assert result.canReduceToConstant();
    }

    @Test
    public void logic() {
        String expressionSource = "FALSE && TRUE";
        Expression result = runExpressionTest(expressionSource);
        assert result.canReduceToConstant();
    }

    @Test
    public void modulo() {
        String expressionSource = "user.sessions.events.eventId == 2789345 || (5999 % 34) == 0";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void testSchemaReferences1() {
        String expressionSource = "user.sessions.events.eventId == 2789345 || FALSE && TRUE";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void testSchemaReferences2() {
        String expressionSource = "user.sessions.events.eventId == 2789345 or FALSE AND TRUE";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void testMapKeys() {
        String expressionSource = "cast(user.sessions.events.parameters['foo bar'] as long) == 2789345 || FALSE && TRUE";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test(expected = ParseException.class)
    public void testMapTyping() {
        String expressionSource = "user.sessions.events.parameters['foo bar'] == 2789345 || FALSE && TRUE";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void testParameterization() {
        String expressionSource = "user.sessions.events.parameters['foo bar'] == 'hello' || (FALSE && user.sessions.events.eventId == 2789345)";
        Expression result = runExpressionTest(expressionSource);
        assert result != null;
    }

    @Test
    public void testCast() {
        String expressionSource = "cast( user.sessions.events.parameters['foo bar'] as boolean) == true";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void testNegation() {
        String expressionSource = "CAST(user.sessions.events.parameters['foo bar'] AS LONG) == -(user.sessions.startTime)";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test(expected = ParseException.class)
    public void testNegationTyping() {
        String expressionSource = "user.sessions.events.parameters['foo bar'] == -(user.sessions.startTime)";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void testMath() {
        String expressionSource = "4.66 + ((1 / 2.3) * 3) - 3";
        Expression result = runExpressionTest(expressionSource);
        assert result.canReduceToConstant();
    }

    @Test
    public void testBetween1() {
        String expressionSource = "user.sessions.events.eventId not between 300 and 600";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void testBetween2() {
        String expressionSource = "user.sessions.events.eventId between 300 and 600";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void testInList() {
        String expressionSource = "user.sessions.events.eventId not in (345, 346, 36, 6000)";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void testIsNull1() {
        String expressionSource = "user.sessions.events.eventId is not null";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void testIsNull2() {
        String expressionSource = "user.sessions.events.eventId is null";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void testSubscript() {
        String expressionSource = "user.sessions.events.eventId is not null OR (TRUE)";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void testAggregation() {
        String expressionSource = "count(user.sessions.events) >= (3*user.sessions.startTime) OR count(user.sessions.events) >= sum(user.project.projectId)";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void testNot() {
        String expressionSource = "NOT(TRUE)";
        Expression result = runExpressionTest(expressionSource);
        assert result.canReduceToConstant();
    }

    @Test
    public void testAggregate() {
        String expressionSource = "count(user.sessions) < sum(user.sessions.startTime) || max(user.sessions.startTime) < min(user.sessions.startTime)";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }


}
