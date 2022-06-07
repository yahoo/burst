/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.expression;

import org.burstsys.motif.Motif;
import org.burstsys.motif.flurry.providers.MotifSchemaProviders;
import org.burstsys.motif.motif.tree.view.View;
import org.burstsys.motif.schema.model.MotifSchema;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Test;

public class MotifBasicSpec extends MotifAbstractParserSpec {

    @Test
    public void test1() {
        Motif motif = Motif.build();
        MotifSchema schema;

        String testSchemaSource = new MotifSchemaProviders.QuoSchemaProvider().getSchema();
        motif.parseSchema(testSchemaSource);
        String schemaExplanation = motif.explainSchema(testSchemaSource);
        log.info("\n---------------\n" + schemaExplanation + "\n---------------\n");

        String testFilterSource = "view hello {include user where user.sessions.events.eventId!=987356}";
        log.info(testFilterSource);
        View view = motif.parseView("quo", testFilterSource);
        String filterExplanation = motif.explainView("quo", testFilterSource);
        log.info("\n---------------\n" + filterExplanation + "\n---------------\n");
    }


}
