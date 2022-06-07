/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.data.context;

import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.data.ValueBinding;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.paths.schemas.StructurePath;
import org.burstsys.motif.schema.model.SchemaValue;

/**
 * A resolve data acccess
 */
abstract class ValueBindingContext extends RelationBindingContext implements ValueBinding {

    ValueBindingContext(NodeGlobal global, NodeLocation location, NodeType ntype, RelationType rtype, StructurePath path) {
        super(global, location, ntype, rtype, path);
    }

    ValueBindingContext(NodeGlobal global, NodeType ntype) {
        super(global, ntype);
    }

    @Override
    public DataType getDatatype() {
        return getValueRelation().getValueDataType();
    }

    @Override
    public SchemaValue getValueRelation() {
        return (SchemaValue) getRelation();
    }
}
