/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test;

import org.apache.logging.log4j.Logger;
import org.burstsys.motif.Motif;
import org.burstsys.motif.motif.tree.eql.common.Statements;
import org.burstsys.motif.motif.tree.eql.queries.Query;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.view.View;
import org.burstsys.vitals.logging.VitalsLog;
import org.junit.Before;

import java.lang.invoke.MethodHandles;

public abstract class MotifAbstractParserSpec {

    static {
        VitalsLog.configureLogging("motif", true);
    }

    // do it with MethodHandles so we get the concrete class
    protected static final Logger log = VitalsLog.getJavaLogger(MethodHandles.lookup().lookupClass());

    protected Motif motif = Motif.build();

    protected String schemaName = "quo";

    @Before
    public void setSchema() {
        //schemaName = "quo";
        // for some reason maven refuses to reflect on these classes. IJ runs the tests just fine
//        motif.parseSchema(new MotifSchemaProviders.QuoSchemaProvider().getSchema());
//        motif.parseSchema(new MotifSchemaProviders.UnitySchemaProvider().getSchema());
    }

    protected String runExpressionJsonTest(String source) {
        log.info("schema '{}' is registered: {}", schemaName, motif.isSchemaRegistered(schemaName));
        Expression result = motif.parseExpression(schemaName, source);
        return result.exportAsJson();
    }

    protected Expression runExpressionTest(String source) {
        log.info("schema '{}' is registered: {}", schemaName, motif.isSchemaRegistered(schemaName));
        Expression result = motif.parseExpression(schemaName, source);
        String explanation = motif.explainExpression(schemaName, source);
        log.info("\n-------------\n" + source + "\n-------------\n" + explanation + "----------\n");
        return result;
    }

    protected View runViewTest(String source) {
        log.info("schema '{}' is registered: {}", schemaName, motif.isSchemaRegistered(schemaName));
        View result = motif.parseView(schemaName, source);
        String explanation = motif.explainView(schemaName, source);
        log.info("\n-------------\n" + source + "\n-------------\n" + explanation + "----------\n");
        return result;
    }

    protected String runViewJsonTest(String source) {
        log.info("schema '{}' is registered: {}", schemaName, motif.isSchemaRegistered(schemaName));
        View result = motif.parseView(schemaName, source);
        return result.exportAsJson();
    }

    protected Query runMotifTest(String source) {
        Query result = motif.parseMotifQuery(source);
        String explanation = motif.explainMotif(source);
        log.info("\n-------------\n" + source + "\n-------------\n" + explanation + "----------\n");
        return result;
    }

    protected Statements runMotifStatementTest(String source) {
        return motif.parseMotifStatements(source);
    }
}
