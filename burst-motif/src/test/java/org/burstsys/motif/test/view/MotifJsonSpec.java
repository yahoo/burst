/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.burstsys.motif.common.NodeContext;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.view.View;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Test;

import java.io.IOException;

public class MotifJsonSpec extends MotifAbstractParserSpec {
    protected String schemaName = "unity";

    @Test
    public void jsonTest1() {
        String source = "VIEW v1 {INCLUDE user WHERE true ; INCLUDE user.sessions WHERE true}";
        testBidirectionViewTranslation(source);

    }

    @Test
    public void jsonTest2() {
        String expressionSource = "" +
                "view jsonTest2 {include user where user.sessions.events.eventId NOT IN (345, 346, 345+1, 345+2)" +
                " OR cast('foo' as string) == 'foo' " +
                "AND (NOT true && 23436 == 5*4)" +
                "OR user.sessions.events.parameters['foo'] IS NOT NULL" +
                "}";
        testBidirectionViewTranslation(expressionSource);

    }

    private void testBidirectionViewTranslation(String expressionSource) {
        System.out.println("INPUT: " + expressionSource);
        String json = runViewJsonTest(expressionSource);

        ObjectMapper mapper = new ObjectMapper();
        try {
            NodeContext deserializedResult = mapper.readValue(json, NodeContext.class);

            View deserializedExpression = (View) deserializedResult;

            String generatedMotif = deserializedExpression.generateMotif(0);

            System.out.println("OUTPUT: " + generatedMotif);

            runViewJsonTest(generatedMotif);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
