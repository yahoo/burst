/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.common;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.symbols.Definition;

import static org.burstsys.motif.common.DataType.NULL;

/**
 */
public interface Schema extends Statement, Definition {
    String getName();

    @Override
    default Context getContext() {
        return Context.SCHEMA;
    }

    @Override
    default DataType getDtype(UsageContext context) {
        return NULL;
    }
}
