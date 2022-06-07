/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.expression;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.constant.*;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.fail;

public class MotifCastSpec extends MotifAbstractParserSpec {

    @Test
    public void cast1() {
        String expressionSource = "cast( 0x1F as byte)";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof ByteConstant;
        } else fail();
    }

    @Test
    public void cast2() {
        String expressionSource = "cast( 123987 as short)";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof ShortConstant;
        } else fail();
    }

    @Test(expected = ParseException.class)
    public void cast3() {
        String expressionSource = "cast( '1200987' as short)";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof ShortConstant;
        } else fail();
    }


    @Test
    public void cast4() {
        String expressionSource = "cast( '10002347' as integer)";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof IntegerConstant;
        } else fail();
    }

    @Test
    public void cast5() {
        String expressionSource = "cast( 'true' as boolean)";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
        } else fail();
    }

    @Test
    public void cast7() {
        String expressionSource = "(COUNT(user.sessions.events) WHERE (user.sessions.events.eventId == 19310758 AND CAST(user.sessions.events.parameters['fl.CampaignId'] AS long) == 1047)) >= 1";
        Expression result = runExpressionTest(expressionSource);
        assert !result.canReduceToConstant();
    }
}
