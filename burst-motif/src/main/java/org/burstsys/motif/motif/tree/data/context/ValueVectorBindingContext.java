/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.data.context;

import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.paths.schemas.StructurePath;
import org.burstsys.motif.motif.tree.data.ValueVectorBinding;
import org.burstsys.motif.schema.model.SchemaValueVector;

/**
 * A resolve data accces
 */
public final class ValueVectorBindingContext extends ValueBindingContext implements ValueVectorBinding {

    public ValueVectorBindingContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.VALUE_VECTOR_BINDING);
    }

    ValueVectorBindingContext(NodeGlobal global, NodeLocation location, StructurePath path) {
        super(global, location, NodeType.VALUE_VECTOR_BINDING, RelationType.VALUE_VECTOR, path);
    }

    @Override
    public SchemaValueVector getValueVectorRelation() {
        return (SchemaValueVector) getRelation();
    }

}
