/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.eql;

import org.burstsys.motif.motif.tree.eql.queries.Query;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Before;
import org.junit.Test;

public class EqlWhereSpec extends MotifAbstractParserSpec {
    @Before
    public void init() {
        schemaName = "unity";
    }

    @Test
    public void testSimpleWhere() {
        String source = "" +
                "select unique(user.sessions) as user \n" +
                "where user.sessions.startTime == 1 \n" +
                "from schema Unity\n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test
    public void testOffAxisWhere() {
        String source = "" +
                "select unique(user.sessions) as user \n" +
                "where user.sessions.startTime == 1 AND user.interests == 2 \n" +
                "from schema Unity\n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

}
