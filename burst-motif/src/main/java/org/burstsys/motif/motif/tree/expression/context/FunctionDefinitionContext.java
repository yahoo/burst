/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.expression.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.burstsys.motif.motif.tree.expression.FunctionDefinition;

/**
 * This is a function definition context.
 *
 * Currently no language allows definitions of functions, but this is here in case we do.  For now,
 * this abstract function is used to instantiate predefined functions that are added to the symbol table by
 * the parser.
 *
 */
abstract public
class FunctionDefinitionContext implements FunctionDefinition {

    @JsonProperty
    private final String name;

    public FunctionDefinitionContext(String name) {
        this.name = name;
    }

    @Override
    public String generateMotif(int level) {
        return "Function Definition " +
                name +
                '(' +
                ')';
    }


    @Override
    public String toString() {
        /*
        return toStringHelper(this)
                .toString();
         */
        return generateMotif(0);
    }

    @Override
    public String getName() {
        return name;
    }
}
