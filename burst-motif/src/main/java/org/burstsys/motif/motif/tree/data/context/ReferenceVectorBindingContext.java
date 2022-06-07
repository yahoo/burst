/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.data.context;

import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.paths.schemas.RelationPath;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.motif.tree.data.ReferenceVectorBinding;
import org.burstsys.motif.schema.model.SchemaReferenceVector;

/**
 * A resolve data accces
 */
public final class ReferenceVectorBindingContext extends ReferenceBindingContext implements ReferenceVectorBinding {

    public ReferenceVectorBindingContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.REFERENCE_VECTOR_BINDING);

    }

    ReferenceVectorBindingContext(NodeGlobal global, NodeLocation location, RelationPath path) {
        super(global, location, NodeType.REFERENCE_VECTOR_BINDING, RelationType.REFERENCE_VECTOR, path);
    }

    @Override
    public SchemaReferenceVector getReferenceVectorRelation() {
        return (SchemaReferenceVector) getRelation();
    }
}
