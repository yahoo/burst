/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.expression;

import org.burstsys.motif.symbols.Definition;

public interface ParameterDefinition extends Definition {
    @Override
    default Context getContext() {
        return Context.PARAMETER;
    }
}
