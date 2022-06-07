/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.data.context;

import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.data.ValueMapBinding;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.schemas.RelationPath;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.schema.model.SchemaValueMap;

/**
 * A resolve data accces
 */
public final class ValueMapBindingContext extends ValueBindingContext implements ValueMapBinding {

    private ValueExpression mapKeyExpression;

    public ValueMapBindingContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.VALUE_MAP_BINDING);

    }

    ValueMapBindingContext(NodeGlobal global, NodeLocation location, RelationPath path, ValueExpression mapKeyExpression) {
        super(global, location, NodeType.VALUE_MAP_BINDING, RelationType.VALUE_MAP, path);
        this.mapKeyExpression = mapKeyExpression;
    }

    DataType getKeyType() {
        return getValueMapRelation().getKeyDataType();
    }

    @Override
    public SchemaValueMap getValueMapRelation() {
        return (SchemaValueMap) getRelation();
    }

    @Override
    public ValueExpression getMapKeyExpression() {
        return mapKeyExpression;
    }
}
