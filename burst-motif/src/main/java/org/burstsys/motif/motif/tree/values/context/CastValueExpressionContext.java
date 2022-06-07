/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.values.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.motif.tree.values.CastValueExpression;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static org.burstsys.motif.common.DataType.NULL;
import static com.google.common.base.MoreObjects.toStringHelper;

public final class CastValueExpressionContext extends ExpressionContext implements CastValueExpression {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private ValueExpression expr;

    @JsonProperty
    private DataType dtype;

    public CastValueExpressionContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.CAST);
    }

    public CastValueExpressionContext(NodeGlobal global, NodeLocation location, ValueExpression expr, DataType dtype) {
        super(global, location, NodeType.CAST);
        this.dtype = dtype;
        this.expr = expr;
        if (dtype == NULL)
            throw new ParseException(getLocation(), "can't cast to a NULL type");
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(dtype);
        builder.append('\n');
        builder.append(expr.explain(level + 1));
        builder.append(indent(level));
        return endExplain(builder);
    }

    @Override
    public Constant reduceToConstant() {
        checkCanReduceToConstant();
        Constant expressionConstant = expr.reduceToConstant();
        return expressionConstant.castTo(dtype);
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols) {
        if (expr.canReduceToConstant()) {
            return (Expression) expr.reduceToConstant().castTo(dtype).optimize(pathSymbols);
        } else {
            expr = (ValueExpression) expr.optimize(pathSymbols);
            return this;
        }
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
    }

    @Override
    public DataType getDtype() {
        return dtype;
    }

    @Override
    public Path getLowestEvaluationPoint() {
        return expr.getLowestEvaluationPoint();
    }

    @Override
    public String generateMotif(int level) {
        StringBuilder builder = new StringBuilder();
        builder.append("CAST");
        builder.append('(');
        builder.append(expr.generateMotif(level + 1));
        builder.append(' ');
        builder.append("AS");
        builder.append(' ');
        builder.append(dtype.generateMotif(level + 1));
        builder.append(')');
        return returnCleanString(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("dtype", dtype)
                .add("expr", expr)
                .toString();
    }

    @Override
    public ValueExpression getExpr() {
        return expr;
    }

    @Override
    public DataType getType() {
        return dtype;
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
