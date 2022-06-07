/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.model.context;

import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.schema.model.SchemaStructure;
import org.burstsys.motif.schema.model.SchemaValueScalar;
import org.burstsys.motif.schema.tree.ParseRelation;

public final class SchemaValueScalarContext extends SchemaValueContext implements SchemaValueScalar {

    SchemaValueScalarContext(NodeGlobal global, NodeLocation location, SchemaStructure structureModel, ParseRelation field) {
        super(global, location, NodeType.SCHEMA_MODEL_VALUE_SCALAR, RelationType.VALUE_SCALAR, structureModel, field);
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(this.getFieldNumber());
        builder.append(':');
        builder.append(this.getFieldName());
        builder.append(':');
        builder.append(this.getValueDataType());
        builder.append(this.explainClassifiers());
        return endExplain(builder);
    }
}
