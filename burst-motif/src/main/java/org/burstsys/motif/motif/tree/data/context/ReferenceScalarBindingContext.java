/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.data.context;

import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.data.ReferenceScalarBinding;
import org.burstsys.motif.paths.schemas.RelationPath;
import org.burstsys.motif.paths.schemas.RelationType;

/**
 * A resolve data accces
 */
public final class ReferenceScalarBindingContext extends ReferenceBindingContext implements ReferenceScalarBinding {

    public ReferenceScalarBindingContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.REFERENCE_SCALAR_BINDING);
    }

    ReferenceScalarBindingContext(NodeGlobal global, NodeLocation location, RelationPath path) {
        super(global, location, NodeType.REFERENCE_SCALAR_BINDING, RelationType.REFERENCE_SCALAR, path);
    }
}
