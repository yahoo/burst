/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.values.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.motif.tree.constant.NumberConstant;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.motif.tree.values.DateTimeQuantumExpression;
import org.burstsys.motif.motif.tree.values.DateTimeQuantumOperatorType;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.lang.String.format;

public final class DateTimeQuantumExpressionContext extends TimezonedExpressionContext implements DateTimeQuantumExpression {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private ValueExpression expr;

    @JsonProperty
    private DateTimeQuantumOperatorType op;

    @SuppressWarnings("unused")
    public DateTimeQuantumExpressionContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.TIME_QUANTUM, null);
    }

    public DateTimeQuantumExpressionContext(NodeGlobal global, NodeLocation location, ValueExpression expr, DateTimeQuantumOperatorType op, ValueExpression timezone) {
        super(global, location, NodeType.TIME_QUANTUM, timezone);
        this.op = op;
        this.expr = expr;
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        stack.push(this);
        expr.bind(pathSymbols, stack);
        stack.pop();
        super.bind(pathSymbols, stack);
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        super.validate(pathSymbols, scope, stack);
        stack.push(this);
        expr.validate(pathSymbols, scope, stack);
        stack.pop();
    }

    @Override
    public DataType getDtype() {
        return DataType.DATETIME;
    }

    @Override
    public Path getLowestEvaluationPoint() {
        return Path.lowest(expr.getLowestEvaluationPoint(), super.getLowestEvaluationPoint());
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols) {
        if (canReduceToConstant()) {
            return (Expression) this.reduceToConstant().optimize(pathSymbols);
        } else {
            expr = (ValueExpression) expr.optimize(pathSymbols);
            super.optimize(pathSymbols);
            return this;
        }
    }

    @Override
    public Constant reduceToConstant() {
        checkCanReduceToConstant();
        Constant exprConstant = expr.reduceToConstant();
        Constant zoneConstant = super.reduceToConstant();
        // we only do this on numbers
        if (!(exprConstant instanceof NumberConstant) || !(zoneConstant instanceof NumberConstant))
            throw new ParseException(getLocation(), format("can't reduce expression (%s %s %s)", op, expr.explain(), super.explain()));
        return op.evaluate(getLocation(),exprConstant.asLong(), zoneConstant.asLong());
    }

    @Override
    public String generateMotif(int level) {
        StringBuilder builder = new StringBuilder();
        builder.append(op.generateMotif(level + 1));
        builder.append('(');
        builder.append(expr.generateMotif(level + 1));
        builder.append(',');
        builder.append(super.generateMotif(level + 1));
        builder.append(')');
        return returnCleanString(builder);
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(op);
        builder.append('\n');
        builder.append(expr.explain(level + 1));
        builder.append('\n');
        builder.append(super.explain(level + 1));
        builder.append(indent(level));
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("op", op)
                .add("expr", expr)
                .add("timezone", super.toString())
                .toString();
    }

    @Override
    public ValueExpression getExpr() {
        return expr;
    }

    @Override
    public DateTimeQuantumOperatorType getOp() {
        return op;
    }

    @Override
    public Boolean canReduceToConstant() {
        return expr.canReduceToConstant() && super.canReduceToConstant();
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
