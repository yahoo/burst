/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.tree;

import org.burstsys.motif.common.NodeContext;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;

import static com.google.common.base.MoreObjects.toStringHelper;

public final class ParseRoot extends NodeContext {

    public String fieldName;
    public String fieldType;

    public ParseRoot(NodeGlobal global, NodeLocation location, String rootFieldName, String rootFieldType) {
        super(global, location, NodeType.SCHEMA_PARSE_ROOT);
        fieldName = rootFieldName;
        fieldType = rootFieldType;
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("fieldName", fieldName)
                .add("fieldType", fieldType)
                .toString();
    }

}
