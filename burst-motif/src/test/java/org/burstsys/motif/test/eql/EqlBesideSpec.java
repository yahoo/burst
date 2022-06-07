/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.eql;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.eql.common.Statements;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Test;

public class EqlBesideSpec extends MotifAbstractParserSpec {
    @Test
    public void testSimpleParallel() {
        String source = "" +
                "select count(user.sessions) as sessions, \n" +
                "       user.sessions.appVersion as appVersion \n" +
                "beside \n" +
                "select count(user) as users, user.deviceModelId as deviceModel \n" +
                "from schema Unity\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testMalformedParallel() {
        String source = "" +
                "select count(user.sessions) as sessions, \n" +
                "       user.sessions.appVersion as appVersion \n" +
                "from schema Unity\n" +
                "beside \n" +
                "select count(user) as users, user.deviceModelId as deviceModel \n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }
    @Test
    public void testGlobalWhere() {
        String source = "" +
                "select count(user.sessions) as sessions, \n" +
                "       user.sessions.appVersion as appVersion \n" +
                "beside \n" +
                "select count(user) as users, user.deviceModelId as deviceModel \n" +
                "from schema Unity\n" +
                "where user.application.firstUse.sessionTime >= (NOW - DAYS(30)) \n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test
    public void testFullMetaData() {
        String source = "" +
                "select count(user.sessions) as sessions, " +
                "       user.sessions.appVersion as appVersion\n" +
                "beside select count(user) as users, " +
                "              user.deviceModelId as deviceModel\n" +
                "beside select count(user.sessions.events) as eventFrequency, " +
                "              user.sessions.events.id as eventId\n" +
                "beside select count(user.sessions.events) as eventParameterFrequency, " +
                "              user.sessions.events.id as eventId," +
                "              user.sessions.events.parameters.key as eventParameterKey\n" +
                "beside select count(user.application) as projects," +
                "              user.application.firstUse.languageId as languageId\n" +
                "beside select count(user.sessions) as sessions," +
                "              user.sessions.mappedOriginId as mappedOrigin\n" +
                "beside select count(user.sessions) as sessions," +
                "              user.sessions.originMethodTypeId as originMethodType\n" +
                "beside select count(user.sessions) as sessions," +
                "              user.sessions.originSourceTypeId as originSourceType\n" +
                "beside select count(user.sessions) as sessions," +
                "              user.sessions.osVersionId as osVersionId\n" +
                "beside select count(user.sessions) as sessions," +
                "              user.sessions.providedOrigin as providedOrigin\n" +
                "from schema Unity\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }
}
