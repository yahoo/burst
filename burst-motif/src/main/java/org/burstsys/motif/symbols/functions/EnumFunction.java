/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.symbols.functions;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.context.FunctionDefinitionContext;
import org.burstsys.motif.motif.tree.values.FunctionExpression;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static org.burstsys.motif.symbols.functions.Functions.ENUM;
import static java.lang.String.format;

/**
 * Split function is a dimension that does takes a relation field and compares it to a sequence of values
 */
public class EnumFunction extends FunctionDefinitionContext {
    public EnumFunction() {
        super(ENUM);
    }

    @Override
    public DataType getDtype(UsageContext context) {
        return ((EnumContext)context).target.getDtype();
    }

    // call context information for a given call instance
    public static class EnumContext implements FunctionExpression.FunctionContext {
       @Override
        public String getFunctionName() {
            return ENUM;
        }
        public List<ValueExpression> values;
        public ValueExpression target;
    }

    @Override
    public void validate(PathSymbols symbols, FunctionExpression call, FunctionExpression.FunctionContext context, Path scope, Stack<Evaluation> stack) {
        EnumContext fc = (EnumContext) call.getContext();

        if (call.getParms().size() < 2)
            throw new ParseException(call.getLocation(), format("%s requires a relation and one or more enum values", this.getName()));

        fc.target = call.getParms().get(0);
        if (fc.target.getDtype().notNumeric() && fc.target.getDtype() != DataType.STRING)
            throw new ParseException(format("%s path '%s' expression must be a numeric or string", this.getName(), fc.target));

        // collect the split ranges
        fc.values = new ArrayList<>(call.getParms().size()-1);
        for (int i=1; i< call.getParms().size(); i++) {
            ValueExpression p = call.getParms().get(i);
            if (!p.canReduceToConstant())
                throw new ParseException(format("%s parameter '%s' must reduce to a constant", this.getName(), p));
            if (!DataType.hasCommonDtype(p.getDtype(), fc.target.getDtype()))
                throw new ParseException(format("%s parameter '%s' match type of field %s", this.getName(), p, fc.target));

            fc.values.add(p);
        }
    }

    @Override
    public EnumContext bind(PathSymbols pathSymbols, List<ValueExpression> parms, Stack<Evaluation> stack) {
        super.bind(pathSymbols, parms, stack);
        return new EnumContext();
    }


    @Override
    public Path getLowestEvaluationPoint(FunctionExpression call) {
        Path t = Path.lowest(call.getParms());
        return t.getParentStructure();
    }
}
