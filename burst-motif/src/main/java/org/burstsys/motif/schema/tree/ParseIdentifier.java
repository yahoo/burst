/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.tree;

import org.burstsys.motif.common.NodeContext;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;

import static com.google.common.base.MoreObjects.toStringHelper;

public final class ParseIdentifier extends NodeContext {

    private String value;

    public ParseIdentifier(NodeGlobal global, NodeLocation location, String v) {
        super(global, location, NodeType.SCHEMA_PARSE_IDENTIFER);
        value = v;
    }


    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(value);
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .addValue(value)
                .toString();
    }

    public String getValue() {
        return value;
    }
}
