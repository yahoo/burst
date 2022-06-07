/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.tree;

import org.burstsys.motif.common.*;

import static com.google.common.base.MoreObjects.toStringHelper;

public final class ParseDataType extends NodeContext {

    public DataType dataType;

    public ParseDataType(NodeGlobal global, NodeLocation location, String datatypeString) {
        super(global, location, NodeType.SCHEMA_PARSE_DATA_TYPE);
        dataType = DataType.parse(datatypeString);
    }


    @Override
    public String toString() {
        return toStringHelper(this)
                .addValue(dataType)
                .toString();
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        return endExplain(builder);
    }
}
