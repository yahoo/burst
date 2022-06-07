/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.eql;

import org.burstsys.motif.motif.tree.eql.queries.Query;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.logical.ValueComparisonBooleanExpression;
import org.burstsys.motif.motif.tree.values.BinaryValueExpression;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class EqlTransformSpec extends MotifAbstractParserSpec {

    @Test
    public void testSimpleWalk() {
        String source = "" +
                "select count(user.sessions) as sessions, \n" +
                "user.sessions.appVersion as appVersion \n" +
                "where user.application.firstUse.sessionTime >= (NOW - DAYS(30)) \n" +
                "from schema Unity\n" +
                "limit 5 \n" +
                "";
        Query result = runMotifTest(source);
        final AtomicInteger nodeCount = new AtomicInteger();
        result.getSelects().get(0).getWhere().walkTree(node ->
                nodeCount.incrementAndGet());
        assert(nodeCount.get() == 5);
    }

    @Test
    public void testSimpleTransform() {
        String selectSource = "" +
                "select count(user.sessions) as sessions, \n" +
                "user.sessions.appVersion as appVersion \n" +
                "where user.application.firstUse.sessionTime >= (NOW - DAYS(30)) \n" +
                "from schema Unity\n" +
                "limit 5 \n" +
                "";
        Query baseResult = runMotifTest(selectSource);
        final AtomicInteger nodeCount = new AtomicInteger();
        baseResult.getSelects().get(0).getWhere().walkTree(node ->
                nodeCount.incrementAndGet());
        assert(nodeCount.get() == 5);

        // change the right side of the comparison expression
        Expression newWhereResult = runExpressionTest("NOW");
        baseResult.getSelects().get(0).getWhere().setChild(1, newWhereResult);
        String explanation = baseResult.generateMotif(0);
        log.info("New query: \n" + explanation);
        nodeCount.set(0);
        baseResult.getSelects().get(0).getWhere().walkTree(node ->
                nodeCount.incrementAndGet());
        assert(nodeCount.get() == 3);
    }

    @Test
    public void testTransformInWalk() {
        String selectSource = "" +
                "select count(user.sessions) as sessions, \n" +
                "user.sessions.appVersion as appVersion \n" +
                "where user.application.firstUse.sessionTime >= (NOW - DAYS(30)) \n" +
                "from schema Unity\n" +
                "limit 5 \n" +
                "";

        Query baseResult = runMotifTest(selectSource);
        // this will make a nonsensical expression
        baseResult.getSelects().get(0).getWhere().walkTree(node -> {
            if (node instanceof BinaryValueExpression) {
                node.setChild(0, node.setChild(1, node.getChild(0)));
            } else if (node instanceof ValueComparisonBooleanExpression) {
                node.setChild(0, node.setChild(1, node.getChild(0)));
            }
        });
        String explanation = baseResult.generateMotif(0);
        log.info("New query: \n" + explanation);
        final AtomicInteger nodeCount = new AtomicInteger();
        baseResult.getSelects().get(0).getWhere().walkTree(node ->
                nodeCount.incrementAndGet());
        assert(nodeCount.get() == 5);

    }
}
