/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.model;

import org.burstsys.motif.common.Node;

import java.util.Map;

public interface SchemaStructure extends Node {
    String getStructureName();

    Map<Integer, SchemaRelation> getRelationNumberMap();

    Map<String, SchemaRelation> getRelationNameMap();
}
