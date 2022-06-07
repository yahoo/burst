/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.tree;

import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.paths.schemas.RelationType;

import java.util.ArrayList;

public final class ParseReferenceVector extends ParseReference {

    public ParseReferenceVector(NodeGlobal global, NodeLocation location, Integer number, String name,
                                ArrayList<ParseClassifier> classifiers, String referenceTypeName) {
        super(global, location, NodeType.SCHEMA_PARSE_REFERENCE_VECTOR, number, name, RelationType.REFERENCE_VECTOR,
                classifiers, referenceTypeName);
    }

}
