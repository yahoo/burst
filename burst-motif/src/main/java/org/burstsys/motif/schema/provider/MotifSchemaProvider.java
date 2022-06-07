/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.provider;

/**
 * The motif subsystem will find all implementations of MotifSchemaProvider and register them when a new
 * {@link org.burstsys.motif.Motif} instance is created. Subclasses <i>must</i> provide a nullary constructor.
 */
public interface MotifSchemaProvider {

    /**
     * @return the text of the schema
     */
    String getSchema();

    /**
     * @return a list of names by which the schema can be referred to.
     */
    String[] getSchemaNames();

}

