/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.model.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.burstsys.motif.common.NodeContext;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.schema.model.SchemaClassifierType;
import org.burstsys.motif.schema.model.SchemaRelation;
import org.burstsys.motif.schema.model.SchemaStructure;
import org.burstsys.motif.schema.tree.ParseClassifier;
import org.burstsys.motif.schema.tree.ParseRelation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.MoreObjects.toStringHelper;

public abstract class SchemaRelationContext extends NodeContext implements Comparable<SchemaRelationContext>, SchemaRelation {

    @JsonProperty("fnum")
    private Integer fieldNumber;

    @JsonProperty("fname")
    private String fieldName;

    private RelationType rtype;

    private String structureName;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ArrayList<SchemaClassifierType> classifiers = new ArrayList<>();

    SchemaRelationContext(NodeGlobal global, NodeLocation location, NodeType ntype, RelationType rtype, SchemaStructure structure, String fieldName, Integer fieldNumber) {
        super(global, location, ntype);
        this.rtype = rtype;
        this.structureName = structure.getStructureName();
        this.fieldNumber = fieldNumber;
        this.fieldName = fieldName;
    }

    SchemaRelationContext(NodeGlobal global, NodeLocation location, NodeType ntype, RelationType rtype, SchemaStructure structure, ParseRelation field) {
        this(global, location, ntype, rtype, structure, field.getFieldName(), field.getFieldNumber());
        for (ParseClassifier node : field.getClassifiers()) {
            classifiers.add(node.getClassifierType());
        }
    }


    String explainClassifiers() {
        if (classifiers.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        builder.append(':');
        for (SchemaClassifierType classifier : classifiers) {
            builder.append(classifier.asString());
            builder.append(',');
        }
        builder.deleteCharAt(builder.lastIndexOf(","));
        return builder.toString();
    }

    @Override
    public int compareTo(SchemaRelationContext that) {
        return this.fieldNumber.compareTo(that.fieldNumber);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("structureName", structureName)
                .add("fieldNumber", fieldNumber)
                .add("fieldName", fieldName)
                .add("classifiers", classifiers)
                .omitNullValues()
                .toString();
    }

    @Override
    public RelationType getRelationType() {
        return rtype;
    }

    String getStructureName() {
        return structureName;
    }

    @Override
    public Integer getFieldNumber() {
        return fieldNumber;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public List<SchemaClassifierType> getClassifiers() {
        return Collections.unmodifiableList(classifiers);
    }
}
