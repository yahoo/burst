/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.logical.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.motif.tree.logical.BooleanValueExpression;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.symbols.Definition;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static java.lang.String.format;

public final class BooleanValueExpressionContext extends ExpressionContext implements BooleanValueExpression {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private ValueExpression expr;

    @SuppressWarnings("unused")
    public BooleanValueExpressionContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.VALUE_BOOLEAN);
    }

    public BooleanValueExpressionContext(NodeGlobal global, NodeLocation location, ValueExpression expr) {
        super(global, location, NodeType.VALUE_BOOLEAN);
        this.expr = expr;
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols) {
        if (expr.canReduceToConstant()) {
            return (Expression) this.reduceToConstant().optimize(pathSymbols);
        } else {
            expr =  (ValueExpression) expr.optimize(pathSymbols);
            if (expr instanceof BooleanValueExpression)
                return expr;
            else
                return this;
        }
    }

    @Override
    public Constant reduceToConstant() {
        checkCanReduceToConstant();
        return expr.reduceToConstant();
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        stack.push(this);
        expr.bind(pathSymbols, stack);
        stack.pop();
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        stack.push(this);
        expr.validate(pathSymbols, scope, stack);
        stack.pop();
        if (!expr.getDtype().equals(DataType.BOOLEAN))
            throw new ParseException(getLocation(), format("expression '%s' must be a boolean", expr));
    }

    @Override
    public DataType getDtype() {
        return DataType.BOOLEAN;
    }

    @Override
    public Path getLowestEvaluationPoint() {
        return expr.getLowestEvaluationPoint();
    }

    @Override
    public String generateMotif(int level) {
        return expr.generateMotif(level);
    }

    @Override
    public String explain(int level) {
        return expr.explain(level);
    }

    @Override
    public String toString() {
        return expr.toString();
    }

    @Override
    public Expression getExpr() {
        return expr;
    }

    @Override
    public Boolean canReduceToConstant() {
        return expr.canReduceToConstant();
    }

    // parent interface returns the child
    @Override
    public List<Expression> getChildren() {
        return Collections.singletonList(expr);
    }

    @Override
    public int childCount() {
        return 1;
    }

    @Override
    public Expression getChild(int index) {
        if (index == 0) {
            return expr;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public Expression setChild(int index, Expression value) {
        Expression old;
        if (index == 0) {
            old = expr;
            expr = (ValueExpression) value;
            return old;
        }
        throw new IndexOutOfBoundsException();
    }
}
