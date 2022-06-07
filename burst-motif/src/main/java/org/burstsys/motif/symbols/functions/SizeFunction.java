/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.symbols.functions;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.data.PathAccessor;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.context.FunctionDefinitionContext;
import org.burstsys.motif.motif.tree.values.FunctionExpression;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.Stack;

import static org.burstsys.motif.symbols.functions.Functions.SIZE;
import static java.lang.String.format;

public class SizeFunction extends FunctionDefinitionContext {
    public SizeFunction() {
        super(SIZE);
    }

    @Override
    public DataType getDtype(UsageContext context) {
        return DataType.LONG;
    }

    @Override
    public void validate(PathSymbols symbols, FunctionExpression call, FunctionExpression.FunctionContext context, Path scope, Stack<Evaluation> stack) {
        if (call.getParms().size() != 1)
            throw new ParseException(call.getLocation(), format("Function %s requires a single collection argument", this.getName()));
        if (!(call.getParms().get(0) instanceof PathAccessor))
            throw new ParseException(call.getLocation(),
                    format("'%s' argument '%s' must refer to a path", call.getFunctionName(), call.getParms().get(0)));
        PathAccessor path = (PathAccessor) call.getParms().get(0);
        if (path.getBinding().getRelationType() != RelationType.INSTANCE &&
            path.getBinding().getRelationType() != RelationType.VALUE_MAP &&
            path.getBinding().getRelationType() != RelationType.VALUE_VECTOR
        )
            throw new ParseException(call.getLocation(),
                    format("'%s' path argument '%s' must refer to a collection", call.getFunctionName(), path));
    }

    @Override
    public Path getLowestEvaluationPoint(FunctionExpression call) {
        Path t = Path.lowest(call.getParms());
        return t.getParentStructure();
    }
}
