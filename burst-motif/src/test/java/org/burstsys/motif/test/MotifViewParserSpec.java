/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.rule.FilterRule;
import org.burstsys.motif.motif.tree.view.View;
import org.junit.Test;

public class MotifViewParserSpec extends MotifAbstractParserSpec {

    @Test
    public void test0() {
        String expressionSource = "view \"SAMPLE\" {\n" +
                "include user where true" +
                "}";
        View result = runViewTest(expressionSource);
        for (FilterRule rule : result.getRules()) {
            assert rule.getWhere().canReduceToConstant();
        }
    }

    @Test
    public void test1() {
        String expressionSource = "view \"SAMPLE\" {\n" +
                "include user where user.sessions.events.eventId == 2789345 || (5999 % 34) == 0\n" +
                "}";
        View result = runViewTest(expressionSource);
        for (FilterRule rule : result.getRules()) {
            assert !rule.getWhere().canReduceToConstant();
        }
    }

    @Test
    public void constantWhere() {
        String expressionSource = "view test {include user where (5999 % 34) == 0;}";
        View result = runViewTest(expressionSource);
        for (FilterRule rule : result.getRules()) {
            assert rule.getWhere().canReduceToConstant();
        }
    }

    @Test
    public void test2() {
        String expressionSource =
                "view test {\n" +
                        "include user where (user.sessions.events.eventId == 2789345 OR user.sessions.events.eventId == 2789346)\n" +
                        "include user.sessions WHERE not(user.sessions.startTime BETWEEN 998723456 and 2568973456)\n" +
                        "}";
        View result = runViewTest(expressionSource);

        for (FilterRule rule : result.getRules()) {
            assert !rule.getWhere().canReduceToConstant();
        }
    }

    @Test(expected = ParseException.class)
    public void error1() {
        String expressionSource =
                "view test {\n" +
                        "include user where (user.sessions.events.eventId == 2789345 OR user.sessions.events.eventId == 2789346)\n" +
                        "include user.sessions WHERE not(user.sessions.startTime BETWEEN 998723456 and 2568973456)\n" +
                        "include user WHERE count(user.sessions.events) < 500\n" +
                        "}";
        View result = runViewTest(expressionSource);

        for (FilterRule rule : result.getRules()) {
            assert !rule.getWhere().canReduceToConstant();
        }
    }

    @Test
    public void filter3() {
        String expressionSource =
                "view filter3 {\n" +
                        "include user where (user.sessions.events.eventId == 2789345 OR user.sessions.events.eventId == 2789346)\n"
                        + " AND  user.sessions.startTime BETWEEN 998723456 and 2568973456\n"
                        + " OR  count(user.sessions.events) < 500\n" +
                        "}";
        View result = runViewTest(expressionSource);

        for (FilterRule rule : result.getRules()) {
            assert !rule.getWhere().canReduceToConstant();
        }
    }


    @Test
    public void filter4() {
        String source = "view filter4 {include user where user.project.installTime between 1453735174 and 1485357578;}";
        View result = runViewTest(source);
        for (FilterRule rule : result.getRules()) {
            assert !rule.getWhere().canReduceToConstant();
        }
    }

    @Test
    public void filter6() {
        String expressionSource = "view filter6 {include user.sessions where true;}";
        View result = runViewTest(expressionSource);
        assert result != null;
    }

    @Test
    public void filter7() {
        String expressionSource = "view filter7 {include user.sessions}";
        View result = runViewTest(expressionSource);
        assert result != null;
    }

    @Test
    public void filter9() {
        String expressionSource = "view filter9 {include user where user.flurryId is null}";
        View result = runViewTest(expressionSource);
        assert result != null;
    }

    @Test
    public void filter10() {
        String expressionSource = "view filter10 {\n" +
                "include user where user.flurryId is not null \n" +
                "include user.sessions where user.sessions.startTime is not null \n" +
                "}";
        View result = runViewTest(expressionSource);
        assert result != null;
    }

    /** One pre and one post sample rule is okay. */
    @Test
    public void filter11() {
        String expressionSource = "view filter11 {\n" +
                "include user where user.flurryId is not null \n" +
                "include user.sessions where user.sessions.startTime is not null \n" +
                "}";
        View result = runViewTest(expressionSource);
        assert result != null;
    }

    /** More than one pre sample rule not okay. */
    @Test(expected = ParseException.class)
    public void filter12pre() {
        String expressionSource = "view filter12 {\n" +
                "presample(1.0) \n" +
                "presample(0.1) \n" +
                "include user where user.flurryId is not null \n" +
                "include user.sessions where user.sessions.startTime is not null \n" +
                "}";
        View result = runViewTest(expressionSource);
        assert result != null;
    }

    /** More than one post sample rule not okay. */
    @Test(expected = ParseException.class) // Max one post-sample rule allowed.
    public void filter12post() {
        String expressionSource = "view filter12 {\n" +
                "postsample(1.0) \n" +
                "postsample(0.1) \n" +
                "include user where user.flurryId is not null \n" +
                "include user.sessions where user.sessions.startTime is not null \n" +
                "}";
        View result = runViewTest(expressionSource);
        assert result != null;
    }

    @Test
    public void filter14() {
        String expressionSource =
                "VIEW v { " +
                        "INCLUDE user WHERE " +
                        "(COUNT(user.sessions.events) WHERE (user.sessions.events.eventId == 19310758 AND CAST(user.sessions.events.parameters['fl.CampaignId'] AS long) == 1047)) >= 1 " +
                 "}";
        View result = runViewTest(expressionSource);
        assert result != null;
    }
}
