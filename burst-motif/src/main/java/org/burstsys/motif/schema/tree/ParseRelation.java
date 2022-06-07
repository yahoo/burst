/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.tree;

import org.burstsys.motif.common.NodeContext;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.paths.schemas.RelationType;

import java.util.ArrayList;
import static com.google.common.base.MoreObjects.toStringHelper;

public abstract class ParseRelation extends NodeContext {
    private Integer fieldNumber;
    private String fieldName;
    private ArrayList<ParseClassifier> classifiers;

    public RelationType rtype;

    public ParseRelation(NodeGlobal global, NodeType ntype) {
        super(global, ntype);
    }

    protected ParseRelation(NodeGlobal global, NodeLocation location, NodeType ntype, Integer number, String name,
                            RelationType rtype, ArrayList<ParseClassifier> classifiers) {
        super(global, location, ntype);
        this.rtype = rtype;
        this.fieldName = name;
        this.fieldNumber = number;
        this.classifiers = classifiers;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("fieldNumber", fieldNumber)
                .add("fieldName", fieldName)
                .add("classifiers", classifiers)
                .omitNullValues().toString();
    }

    public Integer getFieldNumber() {
        return fieldNumber;
    }

    public String getFieldName() {
        return fieldName;
    }

    public ArrayList<ParseClassifier> getClassifiers() {
        return classifiers;
    }
}
