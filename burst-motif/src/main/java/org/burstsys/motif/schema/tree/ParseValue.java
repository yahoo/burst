/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.tree;

import org.burstsys.motif.common.*;
import org.burstsys.motif.paths.schemas.RelationType;

import java.util.ArrayList;

public abstract class ParseValue extends ParseRelation {

    public ParseDataType valueDataType;

    protected ParseValue(NodeGlobal global, NodeLocation location, NodeType ntype,
                         Integer number, String name,
                         RelationType relationType,
                         ArrayList<ParseClassifier> classifiers,
                         Node valueDataType) {
        super(global, location, ntype, number, name, relationType, classifiers);
        this.valueDataType = (ParseDataType) valueDataType;
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(valueDataType.explain(level + 1));
        builder.append(indent(level));
        return endExplain(builder);
    }

}
