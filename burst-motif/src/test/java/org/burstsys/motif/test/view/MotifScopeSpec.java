/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.view;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Test;

public class MotifScopeSpec extends MotifAbstractParserSpec {

    @Test
    public void scope4() {
        String expressionSource = "VIEW scope4 {INCLUDE user WHERE (COUNT(user.sessions.events) SCOPE user WHERE user.sessions.events.eventId == 1234) > 1}";
        runViewTest(expressionSource);
    }

    @Test(expected = ParseException.class)
    public void errorScope5() {
        // can't have a rolling quanta spec without a quanta
        ParseException t;
        String expressionSource = "VIEW errorScope5 {INCLUDE user WHERE (COUNT(user.sessions.events) ROLLING 1 WHERE user.sessions.events.eventId == 1234) > 1}";
        runViewTest(expressionSource);
    }

    @Test(expected = ParseException.class)
    public void errorScope6() {
        // rolling quanta has to be a constant
        ParseException t;
        String expressionSource = "VIEW errorScope6 {INCLUDE user WHERE (COUNT(user.sessions.events) SCOPE ROLLING user.flurryId user.sessions WHERE user.sessions.events.eventId == 1234) > 1}";
        runViewTest(expressionSource);
    }

}
