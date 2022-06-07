/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.expression;

import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MotifPrecedenceSpec extends MotifAbstractParserSpec {

    @Test
    public void constantReduction1() {
        String expressionSource1 = "300 > 123423423434 - 7 * 86400 * 1000";
        Expression result1 = runExpressionTest(expressionSource1);
        String result1Explanation = result1.explain();

        String expressionSource2 = "300 > (123423423434 - ((7 * 86400) * 1000))";
        Expression result2 = runExpressionTest(expressionSource2);
        String result2Explanation = result2.explain();

        assertEquals("result1 and result2 should be equal", result1Explanation, result2Explanation);
    }


}
