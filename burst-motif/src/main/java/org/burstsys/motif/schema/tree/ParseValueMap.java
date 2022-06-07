/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.tree;

import org.burstsys.motif.common.Node;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.paths.schemas.RelationType;

import java.util.ArrayList;

import static com.google.common.base.MoreObjects.toStringHelper;

public final class ParseValueMap extends ParseValue {

    public ParseDataType keyDataType;

    public ParseValueMap(NodeGlobal global, NodeLocation location, Integer number, String name,
                         ArrayList<ParseClassifier> classifiers, Node keyDataType, Node valueDataType) {
        super(global, location, NodeType.SCHEMA_PARSE_VALUE_MAP, number, name, RelationType.VALUE_MAP, classifiers, valueDataType);
        this.keyDataType = (ParseDataType) keyDataType;
    }


    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(keyDataType.explain(level + 1));
        builder.append(valueDataType.explain(level + 1));
        builder.append(indent(level));
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("ktype", keyDataType)
                .add("valueDataType", valueDataType)
                .toString();
    }
}
