/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.eql;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.eql.common.Statements;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Before;
import org.junit.Test;

public class EqlSegmentSpec extends MotifAbstractParserSpec {
    @Before
    public void init() {
        schemaName = "unity";
    }

    @Test
    public void testSimpleSegment() {
        String source = "" +
                "segment test {\n" +
                "    segment 1 when user.sessions.events.id in (1, 2, 3)\n" +
                "    segment 2 when user.sessions.events.id not in (1, 2, 3)\n" +
                "} from schema Unity\n" +
                "select count(user) as number, test.members.id as 'segments' \n" +
                "from schema Unity, segment test\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testInvalidStepIdSegment() {
        String source = "" +
                "segment test {\n" +
                "    segment 1 when user.sessions.events.id in (1, 2, 3)\n" +
                "    segment xx when user.sessions.events.id not in (1, 2, 3)\n" +
                "} from schema Unity\n" +
                "select count(user) as number, test.members.id as 'segments' \n" +
                "from schema Unity, segment test\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test
    public void testParameterSegment() {
        String source = "" +
                "segment test (x: long) {\n" +
                "    segment 1 when user.sessions.events.id in (1, 2, $x)\n" +
                "    segment 2 when user.sessions.events.id not in (1, 2, $x)\n" +
                "} from schema Unity\n" +
                "select(x: long) count(user) as number, test.members.id as 'segments' \n" +
                "from schema Unity, segment test($x)\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testInvalidParameterNotParameterSegment() {
        String source = "" +
                "segment test(x: long) {\n" +
                "    segment 1 when user.sessions.events.id in (1, 2, $x)\n" +
                "    segment 2 when user.sessions.events.id not in (1, 2, $x)\n" +
                "} from schema Unity\n" +
                "select count(user) as number, test.members.id as 'segments' \n" +
                "from schema Unity, segment test('hello')\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testInvalidParameterUseTypeFunnel() {
        String source = "" +
                "segment test(x: string) {\n" +
                "    segment 1 when user.sessions.events.id in (1, 2, $x)\n" +
                "    segment 2 when user.sessions.events.id not in (1, 2, $x)\n" +
                "} from schema Unity\n" +
                "select(x: string) count(user) as number, test.members.id as 'segments' \n" +
                "from schema Unity, segment test($x)\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void testInvalidParameterBadTypeFunnel() {
        String source = "" +
                "segment test(x: long) {\n" +
                "    segment 1 when user.sessions.events.id in (1, 2, $x)\n" +
                "    segment 2 when user.sessions.events.id not in (1, 2, $x)\n" +
                "} from schema Unity\n" +
                "select(x: string) count(user) as number, test.members.id as 'segments' \n" +
                "from schema Unity, segment test($x)\n" +
                "";
        Statements result = runMotifStatementTest(source);
        assert result != null;
    }
}
