/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.data.context;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.data.InstanceBinding;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.paths.schemas.RelationType;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * A resolve data accces
 */
public final class InstanceBindingContext extends BindingContext implements InstanceBinding {

    InstanceBindingContext(NodeGlobal global, NodeLocation location, Path path) {
        super(global, location, NodeType.INSTANCE_BINDING, RelationType.INSTANCE, path);
    }

    public InstanceBindingContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.INSTANCE_BINDING);

    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append('\'');
        builder.append(path);
        builder.append('\'');
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("path", path)
                .toString();
    }

    @Override
    public DataType getDatatype() {
        return DataType.STRUCTURE;
    }
}
