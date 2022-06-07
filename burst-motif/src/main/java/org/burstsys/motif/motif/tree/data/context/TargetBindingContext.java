/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.data.context;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.eql.queries.Target;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.paths.schemas.RelationType;

/**
 *  Bind to a target expression
 */
public final class TargetBindingContext extends BindingContext  {

    private Target target;

    @SuppressWarnings("unused")
    public TargetBindingContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.TARGET_BINDING);

    }

    TargetBindingContext(NodeGlobal global, NodeLocation location, Path p, Target t) {
        super(global, location, NodeType.TARGET_BINDING, RelationType.TARGET, p);
        target = t;
    }

    @Override
    public DataType getDatatype() {
        return target.getDtype();
    }

    public Target getTarget() {
        return target;
    }
}
