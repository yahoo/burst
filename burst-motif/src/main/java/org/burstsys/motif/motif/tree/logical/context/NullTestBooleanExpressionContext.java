/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.logical.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.motif.tree.constant.context.BooleanConstantContext;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.motif.tree.logical.NullTestBooleanExpression;
import org.burstsys.motif.motif.tree.logical.NullTestOperatorType;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * a clause that captures is an expression is null (or not null)
 */
public final class NullTestBooleanExpressionContext extends ExpressionContext implements NullTestBooleanExpression {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private ValueExpression expr;

    @JsonProperty
    private NullTestOperatorType op;

    public NullTestBooleanExpressionContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.NULL_TEST);
    }

    public NullTestBooleanExpressionContext(NodeGlobal global, NodeLocation location, ValueExpression expr, NullTestOperatorType op) {
        super(global, location, NodeType.NULL_TEST);
        this.expr = expr;
        this.op = op;
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols) {
        if (this.canReduceToConstant()) {
            return (Expression) this.reduceToConstant().optimize(pathSymbols);
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
        return DataType.BOOLEAN;
    }

    @Override
    public Path getLowestEvaluationPoint() {
        return expr.getLowestEvaluationPoint();
    }

    @Override
    public String generateMotif(int level) {
        StringBuilder builder = new StringBuilder();
        builder.append(' ');
        builder.append(expr.generateMotif(level + 1));
        builder.append(' ');
        builder.append(op.generateMotif(level + 1));
        builder.append(' ');
        return returnCleanString(builder);
    }


    @Override
    public Constant reduceToConstant() {
        checkCanReduceToConstant();
        Constant constantExpression = expr.reduceToConstant();
        if (!constantExpression.isNull()) {
            return new BooleanConstantContext(getGlobal(), getLocation(), false);
        } else {
            return new BooleanConstantContext(getGlobal(), getLocation(), op == NullTestOperatorType.IS_NULL);
        }
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
                .add("expr", expr)
                .toString();
    }

    @Override
    public ValueExpression getExpr() {
        return expr;
    }

    @Override
    public NullTestOperatorType getOp() {
        return op;
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
