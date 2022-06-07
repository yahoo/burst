/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.model.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.schema.model.SchemaStructure;
import org.burstsys.motif.schema.model.SchemaValue;
import org.burstsys.motif.schema.tree.ParseRelation;
import org.burstsys.motif.schema.tree.ParseValue;

import static com.google.common.base.MoreObjects.toStringHelper;

public abstract class SchemaValueContext extends SchemaRelationContext implements SchemaValue {

    @JsonProperty
    private DataType vtype;

    SchemaValueContext(NodeGlobal global, NodeLocation location, NodeType ntype, RelationType rtype, SchemaStructure structureModel, ParseRelation field) {
        super(global, location, ntype, rtype, structureModel, field);
        this.vtype = ((ParseValue) field).valueDataType.dataType;
    }

    public String getJsonValueDataType() {
        return getValueDataType().name();
    }

    @Override
    public final DataType getValueDataType() {
        return vtype;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("structureName", getStructureName())
                .add("fieldNumber", getFieldNumber())
                .add("fieldName", getFieldName())
                .add("vtype", vtype)
                .add("classifiers", getClassifiers())
                .omitNullValues()
                .toString();
    }




}
