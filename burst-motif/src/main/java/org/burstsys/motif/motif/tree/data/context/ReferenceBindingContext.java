/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.data.context;

import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.data.ReferenceBinding;
import org.burstsys.motif.paths.schemas.RelationPath;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.schema.model.SchemaReference;

/**
 * A resolve data accces
 */
abstract class ReferenceBindingContext extends RelationBindingContext implements ReferenceBinding {

    ReferenceBindingContext(NodeGlobal global, NodeType ntype) {
        super(global, ntype);
    }

    ReferenceBindingContext(NodeGlobal global, NodeLocation location, NodeType ntype, RelationType rtype, RelationPath path) {
        super(global, location, ntype, rtype, path);
    }

    @Override
    public SchemaReference getReferenceRelation() {
        return (SchemaReference) getRelation();
    }

    @Override
    public DataType getDatatype() {
        return DataType.STRUCTURE;
    }
}
