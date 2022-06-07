/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;
import org.burstsys.motif.motif.tree.view.View;
import org.junit.Test;

public class MotifViewSpec extends MotifAbstractParserSpec {

    @Test(expected = ParseException.class)
    public void filterError1() {
        String expressionSource = "view filterError1 {include user.flurryId where true;}";
        View result = runViewTest(expressionSource);
        assert result != null;
    }

    @Test(expected = ParseException.class)
    public void filterError2() {
        String expressionSource = "view filterError2 {include user.sessions.startTime;}";
        View result = runViewTest(expressionSource);
        assert result != null;
    }

    @Test
    public void view3() {
        String expressionSource =
                "VIEW newsroom {\n" +
                        "   INCLUDE user WHERE user.project.installTime BETWEEN NOW AND NOW - DAYS(90)\n" +
                        "   INCLUDE user.sessions.events where user.sessions.events.eventId in (18511982, 12804801, 13047489, 13021316,\n" +
                        "                                                                13095593, 18170570, 12916431)\n" +
                        "}";

        View result = runViewTest(expressionSource);
        assert result != null;
    }

    @Test
    public void view4() {
        String expressionSource =
                "VIEW mobilityWare {\n" +
                        "   INCLUDE user WHERE user.sessions.events.eventId in (17964296, 17964089)\n" +
                        "}";

        View result = runViewTest(expressionSource);
        assert result != null;
    }


    @Test
    public void normalizePredicates1() {
        String expressionSource1 =
                "VIEW view1 {\n" +
                        "   INCLUDE user WHERE user.sessions.events.eventId in (17964296, 17964089)\n" +
                        "   INCLUDE user.sessions.events WHERE user.sessions.events.eventId not in (17964297, 17964090)\n" +
                        "   INCLUDE user.sessions WHERE user.sessions.startTime > 1464764400000\n" +
                        "}";

        View v1 = motif.parseView(schemaName, expressionSource1);


        BooleanExpression be = v1.rootFilterPredicate();

        assert be != null;
        assert be.getDtype() == DataType.BOOLEAN;

        String ne = be.generateMotif(0);
        assert ne != null;
    }

    @Test
    public void normalizeStrings() {
        String expressionSource1 =
                "VIEW view1 {\n" +
                        "   INCLUDE user WHERE user.sessions.events.parameters['Message'] in (\n" +
                        "      \"Double quotes with internal \"\"double quotes\"\"\",\n" +
                        "      \"Double quotes with internal 'single quotes'\",\n" +
                        "      'Single quotes with internal \"double quotes\"',\n" +
                        "      'Single quotes with internal ''single quotes'''\n" +
                        "   )\n" +
                        "}";

        View v1 = motif.parseView(schemaName, expressionSource1);


        String motif = v1.generateMotif(0);
        String expected = "VIEW \"view1\" {\n" +
                "INCLUDE user WHERE user.sessions.events.parameters['Message'] IN (" +
                "'Double quotes with internal \"double quotes\"', " +
                "'Double quotes with internal ''single quotes''', " +
                "'Single quotes with internal \"double quotes\"', " +
                "'Single quotes with internal ''single quotes''' " +
                ");\n" +
                "}";
        log.info("\n{}\n------------\n{}", motif, expected);
        assert motif.equals(expected);

    }
}
