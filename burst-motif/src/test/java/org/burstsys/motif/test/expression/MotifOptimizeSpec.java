/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.expression;

import org.burstsys.motif.motif.tree.constant.BooleanConstant;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MotifOptimizeSpec extends MotifAbstractParserSpec {

    @Test
    public void optimize1() {
        String expressionSource = "(4 < 10) && 'foo' == 'foo'";
        Expression result = runExpressionTest(expressionSource);
        assertThat("can reduce to constant", true, is(result.canReduceToConstant()));
        assertThat("result is true", true, is(((BooleanConstant) result).getBooleanValue()));
    }

    @Test
    public void optimize2() {
        String expressionSource = "user.sessions.events.eventId == (234565 * 349876) + 2345897 / 2.0";
        runExpressionTest(expressionSource);
    }

}
