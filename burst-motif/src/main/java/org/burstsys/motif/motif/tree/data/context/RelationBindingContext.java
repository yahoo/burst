/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.data.context;

import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.data.RelationBinding;
import org.burstsys.motif.paths.schemas.RelationPath;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.paths.schemas.StructurePath;
import org.burstsys.motif.schema.model.SchemaRelation;

/**
 * A resolve data accces
 */
abstract class RelationBindingContext extends BindingContext implements RelationBinding {

    RelationBindingContext(NodeGlobal global, NodeLocation location, NodeType ntype, RelationType rtype, StructurePath path) {
        super(global, location, ntype, rtype, path);
    }

    RelationBindingContext(NodeGlobal global, NodeType ntype) {
        super(global, ntype);
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append('\'');
        builder.append(path);
        builder.append('\'');
        builder.append(", ");
        builder.append('\'');
        builder.append(getRelationPath().getStructureName());
        builder.append('\'');
        builder.append('\n');
        builder.append(getRelationPath().getRelation().explain(level + 1));
        builder.append(indent(level));
        return endExplain(builder);
    }


    private RelationPath getRelationPath() {
       return ((RelationPath)path);
    }

    @Override
    public SchemaRelation getRelation() {
        return getRelationPath().getRelation();
    }

}
