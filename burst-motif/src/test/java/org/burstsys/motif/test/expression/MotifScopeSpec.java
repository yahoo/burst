/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.expression;

import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Test;

public class MotifScopeSpec extends MotifAbstractParserSpec {

    @Test
    public void scope1() {
        String expressionSource = "(COUNT(user.sessions.events) SCOPE ROLLING 30 DAY(user.sessions.startTime)) > 4";
        runExpressionTest(expressionSource);
    }

    @Test
    public void scope2() {
        String expressionSource = "(COUNT(user.sessions.events) SCOPE user.sessions) > 4";
        runExpressionTest(expressionSource);
    }

    @Test
    public void scope3() {
        String expressionSource = "(COUNT(user.sessions.events) " +
                " SCOPE ROLLING 2*10 HOUR(user.sessions.events.startTime)) > 4";
        runExpressionTest(expressionSource);
    }

}
