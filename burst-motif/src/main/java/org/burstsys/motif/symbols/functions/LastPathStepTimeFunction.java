/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.symbols.functions;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.data.context.FunnelBindingContext;
import org.burstsys.motif.motif.tree.data.context.PathAccessorContext;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.context.FunctionDefinitionContext;
import org.burstsys.motif.motif.tree.values.FunctionExpression;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.List;
import java.util.Stack;

import static org.burstsys.motif.symbols.functions.Functions.LAST_PATH_STEP_TIME;
import static java.lang.String.format;

/**
 * Split function is a dimension that does takes a relation field and compares it to a sequence of values
 */
public class LastPathStepTimeFunction extends FunctionDefinitionContext {
    public LastPathStepTimeFunction() {
        super(LAST_PATH_STEP_TIME);
    }

    @Override
    public DataType getDtype(UsageContext context) {
        return DataType.LONG;
    }

    // call context information for a given call instance
    public static class LastPathStepTimeContext implements FunctionExpression.FunctionContext {
       @Override
        public String getFunctionName() {
            return LAST_PATH_STEP_TIME;
        }
        public String funnelName;
    }

    @Override
    public void validate(PathSymbols symbols, FunctionExpression call, FunctionExpression.FunctionContext context, Path scope, Stack<Evaluation> stack) {
        LastPathStepTimeContext fc = (LastPathStepTimeContext) call.getContext();

        if (call.getParms().size() != 1)
            throw new ParseException(call.getLocation(), format("Function %s requires a funnel name argument", this.getName()));
        ValueExpression ve = call.getParms().get(0);
        if (ve instanceof PathAccessorContext && ((PathAccessorContext)ve).getBinding() instanceof FunnelBindingContext) {
            fc.funnelName = ((PathAccessorContext) ve).getBinding().getPath().getPathAsString();
        } else
            throw new ParseException(call.getLocation(),
                    format("'%s' argument '%s' must be a funnel name", call.getFunctionName(), call.getParms().get(0)));
    }

    @Override
    public LastPathStepTimeContext bind(PathSymbols pathSymbols, List<ValueExpression> parms, Stack<Evaluation> stack) {
        super.bind(pathSymbols, parms, stack);
        return new LastPathStepTimeContext();
    }


    @Override
    public Path getLowestEvaluationPoint(FunctionExpression call) {
        Path t = Path.lowest(call.getParms());
        return t.getParentStructure();
    }
}
