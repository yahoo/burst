/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.eql;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.eql.queries.Query;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Test;

public class EqlBasicSpec extends MotifAbstractParserSpec {


    @Test
    public void testMoreAggregatesSingle() {
        String source = "" +
                "select unique(user.sessions) as user, \n" +
                "top[20](user.sessions.parameters.key) as key \n" +
                "where user.application.firstUse.sessionTime >= (NOW - DAYS(30)) \n" +
                "from schema Unity\n" +
                "limit 5 \n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test
    public void testSimpleSingle() {
        String source = "" +
                "select count(user.sessions) as sessions, \n" +
                "user.sessions.appVersion as appVersion \n" +
                "where user.application.firstUse.sessionTime >= (NOW - DAYS(30)) \n" +
                "from schema Unity\n" +
                "limit 5 \n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test
    public void testQuoSchemaSingle() {
        String source = "" +
                "select count(user.sessions) as sessions \n" +
                "where user.project.installTime >= (NOW - DAYS(30)) \n" +
                "from schema Quo\n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test
    public void testAltSimpleSingle() {
        String source = "" +
                "select count(user.sessions) as sessions, \n" +
                "user.sessions.appVersion as appVersion \n" +
                "from schema Unity\n" +
                "where user.application.firstUse.sessionTime >= (NOW - DAYS(30)) \n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testInvalidFieldAccess() {
        String source = "" +
                "select user.foobar['hello']" +
                "from schema Unity\n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

}
