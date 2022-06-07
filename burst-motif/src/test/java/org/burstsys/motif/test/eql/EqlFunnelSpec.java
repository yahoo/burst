/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.eql;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.eql.common.Statements;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Before;
import org.junit.Test;

public class EqlFunnelSpec extends MotifAbstractParserSpec {
    @Before
    public void init() {
        schemaName = "unity";
    }

    @Test
    public void testSimpleFunnel() {
        String source = "" +
                "funnel test conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id in (1, 2, 3)\n" +
                "    ( 1 : 2 )" +
                "} from schema Unity\n" +
                "select count(user) as number, count(test.paths) as paths\n" +
                "from schema Unity, funnel test\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test
    public void testSimpleLimitWithinFunnel() {
        String source = "" +
                "funnel test conversion within minutes(2) limit 1000 {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id in (1, 2, 3)\n" +
                "    ( 1 : 2 )" +
                "} from schema Unity\n" +
                "select count(user) as number, count(test.paths) as paths\n" +
                "from schema Unity, funnel test\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testFailMissingStepFunnel() {
        String source = "" +
                "funnel test conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id in (1, 2, 3)\n" +
                "    1 : 2 : 3" +
                "} from schema Unity\n" +
                "select count(user) as number, count(test.paths) as paths\n" +
                "from schema Unity, funnel test\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test
    public void testOrFunnel() {
        String source = "" +
                "funnel test conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime within seconds(2) \n" +
                "    step 2 when user.sessions.events.id in (1, 2, 3)\n" +
                "    ( 1 : ( 1 | 2 ) )" +
                "} from schema Unity\n" +
                "select count(user) as number, count(test.paths) as paths\n" +
                "from schema Unity, funnel test\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test
    public void testBranchingOrFunnel() {
        String source = "" +
                "funnel test conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id in (1, 2, 3)\n" +
                "    step 3 when user.sessions.events.id in (1, 2, 3)\n" +
                "    step 4 when user.sessions.events.id in (1, 2, 3)\n" +
                "    step 10 when user.sessions.events.id in (1, 2, 3)\n" +
                "    1 : ( (1:2:3:10) | (2:3:4:10:1) | (3:4:10:1:2) | (4:10:1:2:3) ) : 10" +
                "} from schema Unity\n" +
                "select count(user) as number, count(test.paths) as paths\n" +
                "from schema Unity, funnel test\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test
    public void testWhereClauseFunnel() {
        String source = "" +
                "funnel test conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id in (1, 2, 3)\n" +
                "    ( 1 : ( 1 | 2 ) )\n" +
                "} from schema Unity\n" +
                "select count(test.paths) \n" +
                "where size(test.paths.steps) > 4 and test.paths.isComplete\n" +
                "from schema Unity, funnel test\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test
    public void testWhereClause2Funnel() {
        String source = "" +
                "funnel test conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id in (1, 2, 3)\n" +
                "    ( 1 : ( 1 | 2 ) )\n" +
                "} from schema Unity\n" +
                "select count(test.paths) as 'paths' \n" +
                "where test.paths.isComplete && test.paths.endTime - test.paths.startTime >= DAYS(1)\n" +
                "from schema Unity, funnel test\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test
    public void testTwoFunnelFunnel() {
        String source = "funnel 'cohort' conversion { \n" +
                "   step 1 when start of user timing on user.application.firstUse.sessionTime \n" +
                "   1\n" +
                " } from schema unity \n" +
                "\n" +
                "funnel 'measurement' transaction {\n" +
                "   step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "   step 2 when user.sessions.events.id == 4498117 timing on user.sessions.events.startTime\n" +
                "   step 3 when user.sessions.events.id == 4495000 timing on user.sessions.events.startTime \n" +
                "   step 5 when user.sessions.events.id == 5000000 timing on user.sessions.events.startTime\n" +
                "   step 4 when end of user.sessions\n" +
                "    1 : (2 | 3) : 5 \n" +
                "} from schema unity \n" +
                "\n" +
                "select unique(mf.paths) as 'users', \n" +
                "   count(mf.paths) as 'times', \n" +
                "   day(cf.paths.endTime) as cohort, \n" +
                "   day(mf.paths.endTime) - day(lastPathStepTime(cf)) as measure\n" +
                "   from funnel 'cohort' as 'cf', funnel 'measurement' as mf, schema unity\n" +
                "   where day(mf.paths.endTime) - day(lastPathStepTime(cf)) between days(-31) and days(31) and lastPathIsComplete(cf)\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test
    public void testParameterFunnel() {
        String source = "" +
                "funnel test(x: long) conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id == $x\n" +
                "    ( 1 : ( 1 | 2 ) )\n" +
                "} from schema Unity\n" +
                "select(x: long) count(test.paths) \n" +
                "where size(test.paths.steps) > 4 and test.paths.isComplete\n" +
                "from schema Unity, funnel test($x)\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test
    public void testStarFunnel() {
        String source = "" +
                "funnel test(x: long) conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id == $x\n" +
                "    ( 1 : ( 1 | 2 ) *)\n" +
                "} from schema Unity\n" +
                "select(x: long) count(test.paths) \n" +
                "where size(test.paths.steps) > 4 and test.paths.isComplete\n" +
                "from schema Unity, funnel test($x)\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test
    public void testQuestionFunnel() {
        String source = "" +
                "funnel test(x: long) conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id == $x\n" +
                "    ( 1 : ( 1 | 2 ) ?)\n" +
                "} from schema Unity\n" +
                "select(x: long) count(test.paths) \n" +
                "where size(test.paths.steps) > 4 and test.paths.isComplete\n" +
                "from schema Unity, funnel test($x)\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test
    public void testPlusFunnel() {
        String source = "" +
                "funnel test(x: long) conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id == $x\n" +
                "    ( 1 : ( 1 | 2 ) +)\n" +
                "} from schema Unity\n" +
                "select(x: long) count(test.paths) \n" +
                "where size(test.paths.steps) > 4 and test.paths.isComplete\n" +
                "from schema Unity, funnel test($x)\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test
    public void testRangeFunnel() {
        String source = "" +
                "funnel test(x: long) conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id == $x\n" +
                "    ( 1 : ( 1 | 2 ) {8,*} : 1{*,4} : 2{*,*} : 1{1,3})\n" +
                "} from schema Unity\n" +
                "select(x: long) count(test.paths) \n" +
                "where size(test.paths.steps) > 4 and test.paths.isComplete\n" +
                "from schema Unity, funnel test($x)\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testInvalidParameterTypeMismatchFunnel() {
        String source = "" +
                "funnel test(x: string) conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id == $x\n" +
                "    ( 1 : ( 1 | 2 ) )\n" +
                "} from schema Unity\n" +
                "select(x: long) count(test.paths) \n" +
                "where size(test.paths.steps) > 4 and test.paths.isComplete\n" +
                "from schema Unity, funnel test($x)\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testInvalidParameterNotParameterFunnel() {
        String source = "" +
                "funnel test(x: long) conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id == $x\n" +
                "    ( 1 : ( 1 | 2 ) )\n" +
                "} from schema Unity\n" +
                "select(x: long) count(test.paths) \n" +
                "where size(test.paths.steps) > 4 and test.paths.isComplete\n" +
                "from schema Unity, funnel test(user.sessions)\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testInvalidParameterBadTypeFunnel() {
        String source = "" +
                "funnel test(x: long) conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id == $x\n" +
                "    ( 1 : ( 1 | 2 ) )\n" +
                "} from schema Unity\n" +
                "select(y: string) count(test.paths) \n" +
                "where size(test.paths.steps) > 4 and test.paths.isComplete\n" +
                "from schema Unity, funnel test($y)\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test
    public void testBracesFunnel() {
        String source = "" +
                "funnel test conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id == 2\n" +
                "    step 3 when user.sessions.events.id == 3\n" +
                "    1 : [1 2] : 2\n" +
                "} from schema Unity\n" +
                "select count(test.paths) \n" +
                "from schema Unity, funnel test\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test
    public void testNegatingBracesFunnel() {
        String source = "" +
                "funnel test conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id == 2\n" +
                "    step 3 when user.sessions.events.id == 3\n" +
                "    1 : [^1 2] : 2\n" +
                "} from schema Unity\n" +
                "select count(test.paths) \n" +
                "from schema Unity, funnel test\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testInvalidBracesFunnel() {
        String source = "" +
                "funnel test conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id == 2\n" +
                "    step 3 when user.sessions.events.id == 3\n" +
                "    1 : [^(1 | 2)] : 2\n" +
                "} from schema Unity\n" +
                "select count(test.paths) \n" +
                "from schema Unity, funnel test\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test
    public void testNonCaptureFunnel() {
        String source = "" +
                "funnel test(x: long) conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id == $x\n" +
                "    1 : (?: 1 : 2 ) : 2\n" +
                "} from schema Unity\n" +
                "select(x: long) count(test.paths) \n" +
                "where size(test.paths.steps) > 4 and test.paths.isComplete\n" +
                "from schema Unity, funnel test($x)\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testBadOrNonCaptureFunnel() {
        String source = "" +
                "funnel test(x: long) conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id == $x\n" +
                "    1 | (?: 1 : 2 ) | 2\n" +
                "} from schema Unity\n" +
                "select(x: long) count(test.paths) \n" +
                "where size(test.paths.steps) > 4 and test.paths.isComplete\n" +
                "from schema Unity, funnel test($x)\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test
    public void testValidOrNonCaptureFunnel() {
        String source = "" +
                "funnel test(x: long) conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id == $x\n" +
                "    1: ((?: 1 : 2) | (?: 1 : 2 ) | (?: 1 : 2)) : 2\n" +
                "} from schema Unity\n" +
                "select(x: long) count(test.paths) \n" +
                "where size(test.paths.steps) > 4 and test.paths.isComplete\n" +
                "from schema Unity, funnel test($x)\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testBadNonCaptureStartFunnel() {
        String source = "" +
                "funnel test(x: long) conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id == $x\n" +
                "    (?: 1*) :  1 : 2 :  2*\n" +
                "} from schema Unity\n" +
                "select(x: long) count(test.paths) \n" +
                "where size(test.paths.steps) > 4 and test.paths.isComplete\n" +
                "from schema Unity, funnel test($x)\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testBadNonCaptureEndFunnel() {
        String source = "" +
                "funnel test(x: long) conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id == $x\n" +
                "     1* :  1 : 2 :  (?: 2*)\n" +
                "} from schema Unity\n" +
                "select(x: long) count(test.paths) \n" +
                "where size(test.paths.steps) > 4 and test.paths.isComplete\n" +
                "from schema Unity, funnel test($x)\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }
}
