/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.tree;

import org.burstsys.motif.common.*;
import org.burstsys.motif.paths.schemas.RelationType;

import java.util.ArrayList;

import static com.google.common.base.MoreObjects.toStringHelper;

public abstract class ParseReference extends ParseRelation {

    private String referencedTypeName;

    ParseReference(NodeGlobal global, NodeLocation location, NodeType ntype,
                   Integer number, String name,
                   RelationType relationType,
                   ArrayList<ParseClassifier> classifiers,
                   String referenceTypeName) {
        super(global, location, ntype, number, name, relationType, classifiers);
        this.referencedTypeName = referenceTypeName;
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(this.referencedTypeName);
        builder.append(indent(level));
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("referenceTypeName", referencedTypeName)
                .toString();
    }

    public String getReferencedTypeName() {
        return referencedTypeName;
    }
}
