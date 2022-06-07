/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.model.context;

import org.burstsys.motif.common.*;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.schema.model.SchemaReference;
import org.burstsys.motif.schema.model.SchemaStructure;
import org.burstsys.motif.schema.tree.ParseRelation;
import org.burstsys.motif.schema.tree.ParseReference;

import static com.google.common.base.MoreObjects.toStringHelper;

public abstract class SchemaReferenceContext extends SchemaRelationContext implements SchemaReference {

    SchemaStructureContext referenceType;

    private String referencedTypeNodeName;

    SchemaReferenceContext(NodeGlobal global, NodeLocation location, NodeType ntype, RelationType rtype,
                           SchemaStructureContext structureModel, ParseRelation field) {
        super(global, location, ntype, rtype, structureModel, field);
        this.referencedTypeNodeName = ((ParseReference) field).getReferencedTypeName();
    }

    public String getJsonReferenceType() {
        return getReferenceType().getStructureName();
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("structureName", getStructureName())
                .add("fieldNumber", getFieldNumber())
                .add("fieldName", getFieldName())
                .add("referencedTypeNodeName", referencedTypeNodeName)
                .omitNullValues()
                .toString();
    }

    @Override
    public SchemaStructure getReferenceType() {
        return referenceType;
    }

    String getReferenceTypeName() {
        return referencedTypeNodeName;
    }
}
