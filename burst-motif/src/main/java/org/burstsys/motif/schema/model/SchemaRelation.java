/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.model;

import org.burstsys.motif.common.Node;
import org.burstsys.motif.paths.schemas.RelationType;

import java.util.List;

public interface SchemaRelation extends Node {

    /**
     * the type of the relation
     *
     * @return
     */
    RelationType getRelationType();

    /**
     * Field number
     *
     * @return
     */
    Integer getFieldNumber();

    /**
     * Field Name
     *
     * @return
     */
    String getFieldName();

    /**
     * any classifiers
     *
     * @return
     */
    List<SchemaClassifierType> getClassifiers();
}
