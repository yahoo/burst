/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.eql;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.eql.queries.Query;
import org.burstsys.motif.motif.tree.eql.common.Statements;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Test;

public class EqlParameterSpec extends MotifAbstractParserSpec {

    @Test
    public void testSimpleSingle() {
        String source = "" +
                "select(dys: integer) unique(user.sessions) as user, \n" +
                "top[20](user.sessions.parameters.key) as key \n" +
                "where user.application.firstUse.sessionTime >= (NOW - DAYS($dys)) \n" +
                "from schema Unity\n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test
    public void testSimpleQuotedSingle() {
        String source = "" +
                "select('days': integer) unique(user.sessions) as user \n" +
                "where user.application.firstUse.sessionTime >= (NOW - DAYS($'days')) \n" +
                "from schema Unity\n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test
    public void testSimpleDoubleQuotedSingle() {
        String source = "" +
                "select(\"days\": integer) unique(user.sessions) as user \n" +
                "from schema Unity where " +
                " 30 == $\"days\"" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test
    public void testSimpleMany() {
        String source = "" +
                "select(dys: integer, hrs: byte, 'seconds': long) unique(user.sessions) as user \n" +
                "where user.application.firstUse.sessionTime >= (NOW - DAYS($dys) - HOURS($hrs)) \n" +
                "from schema Unity\n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test
    public void testSimpleVector() {
        String source = "" +
                "select(evntParms: vector[integer], sessionParms: vector[string]) " +
                "unique(user.sessions) as user \n" +
                "where user.application.firstUse.sessionTime >= (NOW - DAYS(30)) \n" +
                "from schema Unity\n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test
    public void testParallelSeparateWhere() {
        String source = "" +
                "select(dys: long) count(user.sessions) as sessions, \n" +
                "       user.sessions.appVersion as appVersion \n" +
                " where user.sessions.startTime < now - days($dys)" +
                "beside \n" +
                "select count(user) as users, user.deviceModelId as deviceModel \n" +
                "where user.sessions.startTime < now - days($dys) " +
                "from schema Unity\n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test
    public void testParallelCommonWhere() {
        String source = "" +
                "select(dys: long) count(user.sessions) as sessions, \n" +
                "       user.sessions.appVersion as appVersion \n" +
                "beside \n" +
                "select count(user) as users, user.deviceModelId as deviceModel \n" +
                "from schema Unity\n" +
                " where user.sessions.startTime < now - days($dys)" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testBadParameterParallelSeparateWhere() {
        String source = "" +
                "select(dys: string) count(user.sessions) as sessions, \n" +
                "       user.sessions.appVersion as appVersion \n" +
                " where user.sessions.startTime < now - days($dys)" +
                "beside \n" +
                "select count(user) as users, user.deviceModelId as deviceModel \n" +
                "where user.sessions.startTime < now - days($dys) " +
                "from schema Unity\n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testBadParameterTypeParallelCommonWhere() {
        String source = "" +
                "select(dys: string) count(user.sessions) as sessions, \n" +
                "       user.sessions.appVersion as appVersion \n" +
                "beside \n" +
                "select count(user) as users, user.deviceModelId as deviceModel \n" +
                "from schema Unity\n" +
                " where user.sessions.startTime < now - days($dys)" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testBadSeparateWhere() {
        String source = "" +
                "select(dys: string) count(user.sessions) as sessions, \n" +
                "       user.sessions.appVersion as appVersion \n" +
                " where now - days($dys)" +
                "beside \n" +
                "select count(user) as users, user.deviceModelId as deviceModel \n" +
                "where user.sessions.startTime < now - days($dys) " +
                "from schema Unity\n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testBadCommonWhere() {
        String source = "" +
                "select(dys: string) count(user.sessions) as sessions, \n" +
                "       user.sessions.appVersion as appVersion \n" +
                "beside \n" +
                "select count(user) as users, user.deviceModelId as deviceModel \n" +
                "from schema Unity\n" +
                " where now - days($dys)" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testErrorParallel() {
        String source = "" +
                "select count(user.sessions) as sessions, \n" +
                "       user.sessions.appVersion as appVersion \n" +
                "beside \n" +
                "select(dys: string) count(user) as users, user.deviceModelId as deviceModel \n" +
                "from schema Unity\n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test
    public void testParameterFunnel() {
        String source = "" +
                "funnel test(event: long) conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id == $event\n" +
                "    ( 1 : 2 )" +
                "} from schema Unity\n" +
                "select count(user) as number, count(test.paths) as paths\n" +
                "from schema Unity, funnel test(20)\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testErrorParameterFunnel() {
        String source = "" +
                "funnel test(event: string) conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id == $event\n" +
                "    ( 1 : 2 )" +
                "} from schema Unity\n" +
                "select count(user) as number, count(test.paths) as paths\n" +
                "from schema Unity, funnel test\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }
}
