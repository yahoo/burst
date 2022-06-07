/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.symbols.functions;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.constant.context.LongConstantContext;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.expression.context.FunctionDefinitionContext;
import org.burstsys.motif.motif.tree.values.FunctionExpression;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.symbols.PathSymbols;
import org.joda.time.DateTime;

import java.util.Stack;

import static org.burstsys.motif.symbols.functions.Functions.DATETIME;
import static java.lang.String.format;

public class DateTimeFunction extends FunctionDefinitionContext {
    public DateTimeFunction() {
        super(DATETIME);
    }

    @Override
    public DataType getDtype(UsageContext context) {
        return DataType.LONG;
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols, FunctionExpression func, FunctionExpression.FunctionContext context) {
        ValueExpression parm = func.getParms().get(0);
        if (parm.canReduceToConstant()) {
            // at this point it is a string constant
            try {
                Long result = new DateTime(parm.reduceToConstant().asString()).getMillis();
                return (Expression) (new LongConstantContext(func.getGlobal(), func.getLocation(), result)).optimize(pathSymbols);
            } catch (Exception e) {
                throw new ParseException(func.getLocation(),
                        format("'%s' unable to parse constant '%s' as a date time string", func.getFunctionName(), parm));
            }
        } else
            return func;
    }

    @Override
    public void validate(PathSymbols symbols, FunctionExpression call, FunctionExpression.FunctionContext context, Path scope, Stack<Evaluation> stack) {
        if (call.getParms().size() != 1)
            throw new ParseException(call.getLocation(), format("Function %s requires a string argument", this.getName()));
        if (call.getParms().get(0).getDtype() != DataType.STRING)
            throw new ParseException(call.getLocation(),
                    format("'%s' argument '%s' must refer to a string expression", call.getFunctionName(), call.getParms().get(0)));
    }

    @Override
    public Path getLowestEvaluationPoint(FunctionExpression call) {
        Path t = Path.lowest(call.getParms());
        return t.getParentStructure();
    }
}
