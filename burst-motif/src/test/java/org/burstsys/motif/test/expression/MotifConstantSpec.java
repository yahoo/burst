/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.expression;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.constant.BooleanConstant;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.motif.tree.constant.LongConstant;
import org.burstsys.motif.motif.tree.constant.StringConstant;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Assert;
import org.junit.Test;

public class MotifConstantSpec extends MotifAbstractParserSpec {

    @Test
    public void constantReduction1() {
        String expressionSource = "(5999 % 34) == 0";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert !((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction2() {
        String expressionSource = "(5999 % 34) < 5450000";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert ((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction3() {
        String expressionSource = "12345 + 1";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof LongConstant;
            assert ((LongConstant) c).getLongValue() == 12346;
        } else Assert.fail();
    }

    @Test
    public void constantReduction4() {
        String expressionSource = "12345 NOT IN (12345, 456, 987)";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert !((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction5() {
        String expressionSource = "12345 IN (12345, 456, 987)";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert ((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction6() {
        String expressionSource = "12345 between 0 and 1";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert !((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction7() {
        String expressionSource = "12345 not between 0 and 1";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert ((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction8() {
        String expressionSource = "12345 between 0 and 1000000";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert ((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction9() {
        String expressionSource = "12345 not between 0 and 1000000";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert !((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction10() {
        String expressionSource = "NULL == null";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert ((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction11() {
        String expressionSource = "NULL != NULL";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert !((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test(expected = ParseException.class)
    public void constantReduction12() {
        String expressionSource = "NULL <= 9087456";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert !((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction13a() {
        String expressionSource = "user.sessions IS NULL";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Assert.fail();
        }
    }

    @Test
    public void constantReduction13b() {
        String expressionSource = "user.sessions == NULL";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Assert.fail();
        }
    }

    @Test
    public void constantReduction14() {
        String expressionSource = "cast('1.0' as double) == 1.0";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert ((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction15() {
        String expressionSource = "NULL is null";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert ((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction16() {
        String expressionSource = "NULL is not null";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert !((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction17() {
        String expressionSource = "2345 is not null";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert !((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction18() {
        String expressionSource = "(1<=2 AND (true || false))";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert ((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction19() {
        String expressionSource = "(1<=2 AND (true && ('hello' == 'hello')))";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert ((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction20() {
        String expressionSource = "(1<=2 AND (true && ('hello' == 'goodbye')))";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert !((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction21() {
        String expressionSource = "cast(1234 as string)";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof StringConstant;
            assert ((StringConstant) c).getStringValue().equals("1234");
        } else Assert.fail();
    }

    @Test
    public void constantReduction22() {
        String expressionSource = "cast(2.3 as string)";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof StringConstant;
            String stringValue = ((StringConstant) c).getStringValue();
            assert stringValue.equals("2.3");
        } else Assert.fail();
    }

    @Test
    public void constantReduction23() {
        String expressionSource = "cast('2.3' as double) > cast('2' as integer) ";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert ((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction24() {
        String expressionSource = "5 in ((0 + 1), 4*5, 6-1)  ";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert ((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

    @Test
    public void constantReduction25() {
        String expressionSource = "5 in ((0 + 1), 4*5, 6 / 2)  ";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof BooleanConstant;
            assert !((BooleanConstant) c).getBooleanValue();
        } else Assert.fail();
    }

/*
    @Test
    public void constantReduction26() {
        String expressionSource = " ((5 + 3) / 2) * 1.5";
        Expression result = runExpressionTest(expressionSource);
        if (result.canReduceToConstant()) {
            Constant c = result.reduceToConstant();
            assert c instanceof LongConstant;
            assert ((LongConstant) c).getLongValue();
        } else Assert.fail();
    }
*/


}
