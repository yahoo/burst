/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.view;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Test;

public class MotifErrorSpec extends MotifAbstractParserSpec {

    @Test(expected = ParseException.class)
    public void membershipTooLowTest() {
        String expressionSource = "view membershipConstantTest {" +
                "include user where user.sessions.sessionId NOT IN (345, 346, 345+1, user.sessions.events.eventId);" +
                "}";
        Expression result = runExpressionTest(expressionSource);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void betweenTestOffAxis() {
        String source = "view betweenConstantTest {sample(0.6) user where user.sessions.startTime BETWEEN 1486743658 AND user.segments.segmentId;}";
        Expression result = runExpressionTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void betweenTestTooLow() {
        String source = "view betweenConstantTest {sample(0.6) user where user.sessions.startTime BETWEEN 1486743658 AND user.sessions.events.startTime;}";
        Expression result = runExpressionTest(source);
        assert result != null;
    }

}
