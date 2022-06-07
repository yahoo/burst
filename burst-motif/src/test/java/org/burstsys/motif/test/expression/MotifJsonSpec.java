/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.expression;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.burstsys.motif.common.NodeContext;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class MotifJsonSpec extends MotifAbstractParserSpec {

    @Test
    public void jsonTest1() {
        String json = motif.getSchema(schemaName).exportAsJson();
        log.info(json);
    }

    @Test(expected = ParseException.class)
    public void jsonTest5() {
        String source = "cast('foo' as DOUBLE)";
        testBidirectionExpressionTranslation(source);
    }

    @Test
    public void jsonTest6() {
        String source = "user.sessions.events is not null and 56 > 2";
        testBidirectionExpressionTranslation(source);
    }

    @Test
    public void jsonTest7() {
        String source = "(45 > 6) AND (45 + 4 / 32 * 5 % 4) > 0";
        testBidirectionExpressionTranslation(source);
    }

    @Test
    public void jsonTest8() {
        String source = "(count(user.sessions) where user.sessions.startTime between 10 and 11)";
        testBidirectionExpressionTranslation(source);
    }

    @Test
    public void jsonTest9() {
        String source = "count(user.sessions)";
        testBidirectionExpressionTranslation(source);
    }

    @Test
    public void jsonTest10() {
        String source = "(count(user.sessions) where user.sessions.startTime between 10 and user.sessions.startTime - 10)";
        testBidirectionExpressionTranslation(source);
    }

    @Test
    public void jsonTest11() {
        String source = "(count(user.sessions) where user.sessions.startTime in (0, user.sessions.startTime - 10, user.sessions.startTime - 20, user.sessions.startTime - 30))";
        testBidirectionExpressionTranslation(source);
    }

    @Test
    public void jsonTest12() {
        String source = "(count(user.sessions) where user.sessions.events.parameters['CampaignName'] == 'Bug fixing')";
        testBidirectionExpressionTranslation(source);
    }

    /**
     * Confirms Motif/JSON/Motif conversion is working:
     * <OL>
     *     <LI>parses {@code expressionSource} to Motif tree in superclass method, which returns generated JSON (json1);</LI>
     *     <LI>uses Jackson to convert json1 to a new Motif expression tree (deserializedExpression);</LI>
     *     <LI>gets generated Motif source from the tree (generatedMotif);</LI>
     *     <LI>parses {@code generatedMotif} to Motif tree in superclass method, which returns generated JSON (json2);</LI>
     *     <LI>confirms that json1 and json2 are identical.</LI>
     * </OL>
     * The Motif source and expression trees are not in general the same, because of letter case and implicit/explicit defaults.
     */
    private void testBidirectionExpressionTranslation(String expressionSource) {
        System.out.println("INPUT: " + expressionSource);
        String json1 = runExpressionJsonTest(expressionSource);

        ObjectMapper mapper = new ObjectMapper();
        try {
            NodeContext deserializedResult = mapper.readValue(json1, NodeContext.class);

            Expression deserializedExpression = (Expression) deserializedResult;

            String generatedMotif = deserializedExpression.generateMotif(0);

            System.out.println("OUTPUT: " + generatedMotif);

            String json2 = runExpressionJsonTest(generatedMotif);

            Assert.assertEquals(json1, json2);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void testBidirectionViewTranslation(String expressionSource) {
        System.out.println("INPUT: " + expressionSource);
        String json1 = runViewJsonTest(expressionSource);

        ObjectMapper mapper = new ObjectMapper();
        try {
            NodeContext deserializedResult = mapper.readValue(json1, NodeContext.class);

            Expression deserializedExpression = (Expression) deserializedResult;

            String generatedMotif = deserializedExpression.generateMotif(0);

            System.out.println("OUTPUT: " + generatedMotif);

            String json2 = runViewJsonTest(generatedMotif);

            Assert.assertEquals(json1, json2);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
