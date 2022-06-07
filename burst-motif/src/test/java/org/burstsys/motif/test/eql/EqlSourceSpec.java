/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.eql;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.eql.common.Statements;
import org.burstsys.motif.motif.tree.eql.queries.Query;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class EqlSourceSpec extends MotifAbstractParserSpec {
    @Before
    public void init() {
        schemaName = "unity";
    }

    @Test
    public void testSingleSourceWhere() {
        String source = "" +
                "select unique(user.sessions) as user \n" +
                "from schema Unity\n" +
                "where user.sessions.startTime == 1 \n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test
    public void testSingleAliasedSourceWhere() {
        String source = "" +
                "select unique(u.sessions) as user \n" +
                "from schema Unity as u\n" +
                "where u.sessions.startTime == 1 \n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test
    public void testDuplicateSchema() {
        String source = "" +
                "select unique(user.sessions) as user \n" +
                "from schema Unity, schema Unity\n" +
                "";
        ParseException e = assertThrows(ParseException.class, () -> {
            Query result = runMotifTest(source);
            assert result != null;
        });
        assertTrue(e.getMessage().contains("one schema source"));
    }

    @Test
    public void testMultipleSchema() {
        String source = "" +
                "select unique(user.sessions) as user \n" +
                "from schema Unity, schema Quo\n" +
                "";
        ParseException e = assertThrows(ParseException.class, () -> {
            Query result = runMotifTest(source);
            assert result != null;
        });
        assertTrue(e.getMessage().contains("one schema source"));
    }

    @Test
    public void testInvalidSchema() {
        String source = "" +
                "select unique(user.sessions) as user \n" +
                "from schema Foo as user\n" +
                "";
        ParseException e = assertThrows(ParseException.class, () -> {
            Query result = runMotifTest(source);
            assert result != null;
        });
        assertTrue(e.getMessage().contains("Foo not found"));
    }

    @Test
    public void testDuplicateAlias() {
        String source = "" +
                "funnel test conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id in (1, 2, 3)\n" +
                "    ( 1 : 2 )" +
                "} from schema Unity\n" +
                "select count(user) as number, count(test.paths) as paths\n" +
                "from schema Unity as u, funnel test as u\n" +
                "";

        ParseException e = assertThrows(ParseException.class, () -> {
            Statements result = runMotifStatementTest(source);
            assert result != null;
        });
        assertTrue(e.getMessage().contains("name u is already defined"));
    }

    @Test
    public void testMultipleFromWhere() {
        String source = "" +
                "funnel test conversion {\n" +
                "    step 1 when start of user.sessions timing on user.sessions.startTime\n" +
                "    step 2 when user.sessions.events.id in (1, 2, 3)\n" +
                "    ( 1 : 2 )" +
                "} from schema Unity\n" +
                "select count(user) as number, count(t.paths) as paths\n" +
                "from schema Unity as u where user.id == 2 from funnel test as t where user.sessions.startTime > now - days(2)\n" +
                "";

        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

}
