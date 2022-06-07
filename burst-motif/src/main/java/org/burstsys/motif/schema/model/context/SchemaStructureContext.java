/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.model.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.schema.model.SchemaRelation;
import org.burstsys.motif.schema.model.SchemaStructure;
import org.burstsys.motif.schema.tree.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.lang.String.format;

public final class SchemaStructureContext extends NodeContext implements SchemaStructure {

    @JsonProperty("name")
    private String structureName;

    private HashMap<Integer, SchemaRelationContext> relationNumberMap = new HashMap<>();

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty("relations")
    private HashMap<String, SchemaRelationContext> relationNameMap = new HashMap<>();

    SchemaStructureContext(NodeGlobal global, NodeLocation location, String structureName, ArrayList<ParseRelation> fields) {
        super(global, location, NodeType.SCHEMA_MODEL_STRUCTURE);
        this.structureName = structureName;
        for (ParseRelation field : fields) {
            if (relationNumberMap.containsKey(field.getFieldNumber()))
                throw new ParseException(field,
                        format("'%s.%s' has redundant field number %d", structureName, field.getFieldName(), field.getFieldNumber())
                );
            if (field instanceof ParseValueScalar) {
                SchemaValueScalarContext relation = new SchemaValueScalarContext(getGlobal(), field.getLocation(), this, field);
                addRelation(field, relation);
            } else if (field instanceof ParseValueMap) {
                SchemaValueMapContext relation = new SchemaValueMapContext(getGlobal(), field.getLocation(), this, field);
                addRelation(field, relation);
            } else if (field instanceof ParseValueVector) {
                SchemaValueVectorContext relation = new SchemaValueVectorContext(getGlobal(), field.getLocation(), this, field);
                addRelation(field, relation);
            } else if (field instanceof ParseReferenceScalar) {
                SchemaReferenceScalarContext relation = new SchemaReferenceScalarContext(getGlobal(), field.getLocation(), this, field);
                addRelation(field, relation);
            } else if (field instanceof ParseReferenceVector) {
                SchemaReferenceVectorContext relation = new SchemaReferenceVectorContext(getGlobal(), field.getLocation(), this, field);
                addRelation(field, relation);
            }
        }
    }

    private void addRelation(ParseRelation field, SchemaRelationContext relation) {
        relationNumberMap.put(field.getFieldNumber(), relation);
        relationNameMap.put(field.getFieldName(), relation);
    }

    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append('\'');
        builder.append(structureName);
        builder.append('\'');
        builder.append('\n');
        for (SchemaRelationContext relation : relationNumberMap.values()) {
            builder.append(relation.explain(level + 1));
        }
        builder.append(indent(level));
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("structureName", structureName)
                .add("relationNumberMap", relationNumberMap)
                .omitNullValues()
                .toString();
    }


    @Override
    public String getStructureName() {
        return structureName;
    }

    @Override
    public Map<Integer, SchemaRelation> getRelationNumberMap() {
        return Collections.unmodifiableMap(relationNumberMap);
    }

    @Override
    public Map<String, SchemaRelation> getRelationNameMap() {
        return Collections.unmodifiableMap(relationNameMap);
    }
}
