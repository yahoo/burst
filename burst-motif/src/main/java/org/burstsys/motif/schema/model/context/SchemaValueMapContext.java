/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.model.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.schema.model.SchemaStructure;
import org.burstsys.motif.schema.model.SchemaValueMap;
import org.burstsys.motif.schema.tree.ParseRelation;
import org.burstsys.motif.schema.tree.ParseValueMap;

import static com.google.common.base.MoreObjects.toStringHelper;

public final class SchemaValueMapContext extends SchemaValueContext implements SchemaValueMap {

    @JsonProperty
    private DataType ktype;

    SchemaValueMapContext(NodeGlobal global, NodeLocation location, SchemaStructure structureModel, ParseRelation field) {
        super(global, location, NodeType.SCHEMA_MODEL_VALUE_MAP, RelationType.VALUE_MAP, structureModel, field);
        ktype = ((ParseValueMap) field).keyDataType.dataType;
    }

    public String getJsonKeyDataType() {
        return getKeyDataType().name();
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(this.getFieldNumber());
        builder.append(':');
        builder.append(this.getFieldName());
        builder.append(':');
        builder.append("map[");
        builder.append(this.getKeyDataType());
        builder.append(',');
        builder.append(this.getValueDataType());
        builder.append(']');
        builder.append(this.explainClassifiers());
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("structureName", getStructureName())
                .add("fieldNumber", getFieldNumber())
                .add("fieldName", getFieldName())
                .add("ktype", ktype)
                .add("valueDataType", getValueDataType())
                .add("classifiers", getClassifiers())
                .omitNullValues()
                .toString();
    }

    @Override
    public DataType getKeyDataType() {
        return ktype;
    }
}
