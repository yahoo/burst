/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.expression;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.constant.BooleanConstant;
import org.burstsys.motif.motif.tree.constant.LongConstant;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Test;

public class MotifTimeSpec extends MotifAbstractParserSpec {

    @Test
    public void time1() {
        String expressionSource = "user.sessions.startTime > NOW";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void time2() {
        String expressionSource = "year(user.sessions.startTime) > 0";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void time3() {
        String expressionSource = "day(user.sessions.startTime) > 0";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void time4() {
        String expressionSource = "month(user.sessions.startTime) > MONTH(now)";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void time5() {
        String expressionSource = "WEEKS(2)";
        Expression result = runExpressionTest(expressionSource);
        assert result.canReduceToConstant();
        LongConstant value = (LongConstant) result.reduceToConstant();
        Long longValue = value.getLongValue();
        assert longValue.equals(2 * 7 * 24 * 60 * 60 * 1000L);
    }

    @Test
    public void time6() {
        String expressionSource = "WEEKS(3) > DAYS(20)";
        Expression result = runExpressionTest(expressionSource);
        assert result.canReduceToConstant();
        BooleanConstant value = (BooleanConstant) result.reduceToConstant();
        assert value.getBooleanValue();
    }

    @Test
    public void time7() {
        // TODO nonsense expression for use of path
        String expressionSource = "WEEKS(user.sessions.events.duration)";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void time8() {
        String expressionSource = "month(user.sessions.startTime, -7) > MONTH(now)";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void time9() {
        String expressionSource = "month(user.sessions.startTime, 'America/Los_Angeles') > MONTH(now)";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test
    public void time10() {
        String expressionSource = "month(user.sessions.startTime, user.sessions.startTime % 7) > MONTH(now)";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test(expected = ParseException.class)
    public void timeE1() {
        // invalid offset
        String expressionSource = "month(user.sessions.startTime, -33) > MONTH(now)";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test(expected = ParseException.class)
    public void timeE2() {
        // invalid id
        String expressionSource = "month(user.sessions.startTime, 'GobblyGook') > MONTH(now)";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }

    @Test(expected = ParseException.class)
    public void timeE3() {
        // invalid expression type
        String expressionSource = "month(user.sessions.startTime, user.sessions.startTime > 0";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }
}
