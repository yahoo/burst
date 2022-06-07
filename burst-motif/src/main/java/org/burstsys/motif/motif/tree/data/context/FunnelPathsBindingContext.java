/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.data.context;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.data.FunnelBinding;
import org.burstsys.motif.paths.funnels.FunnelPathFieldPath;
import org.burstsys.motif.paths.funnels.FunnelPathsPath;
import org.burstsys.motif.paths.schemas.RelationType;

/**
 * A resolve data accces
 */
public final class FunnelPathsBindingContext extends BindingContext implements FunnelBinding {

    @SuppressWarnings("unused")
    public FunnelPathsBindingContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.INSTANCE_BINDING);

    }

    FunnelPathsBindingContext(NodeGlobal global, NodeLocation location, FunnelPathsPath path) {
        super(global, location, NodeType.INSTANCE_BINDING, RelationType.INSTANCE, path);
    }

    @Override
    public DataType getDatatype() {
        if (path instanceof FunnelPathFieldPath)
            return ((FunnelPathFieldPath)path).getField().getDType();
        else
            return null;
    }
}
