/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.paths.schemas;

import org.burstsys.motif.schema.model.MotifSchema;
import org.burstsys.motif.schema.model.SchemaReference;
import org.burstsys.motif.schema.model.context.SchemaRelationContext;

/**
 * Help with paths
 */
public class ValueVectorPath extends RelationPath {

    ValueVectorPath(MotifSchema s, String fullPath, SchemaReference sr , SchemaRelationContext src) {
        super(s, fullPath, sr, src);
    }

    @Override
    public StructurePath getEnclosingStructure() {
        return this;
    }

    @Override
    public StructurePath getParentStructure() {
        return new StructurePath(getSchema(), SchemaPathBase.pathWithoutRelationAsString(components), getStructureReference());
    }
}
