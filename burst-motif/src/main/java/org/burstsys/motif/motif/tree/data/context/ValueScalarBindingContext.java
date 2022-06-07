/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.data.context;

import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.paths.schemas.RelationPath;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.motif.tree.data.ValueScalarBinding;
import org.burstsys.motif.schema.model.SchemaValueScalar;

/**
 * A resolve data accces
 */
public final class ValueScalarBindingContext extends ValueBindingContext implements ValueScalarBinding {

    public ValueScalarBindingContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.VALUE_SCALAR_BINDING);

    }

    ValueScalarBindingContext(NodeGlobal global, NodeLocation location, RelationPath path) {
        super(global, location, NodeType.VALUE_SCALAR_BINDING, RelationType.VALUE_SCALAR, path);
    }

    @Override
    public SchemaValueScalar getValueScalarRelation() {
        return (SchemaValueScalar) getRelation();
    }
}
