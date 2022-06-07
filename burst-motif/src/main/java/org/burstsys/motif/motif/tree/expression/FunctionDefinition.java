/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.expression;

import org.burstsys.motif.motif.tree.values.FunctionExpression;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.symbols.Definition;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.List;
import java.util.Stack;

public interface FunctionDefinition extends Definition {
    default FunctionExpression.FunctionContext bind(PathSymbols pathSymbols, List<ValueExpression> parms, Stack<Evaluation> stack) {
        for (ValueExpression parm : parms) {
            parm.bind(pathSymbols, stack);
        }
        return null;
    }

    default Expression optimize(PathSymbols pathSymbols, FunctionExpression func, FunctionExpression.FunctionContext context) {
        return func;
    }

    void validate(PathSymbols symbols, FunctionExpression call, FunctionExpression.FunctionContext context, Path scope, Stack<Evaluation> stack);

    Path getLowestEvaluationPoint(FunctionExpression call);

    @Override
    default Context getContext() {
        return Context.FUNCTION;
    }
}
