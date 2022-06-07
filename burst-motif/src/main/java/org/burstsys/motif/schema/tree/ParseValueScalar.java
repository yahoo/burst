/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.tree;

import org.burstsys.motif.common.Node;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.paths.schemas.RelationType;

import java.util.ArrayList;

public final class ParseValueScalar extends ParseValue {

    public ParseValueScalar(NodeGlobal global, NodeLocation location, Integer number, String name,
                            ArrayList<ParseClassifier> classifiers, Node valueDataType) {
        super(global, location, NodeType.SCHEMA_PARSE_VALUE_SCALAR, number, name, RelationType.VALUE_SCALAR, classifiers, valueDataType);
    }

}
