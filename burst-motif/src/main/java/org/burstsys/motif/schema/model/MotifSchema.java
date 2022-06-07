/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.model;

import org.burstsys.motif.common.Node;
import org.burstsys.motif.motif.tree.eql.common.Schema;

import java.util.List;
import java.util.Map;

public interface MotifSchema extends Node, Schema {
    String getSchemaName();

    String getRootFieldName();

    String getRootStructureName();

    SchemaStructure getRootStructure();

    Map<String, SchemaStructure> getStructureNameMap();

    Map<String, SchemaReference> getStructurePathMap();

    Map<String, SchemaRelation> getRelationPathMap();

    List<SchemaStructure> getStructurePathList();

    List<SchemaRelation> getRelationList();

}
