/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.data.context;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.data.FunnelBinding;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.paths.schemas.RelationType;

/**
 * A resolve data accces
 */
public final class FunnelBindingContext extends BindingContext implements FunnelBinding {

    private Path enclosingPath;

    @SuppressWarnings("unused")
    public FunnelBindingContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.REFERENCE_SCALAR_BINDING);
    }

    FunnelBindingContext(NodeGlobal global, NodeLocation location, Path path, Path enclosingPath) {
        super(global, location, NodeType.REFERENCE_SCALAR_BINDING, RelationType.REFERENCE_SCALAR, path);
        this.enclosingPath = enclosingPath;
    }

    @Override
    public DataType getDatatype() {
        return null;
    }

    @Override
    Path getEvalPath() {
        return enclosingPath;
    }
}
