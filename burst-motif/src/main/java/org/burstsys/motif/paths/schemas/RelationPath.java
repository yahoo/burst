/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.paths.schemas;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.schema.model.MotifSchema;
import org.burstsys.motif.schema.model.SchemaReference;
import org.burstsys.motif.schema.model.context.SchemaRelationContext;

import static java.lang.String.format;

/**
 * Help with paths
 */
public class RelationPath extends StructurePath {
    private final SchemaRelationContext relation;

    RelationPath(MotifSchema s, String fullPath, SchemaReference sr , SchemaRelationContext src) {
        super(s, fullPath, sr);
        this.relation = src;

        // validate that the passed relation is valid
        if (!last().equals(relation.getFieldName()))
            throw new ParseException(format("final path name '%s' doesn't match relation field name '%s'", last(), relation.getFieldName()));
    }

    public RelationType getRelationType() {
        return relation.getRelationType();
    }

    public SchemaRelationContext getRelation() {
        return relation;
    }

    public String getFieldName() {
        return relation.getFieldName();
    }

    @Override
    public org.burstsys.motif.paths.Path getEnclosingStructure() {
        return new StructurePath(getSchema(), SchemaPathBase.pathWithoutRelationAsString(components), getStructureReference());
    }

    @Override
    public org.burstsys.motif.paths.Path getParentStructure() {
        StructurePath enclosing = new StructurePath(getSchema(), SchemaPathBase.pathWithoutRelationAsString(components), getStructureReference());
        return enclosing.getParentStructure();
    }
}
