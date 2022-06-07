/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.queries;

import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.symbols.Definition;

public interface Target extends Expression, Definition {

   String getName();

    Expression getExpression();

    @Override
    default Context getContext() {
        return Context.SCHEMA;
    }
}
