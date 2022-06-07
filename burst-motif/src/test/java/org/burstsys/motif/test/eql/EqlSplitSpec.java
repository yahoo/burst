/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.eql;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.eql.queries.Query;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Test;

public class EqlSplitSpec extends MotifAbstractParserSpec {

    @Test
    public void testSimpleIntegerSplit() {
        String source = "" +
                "   select split(user.sessions.events.id, 0, 10, 20, 30) as ids \n" +
                "    from schema unity\n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test (expected = ParseException.class)
    public void testNotValueScalar() {
        String source = "" +
                "   select split(user.sessions.events) as ids \n" +
                "    from schema unity\n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test (expected = ParseException.class)
    public void testNotEnoughArgs() {
        String source = "" +
                "   select split(user.sessions.event.id) as ids \n" +
                "    from schema unity\n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test (expected = ParseException.class)
    public void testParameterMismatch1() {
        String source = "" +
                "   select split(user.sessions.id, \"hello\") as ids \n" +
                "    from schema unity\n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test
    public void testParameterCoerce() {
        String source = "" +
                "   select split(user.sessions.id, 1, 1.0) as ids \n" +
                "    from schema unity\n" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }
}
