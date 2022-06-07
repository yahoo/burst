/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.paths.schemas;

import org.burstsys.motif.schema.model.MotifSchema;
import org.burstsys.motif.schema.model.SchemaReference;
import org.burstsys.motif.schema.model.SchemaStructure;

/**
 * Help with paths
 */
public class StructurePath extends SchemaPathBase {
    private final SchemaReference reference;

    StructurePath(MotifSchema s, String fullPath, SchemaReference sr) {
        super(s, fullPath);

        this.reference = sr;
    }

    public String getStructureName() {
        return getStructure().getStructureName();
    }

    SchemaReference getStructureReference() {
        return reference;
    }

    public SchemaStructure getStructure() {
        return getStructureReference().getReferenceType();
    }

    @Override
    public org.burstsys.motif.paths.Path getEnclosingStructure() {
        // this is already a path to a structure, but if it is a scalar we can treat it as being within the
        // parent
        StructurePath val = this;
        while(!val.isRoot() && val.getStructureReference().getRelationType() == RelationType.REFERENCE_SCALAR) {
            val = val.getParentStruct();
        }
        return val;
    }

    @Override
    public org.burstsys.motif.paths.Path getParentStructure() {
        return getParentStruct();
    }

    private StructurePath getParentStruct() {
        if (this.isRoot())
            return this;
        else
            return (StructurePath) SchemaPathBase.formPath(getSchema(), SchemaPathBase.pathWithoutRelationAsString(components), null);
    }
}
