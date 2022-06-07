/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.model.context;

import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.schema.model.SchemaReference;
import org.burstsys.motif.schema.model.SchemaStructure;

public final class SchemaRootVectorContext extends SchemaRelationContext implements SchemaReference {
    private SchemaStructure structure;

    SchemaRootVectorContext(NodeGlobal global, NodeLocation location, SchemaStructureContext structureModel, String name) {
        super(global, location, NodeType.SCHEMA_MODEL_REFERENCE_VECTOR, RelationType.REFERENCE_VECTOR, structureModel, name, -1);
        this.structure = structureModel;
    }


    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append("root:");
        builder.append(getFieldName());
        builder.append(':');
        builder.append("vector[");
        builder.append(this.getRelationType());
        builder.append(']');
        return endExplain(builder);
    }

    @Override
    public SchemaStructure getReferenceType() {
        return structure;
    }
}
