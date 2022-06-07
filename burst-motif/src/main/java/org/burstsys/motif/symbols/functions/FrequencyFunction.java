/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.symbols.functions;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.data.PathAccessor;
import org.burstsys.motif.motif.tree.eql.queries.Target;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.context.FunctionDefinitionContext;
import org.burstsys.motif.motif.tree.values.FunctionExpression;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.List;
import java.util.Optional;
import java.util.Stack;

import static org.burstsys.motif.symbols.functions.Functions.FREQUENCY;
import static java.lang.String.format;

/**
 * Frequency function is a complex dimension that does a `count` aggregation of the relation and the reset of the
 * count whenever the control expression changes.   Mainly used for session frequency calculations, but it could
 * be more broadly useful
 */
public class FrequencyFunction extends FunctionDefinitionContext {
    public FrequencyFunction() {
        super(FREQUENCY);
    }

    @Override
    public DataType getDtype(UsageContext context) {
        return DataType.LONG;
    }

    // call context information for a given call instance
    public static class FrequencyContext implements FunctionExpression.FunctionContext {
       @Override
        public String getFunctionName() {
            return FREQUENCY;
        }
        public ValueExpression dimensionExpression;
        public PathAccessor frequencyPath;
        public Path scopePath;
        public Target targetDimension;
    }

    @Override
    public void validate(PathSymbols symbols, FunctionExpression call,
                         FunctionExpression.FunctionContext context,
                         Path scope,
                         Stack<Evaluation> stack)
    {
        FrequencyContext fc = (FrequencyContext)call.getContext();

        if (call.getParms().size() != 2)
            throw new ParseException(call.getLocation(), format("%s requires a numeric expression and a target", this.getName()));

        ValueExpression expr =call.getParms().get(0);
        if (!(expr instanceof PathAccessor))
            throw new ParseException(format("%s expression '%s' must refer to a path", this.getName(), expr));
        fc.frequencyPath = (PathAccessor) expr;
        if (fc.frequencyPath.getBinding().getRelationType() != RelationType.INSTANCE &&
                fc.frequencyPath.getBinding().getRelationType() != RelationType.VALUE_MAP &&
                fc.frequencyPath.getBinding().getRelationType() != RelationType.VALUE_VECTOR
        )
            throw new ParseException(format("%s path '%s' wrong must refer to an instance", this.getName(), fc.frequencyPath));

        // extract the dimension expression into the call context
        fc.dimensionExpression = call.getParms().get(1);

        // the init expression must evaluate at the same level as the relation
        if (expr.getLowestEvaluationPoint().equals(fc.dimensionExpression.getLowestEvaluationPoint()))
            throw new ParseException(format("%s expression '%s' evaluate at the same level as '%s'", this.getName(), fc.dimensionExpression, expr));

        // need to find the top target
        Optional<Evaluation> t = stack.stream().filter(x -> x instanceof Target).findFirst();
        if (!t.isPresent())
            throw new ParseException(format("%s may only be used in a target", this.getName()));

        fc.targetDimension = (Target)t.get();
        fc.scopePath = scope;
    }

    @Override
    public FrequencyContext bind(PathSymbols pathSymbols, List<ValueExpression> parms, Stack<Evaluation> stack) {
        super.bind(pathSymbols, parms, stack);
        return new FrequencyContext();
    }

    @Override
    public Path getLowestEvaluationPoint(FunctionExpression call) {
        Path t = Path.lowest(call.getParms());
        return t.getParentStructure();
    }
}
