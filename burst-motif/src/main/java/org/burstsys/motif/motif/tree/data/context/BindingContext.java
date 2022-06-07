/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.data.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.data.SchemaBinding;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.paths.schemas.RelationType;

/**
 * A resolve data accces
 */
public abstract class BindingContext extends NodeContext implements SchemaBinding {


    protected Path path;

    @JsonProperty
    private RelationType rtype;

    BindingContext(NodeGlobal global, NodeLocation location, NodeType ntype, RelationType rtype, Path path) {
        super(global, location, ntype);
        this.rtype = rtype;
        this.path = path;
    }

    BindingContext(NodeGlobal global, NodeType ntype) {
        super(global, ntype);
    }

    @Override
    public final Path getPath() {
        return path;
    }

    @Override
    public final RelationType getRelationType() {
        return rtype;
    }

    Path getEvalPath() { return path.getEnclosingStructure(); }
}
