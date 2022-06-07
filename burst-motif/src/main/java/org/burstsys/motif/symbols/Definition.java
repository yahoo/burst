/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.symbols;

import org.burstsys.motif.common.DataType;

public interface Definition {
    interface UsageContext {}

    enum Context {
        FUNCTION, PARAMETER, SOURCE, FUNNEL, SEGMENT, ALIAS, SCHEMA, TARGET
    }

    /**
     *  Name of the declaration
     */
    String getName();

    /**
     * Scope of the name
     */
    Context getContext();

    /**
     * Return type,
     */
    DataType getDtype(UsageContext context);

    /**
     *  Motif equivalent
     */
    String generateMotif(int level);
}
