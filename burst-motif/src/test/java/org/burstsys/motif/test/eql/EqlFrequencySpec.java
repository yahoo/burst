/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.eql;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.eql.queries.Query;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Test;

public class EqlFrequencySpec extends MotifAbstractParserSpec {

    @Test
    public void testSimpleStringFrequency() {
        String source = "" +
                "   select count(user) as users, \n" +
                "           frequency(user.sessions, 'time') as 'frequency',\n" +
                "           day(user.sessions.startTime) as 'time' \n" +
                "    from schema unity\n" +
                "    where day(NOW) - day(user.sessions.startTime) < 30" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test
    public void testSimpleExpressionFrequency() {
        String source = "" +
                "   select count(user) as users, \n" +
                "          frequency(user.sessions, day(user.sessions.startTime)) as 'frequency'\n" +
                "    from schema unity\n" +
                "    where day(NOW) - day(user.sessions.startTime) < 30" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test
    public void testSplitExpressionFrequency() {
        String source = "" +
                "   select count(user) as users, \n" +
                "          split(frequency(user.sessions, day(user.sessions.startTime)), 2, 4, 6) as 'frequency'\n" +
                "    from schema unity\n" +
                "    where day(NOW) - day(user.sessions.startTime) < 30" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test (expected = ParseException.class)
    public void testNotAStructureFrequency() {
        String source = "" +
                "   select count(user) as users, \n" +
                "           frequency(user.sessions.startTime, 'time') as 'frequency',\n" +
                "           day(user.sessions.startTime) as 'time' \n" +
                "    from schema unity\n" +
                "    where day(NOW) - day(user.sessions.startTime) < 30" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test (expected = ParseException.class)
    public void testNotATargetFrequency() {
        String source = "" +
                "   select count(user) as users, \n" +
                "           frequency(user.sessions, wrong) as 'frequency',\n" +
                "           day(user.sessions.startTime) as 'time' \n" +
                "    from schema unity\n" +
                "    where day(NOW) - day(user.sessions.startTime) < 30" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }

    @Test (expected = ParseException.class)
    public void testNotADimenstion() {
        String source = "" +
                "   select count(user) as users, \n" +
                "           day(user.sessions.startTime) as 'time' \n" +
                "    from schema unity\n" +
                "    where day(NOW) - day(user.sessions.startTime) < 30 && " +
                "          frequency(user.sessions, day(user.sessions.startTime)) > 2" +
                "";
        Query result = runMotifTest(source);
        assert result != null;
    }
}
