/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.model.context;

import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.schema.model.SchemaReferenceScalar;
import org.burstsys.motif.schema.tree.ParseRelation;

public final class SchemaReferenceScalarContext extends SchemaReferenceContext implements SchemaReferenceScalar {

    SchemaReferenceScalarContext(NodeGlobal global, NodeLocation location, SchemaStructureContext structureModel, ParseRelation field) {
        super(global, location, NodeType.SCHEMA_MODEL_REFERENCE_SCALAR, RelationType.REFERENCE_SCALAR, structureModel, field);
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(getFieldNumber());
        builder.append(':');
        builder.append(getFieldName());
        builder.append(':');
        builder.append(getReferenceTypeName());
        builder.append(this.explainClassifiers());
        return endExplain(builder);
    }

}
