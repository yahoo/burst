/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.logical.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.constant.BooleanConstant;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.motif.tree.constant.context.BooleanConstantContext;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;
import org.burstsys.motif.motif.tree.logical.UnaryBooleanExpression;
import org.burstsys.motif.motif.tree.logical.UnaryBooleanOperatorType;
import org.burstsys.motif.paths.Path;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.lang.String.format;

public final class UnaryBooleanExpressionContext extends ExpressionContext implements UnaryBooleanExpression {

    @JsonProperty
    private UnaryBooleanOperatorType op;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private BooleanExpression expr;

    public UnaryBooleanExpressionContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.UNARY_BOOLEAN);
    }

    public UnaryBooleanExpressionContext(NodeGlobal global, NodeLocation location, UnaryBooleanOperatorType operatorType, BooleanExpression expr) {
        super(global, location, NodeType.UNARY_BOOLEAN);
        this.expr = expr;
        this.op = operatorType;
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols) {
        if (expr.canReduceToConstant()) {
            return (Expression) this.reduceToConstant().optimize(pathSymbols);
        } else {
            expr = (BooleanExpression) expr.optimize(pathSymbols);
            return this;
        }
    }

    @Override
    public Constant reduceToConstant() {
        checkCanReduceToConstant();
        BooleanConstant exprConstant = (BooleanConstant)expr.reduceToConstant();
       return new BooleanConstantContext(getGlobal(), getLocation(), !exprConstant.getBooleanValue());
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
        StringBuilder builder = new StringBuilder();
        builder.append(op);
        builder.append(" (");
        builder.append(expr.generateMotif(level + 1));
        builder.append(')');
        return returnCleanString(builder);
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(op);
        builder.append('\n');
        builder.append(expr.explain(level + 1));
        builder.append(indent(level));
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("op", op)
                .add("getExpr", expr)
                .toString();
    }

    @Override
    public UnaryBooleanOperatorType getOp() {
        return op;
    }

    @Override
    public BooleanExpression getExpr() {
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
            expr = (BooleanExpression) value;
            return old;
        }
        throw new IndexOutOfBoundsException();
    }
}
