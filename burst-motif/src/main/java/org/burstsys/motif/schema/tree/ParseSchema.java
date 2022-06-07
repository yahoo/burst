/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.tree;

import org.burstsys.motif.common.NodeContext;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;

import java.util.ArrayList;

import static com.google.common.base.MoreObjects.toStringHelper;

public final class ParseSchema extends NodeContext {

    public String schemaName;
    public ParseRoot root;
    public ArrayList<ParseStructure> structures;

    public ParseSchema(NodeGlobal global, NodeLocation location, String schemaName, ParseRoot root, ArrayList<ParseStructure> structures) {
        super(global, location, NodeType.SCHEMA_PARSE_TREE);
        this.schemaName = schemaName;
        this.root = root;
        this.structures = structures;
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("schemaName", schemaName)
                .add("root", root)
                .add("structures", structures)
                .omitNullValues().toString();
    }

}
