/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.view;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.view.View;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Before;
import org.junit.Test;

public class MotifAggregateSpec extends MotifAbstractParserSpec {

    @Before
    public void init() {
        schemaName = "unity";
    }

    @Test
    public void test3() {
        String source = "view \"sample\" {\n" +
                "INCLUDE user WHERE (COUNT(user.sessions) WHERE user.sessions.startTime BETWEEN 1234567890123 AND 1234567890999) > 10;" +
                "}";
        View result = runViewTest(source);
        assert result != null;
    }

    @Test
    public void test4() {
        String source = "" +
                "VIEW v {" +
                "     INCLUDE user WHERE (" +
                "         DAY(user.application.firstUse.sessionTime) == DAY(NOW - DAYS(1))) AND " +
                "         (COUNT(user.sessions) where DAY(user.sessions.events.startTime) == DAY(NOW - DAYS(1))) == 0 " +
                "}";
        View result = runViewTest(source);
        assert result != null;
    }

    @Test
    public void test5() {
        String source = "VIEW v1 {INCLUDE user WHERE (COUNT(user.sessions.events) "
                + "WHERE user.sessions.events.id == 9873456 AND user.sessions.startTime BETWEEN 1483228800 AND 1486489940"
                + ") > 10}";
        View result = runViewTest(source);
        assert result != null;
    }

    @Test
    public void test6() {
        String source = "VIEW v1 {INCLUDE user WHERE (COUNT(user.sessions.events) SCOPE user.sessions "
                        + "WHERE user.sessions.events.id == 9873456 AND user.sessions.startTime BETWEEN 1483228800 AND 1486489940"
                        + ") > 10}"
;
        View result = runViewTest(source);
        assert result != null;
    }

    @Test
    public void test7() {
        String source = "VIEW v { "+
                "INCLUDE user WHERE (COUNT(user.sessions) WHERE user.application.lastUse.sessionTime >= (DAY(NOW) - DAYS(1)) " +
                "    && user.application.lastUse.sessionTime < DAY(NOW) ) > 0 "+
                "INCLUDE user.sessions WHERE user.sessions.startTime >= (DAY(NOW) - DAYS(1)) && user.sessions.startTime < DAY(NOW) "+
                "INCLUDE user.sessions.events where false "+
                "}" ;
        View result = runViewTest(source);
        assert result != null;
    }

    @Test
    public void test8() {
        String source = "VIEW x { "
                + "INCLUDE user WHERE (COUNT(user.sessions.events) WHERE user.sessions.startTime >= (NOW - DAYS(30)) && user.sessions.events.id == 14931772) > 0 "
                + "INCLUDE user.sessions WHERE user.sessions.startTime >= (NOW - DAYS(30)) && (COUNT(user.sessions.events) SCOPE user.sessions WHERE user.sessions.events.id == 14931772) > 0 "
                + "}";
        View result = runViewTest(source);
        assert result != null;
    }

    @Test
    public void test9() {
        String source = "VIEW x { "
                + "INCLUDE user WHERE (COUNT(user.sessions.events) WHERE user.sessions.startTime >= (NOW - DAYS(30)) && user.sessions.events.id == 14931772) > 0 "
                + "INCLUDE user.sessions WHERE user.sessions.startTime >= (NOW - DAYS(30)) && (COUNT(user.sessions.events) WHERE user.sessions.events.id == 14931772) > 0 "
                + "}";
        View result = runViewTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void test10() {
        String source = "VIEW v { INCLUDE user.sessions.events WHERE user.sessions.events.id IN (1001, 1002, 1003, 1004) "
                + "OR user.application.channels.campaignId IN (1, 2, 3, 4) }";
        View result = runViewTest(source);
        assert result != null;
    }
}
