/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.tree;

import org.burstsys.motif.common.NodeContext;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.schema.model.SchemaClassifierType;

import static com.google.common.base.MoreObjects.toStringHelper;

public final class ParseClassifier extends NodeContext {

    private SchemaClassifierType schemaClassifierType;

    public ParseClassifier() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.SCHEMA_PARSE_CLASSIFIER);

    }

    public ParseClassifier(NodeGlobal global, NodeLocation location, SchemaClassifierType schemaClassifierType) {
        super(global, location, NodeType.SCHEMA_PARSE_CLASSIFIER);
        this.schemaClassifierType = schemaClassifierType;
    }


    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(schemaClassifierType.asString());
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("classifierType", schemaClassifierType)
                .toString();
    }

    public SchemaClassifierType getClassifierType() {
        return schemaClassifierType;
    }
}
