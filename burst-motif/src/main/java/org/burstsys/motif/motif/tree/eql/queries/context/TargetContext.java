/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.queries.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.eql.queries.Target;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.paths.Path;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

public final class TargetContext extends ExpressionContext implements Target {

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private Expression expression;

    @SuppressWarnings("unused")
    public TargetContext() { super(NodeGlobal.defaultNodeGlobal(), NodeType.TARGET); }

    public TargetContext(NodeGlobal global, NodeLocation location, String name, Expression expression) {
        super(global, location, NodeType.TARGET);
        this.name = name;
        this.expression = expression;
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(this.expression.explain(level));
        builder.append(" as ");
        builder.append(this.name);
        return endExplain(builder);
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        stack.push(this);
        expression.bind(pathSymbols, stack);
        stack.pop();
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        stack.push(this);
        this.expression.validate(pathSymbols, scope, stack);
        stack.pop();
    }

    @Override
    public DataType getDtype(UsageContext context) {
        return getDtype();
    }

    @Override
    public DataType getDtype() {
        return expression.getDtype();
    }

    @Override
    public Path getLowestEvaluationPoint() {
        return this.expression.getLowestEvaluationPoint();
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String generateMotif(int level) {
        return name + "=" + this.expression.generateMotif(level);
    }

    @Override
    public Evaluation optimize(PathSymbols pathSymbols) {
        this.expression = (Expression)this.expression.optimize(pathSymbols);
        return this;
    }

    @Override
    public Boolean canReduceToConstant() {
        return super.canReduceToConstant();
    }

    // parent interface returns the child expression
    @Override
    public List<Expression> getChildren() {
        return Collections.singletonList(expression);
    }

    @Override
    public int childCount() {
        return 1;
    }

    @Override
    public Expression getChild(int index) {
        if (index == 0) {
            return expression;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public Expression setChild(int index, Expression value) {
        Expression old;
        if (index == 0) {
            old = expression;
            expression = value;
            return old;
        }
        throw new IndexOutOfBoundsException();
    }
}
