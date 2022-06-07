/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.data.context;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.data.SegmentBinding;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.paths.segments.SegmentMembersFieldPath;
import org.burstsys.motif.paths.segments.SegmentMembersPath;

/**
 * A resolve data accces
 */
public final class SegmentBindingContext extends BindingContext implements SegmentBinding {

    @SuppressWarnings("unused")
    public SegmentBindingContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.INSTANCE_BINDING);

    }

    SegmentBindingContext(NodeGlobal global, NodeLocation location, SegmentMembersPath path) {
        super(global, location, NodeType.INSTANCE_BINDING, RelationType.INSTANCE, path);
    }

    @Override
    public DataType getDatatype() {
        if (path instanceof SegmentMembersFieldPath)
            return ((SegmentMembersFieldPath)path).getField().getDType();
        else
            return null;
    }
}
