/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.common.context;

import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;
import org.burstsys.motif.schema.model.MotifSchema;
import org.burstsys.motif.symbols.Definition;
import org.burstsys.motif.symbols.PathSymbols;

import static java.lang.String.format;

public class SchemaSourceContext extends BaseSourceContext {

    public SchemaSourceContext() {
        super(NodeType.SCHEMA_SOURCE);
    }

    public SchemaSourceContext(NodeGlobal global, NodeLocation location, String name, String alias) {
        super(global, location, NodeType.SCHEMA_SOURCE, name, alias);
    }

    public String generateMotif(int level) {
        return "schema " + super.generateMotif(level);
    }

    @Override
    public Definition register(PathSymbols pathSymbols) {
        boolean hasName = this.name != null;
        Definition def = super.register(pathSymbols);
        if (!(def instanceof MotifSchema))
            throw new ParseException(getLocation(), format("definition for %s is not a schema", getDeclaredName()));
        if (!hasName) {
            this.name = ((MotifSchema) def).getRootFieldName();
        }
        return def;
    }

}
