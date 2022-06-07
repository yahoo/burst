/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.common;

import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.schema.model.MotifSchema;

import java.util.List;

/**
 * Placeholder for all motif statements
 */
public interface Statement extends Evaluation {
    MotifSchema getSchema();

    String getSchemaName();
}
