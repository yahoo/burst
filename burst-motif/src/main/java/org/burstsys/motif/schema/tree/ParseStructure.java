/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.tree;

import org.burstsys.motif.common.NodeContext;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;

import java.util.ArrayList;

import static com.google.common.base.MoreObjects.toStringHelper;

public final class ParseStructure extends NodeContext {

    public String structureName;
    public ArrayList<ParseRelation> fields;

    public ParseStructure(NodeGlobal global, NodeLocation location, String structureName, ArrayList<ParseRelation> fields) {
        super(global, location, NodeType.SCHEMA_PARSE_STRUCTURE);
        this.structureName = structureName;
        this.fields = fields;
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("structureName", structureName)
                .add("fields", fields)
                .omitNullValues()
                .toString();
    }

}
