/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.expression;

import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.logical.BinaryBooleanExpression;
import org.burstsys.motif.motif.tree.logical.BinaryBooleanOperatorType;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MotifGenerateSpec extends MotifAbstractParserSpec {

    @Before
    public void init() {
        schemaName = "unity";
    }

    @Test
    public void generate1() {
        String expressionSource = "user.sessions.appVersion.id in (9646535) && (user.application.mostUse.regionId in (4,5) || user.application.mostUse.countryId in (435462,435498))";
        Expression originalResult = runExpressionTest(expressionSource);
        String generatedSource = originalResult.generateMotif(0);
        Expression generatedResult = runExpressionTest(generatedSource);
        assertThat("generatedResult agree at root", ((BinaryBooleanExpression) generatedResult).getOp() , is(BinaryBooleanOperatorType.AND));
        assertThat("originalResult agree at root", ((BinaryBooleanExpression) originalResult).getOp() , is(BinaryBooleanOperatorType.AND));
    }

    @Test
    public void generateNormalizedStrings() {
        Expression e1 = runExpressionTest("\"Double quotes with internal \"\"double quotes\"\"\"");
        assertThat("double quotes from double quotes are not escaped",
                e1.generateMotif(0),
                equalTo("'Double quotes with internal \"double quotes\"'"));

        Expression e2 = runExpressionTest("\"Double quotes with internal 'single quotes'\"");
        assertThat("single quotes from double quotes are escaped",
                e2.generateMotif(0),
                equalTo("'Double quotes with internal ''single quotes'''"));

        Expression e3 = runExpressionTest("'Single quotes with internal \"double quotes\"'");
        assertThat("double quotes from single quotes are not escaped",
                e3.generateMotif(0),
                equalTo("'Single quotes with internal \"double quotes\"'"));

        Expression e4 = runExpressionTest("'Single quotes with internal ''single quotes'''" );
        assertThat("single quotes from single quotes are escaped",
                e4.generateMotif(0),
                equalTo("'Single quotes with internal ''single quotes'''"));


    }
}
