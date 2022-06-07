/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.expression;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Test;

public class MotifErrorSpec extends MotifAbstractParserSpec {

    @Test(expected = ParseException.class)
    public void test1() {
        String expressionSource = "user.sessions.event.eventId == 2789345";
        Expression result = runExpressionTest(expressionSource);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void test2() {
        String expressionSource = "user.sessions.events.eventId['456'] == 2789345";
        Expression result = runExpressionTest(expressionSource);
        assert result != null;
    }

    /*
    @Test(expected = ParseException.class)
    public void test3() {
        String expressionSource = "user.sessions.events.parameters[987 356] == 2789345";
        Expression result = runExpressionTest(expressionSource);
        assert result != null;
    }
     */

    @Test(expected = ParseException.class)
    public void test4() {
        String expressionSource = "user.sessions.events.foo == 2789345";
        Expression result = runExpressionTest(expressionSource);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void test5() {
        String expressionSource = "user.sessions.events.eventId == sum(8973456 as DOUBLE)";
        Expression result = runExpressionTest(expressionSource);
        assert result != null;
    }

    // off axis comparison
    @Test(expected = ParseException.class)
    public void test6() {
        String expressionSource = "user.sessions.events.eventId == user.segments.segmentId";
        Expression result = runExpressionTest(expressionSource);
        assert result != null;
    }

    // off axis addition
    @Test(expected = ParseException.class)
    public void test7() {
        String expressionSource = "user.sessions.events.eventId + user.segments.segmentId";
        Expression result = runExpressionTest(expressionSource);
        assert result != null;
    }

    // invalid scope
    @Test(expected = ParseException.class)
    public void test9() {
        String expressionSource = "(count(user.sessions) scope user.sessions.events where user.sessions.startTime between 1 and 2)";
        Expression result = runExpressionTest(expressionSource);
        assert result != null;
    }
}
