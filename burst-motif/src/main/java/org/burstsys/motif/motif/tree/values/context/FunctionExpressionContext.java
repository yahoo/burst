/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.values.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.expression.FunctionDefinition;
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.motif.tree.values.FunctionExpression;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.symbols.Definition;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.lang.String.format;

public final class FunctionExpressionContext extends ExpressionContext implements FunctionExpression {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private List<ValueExpression> parms;

    @JsonProperty
    private String name = null;

    private FunctionDefinition functionDefinition;

    private FunctionContext functionContext;

    public FunctionExpressionContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.FUNCTION);
    }

    public FunctionExpressionContext(NodeGlobal global, NodeLocation location, ArrayList<ValueExpression> parms, String name) {
        super(global, location, NodeType.FUNCTION);
        this.parms = parms;
        this.name = name;
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        Definition d = pathSymbols.getDefinition(Definition.Context.FUNCTION, name);
        if (d == null) {
            throw new ParseException(getLocation(), format("FunctionDefinition '%s' is not defined", name));
        } else if ( d instanceof FunctionDefinition) {
            this.functionDefinition = (FunctionDefinition) d;
        } else {
            throw new ParseException(getLocation(), format("Identifier '%s' is not a function (%s)", name, d.getClass().toString()));
        }

        this.functionContext = functionDefinition.bind(pathSymbols, parms, stack);
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        stack.push(this);
        for (ValueExpression parm : parms) {
            parm.validate(pathSymbols, scope, stack);
        }
        stack.pop();
        functionDefinition.validate(pathSymbols, this, this.functionContext, scope, stack);
    }

    @Override
    public DataType getDtype() {
        return functionDefinition.getDtype(this.functionContext);
    }

    @Override
    public Path getLowestEvaluationPoint() {
        return functionDefinition.getLowestEvaluationPoint(this);
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols) {
        parms = parms.stream().map(p -> (ValueExpression)p.optimize(pathSymbols)).collect(Collectors.toCollection(ArrayList::new));
        return functionDefinition.optimize(pathSymbols, this, this.functionContext);
    }


    @Override
    public String generateMotif(int level) {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        builder.append('(');
        for (ValueExpression parm : parms) {
            builder.append(parm.generateMotif(level + 1));
            builder.append(',');
            builder.append(' ');
        }
        if (!parms.isEmpty())
            builder.deleteCharAt(builder.lastIndexOf(","));
        builder.append(')');
        return returnCleanString(builder);
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .toString();
    }


    @Override
    public Boolean canReduceToConstant() {
        return false;
    }

    @Override
    public String getFunctionName() {
        return name;
    }

    @Override
    public List<ValueExpression> getParms() {
        return parms;
    }

    @Override
    public FunctionContext getContext() {
        assert this.functionContext.getFunctionName().equals(this.getFunctionName());
        return this.functionContext;
    }

    @Override
    public void setContext(FunctionContext ctx) {
        assert this.getFunctionName().equals(ctx.getFunctionName());
        this.functionContext = ctx;
    }

    // parent interface returns the child
    @Override
    public List<Expression> getChildren() {
        return  new ArrayList<>(parms);
    }

    @Override
    public int childCount() {
        return parms.size();
    }

    @Override
    public Expression getChild(int index) {
        if (index >= 0 && index < parms.size())
            return parms.get(index);
        else
            throw new IndexOutOfBoundsException();
    }

    @Override
    public Expression setChild(int index, Expression value) {
        if (index >= 0 && index < parms.size()) {
            Expression old;
            old = parms.get(index);
            parms.set(index, (ValueExpression)value);
            return old;
        } else
            throw new IndexOutOfBoundsException();
    }
}
