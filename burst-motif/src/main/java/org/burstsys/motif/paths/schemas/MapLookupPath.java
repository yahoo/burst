/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.paths.schemas;

import org.burstsys.motif.schema.model.MotifSchema;
import org.burstsys.motif.schema.model.SchemaReference;
import org.burstsys.motif.schema.model.context.SchemaRelationContext;

/**
 */
public class MapLookupPath extends RelationPath {

    MapLookupPath(MotifSchema s, String fullPath, SchemaReference sr , SchemaRelationContext src) {
        super(s, fullPath, sr, src);
    }

    /**
     *  Lookups are actually evaluated at the parent so the path is the enclosing structure
     */
    public String getPathAsString() {
        return super.getEnclosingStructure().getPathAsString();
    }

    public String toString() {
        return super.getPathAsString();
    }

    @Override
    public StructurePath getEnclosingStructure() {
        return this;
    }
}
