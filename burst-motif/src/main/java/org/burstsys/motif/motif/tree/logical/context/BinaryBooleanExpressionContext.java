/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.logical.context;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.motif.tree.constant.context.BooleanConstantContext;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.motif.tree.logical.BinaryBooleanExpression;
import org.burstsys.motif.motif.tree.logical.BinaryBooleanOperatorType;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;
import org.burstsys.motif.paths.Path;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.lang.String.format;

public final class BinaryBooleanExpressionContext extends ExpressionContext implements BinaryBooleanExpression {

    @JsonProperty
    private BinaryBooleanOperatorType op;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private BooleanExpression left;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private BooleanExpression right;

    public BinaryBooleanExpressionContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.BINARY_BOOLEAN);
    }

    public BinaryBooleanExpressionContext(NodeGlobal global, NodeLocation location, BinaryBooleanOperatorType op, BooleanExpression left, BooleanExpression right) {
        super(global, location, NodeType.BINARY_BOOLEAN);
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols) {
        if (this.canReduceToConstant()) {
            return (Expression) this.reduceToConstant().optimize(pathSymbols);
        } else {
            left = (BooleanExpression) left.optimize(pathSymbols);
            right = (BooleanExpression) right.optimize(pathSymbols);
            return this;
        }
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        stack.push(this);
        left.bind(pathSymbols, stack);
        right.bind(pathSymbols, stack);
        stack.pop();
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        stack.push(this);
        left.validate(pathSymbols, scope, stack);
        right.validate(pathSymbols, scope, stack);
        stack.pop();
        if (!left.getDtype().equals(DataType.BOOLEAN))
            throw new ParseException(getLocation(), "left hand side must be a boolean");
        if (!right.getDtype().equals(DataType.BOOLEAN))
            throw new ParseException(getLocation(), "right hand side must be a boolean");
        if (Path.lowest(right.getLowestEvaluationPoint(), scope) == null)
            throw new ParseException(getLocation(), "right hand side is out of scope " + scope);
        if (Path.lowest(left.getLowestEvaluationPoint(),scope) == null)
            throw new ParseException(getLocation(), "left hand side is out of scope " + scope);

    }

    @Override
    public DataType getDtype() {
        return DataType.BOOLEAN;
    }

    @Override
    public Path getLowestEvaluationPoint() {
        return Path.lowest(left.getLowestEvaluationPoint(), right.getLowestEvaluationPoint());
    }

    @Override
    public String generateMotif(int level) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(left.generateMotif(level + 1));
        builder.append(' ');
        builder.append(op);
        builder.append(' ');
        builder.append(right.generateMotif(level + 1));
        builder.append(')');
        return returnCleanString(builder);
    }

    @Override
    public Constant reduceToConstant() {
        checkCanReduceToConstant();
        Constant leftConstant = left.reduceToConstant();
        Constant rightConstant = right.reduceToConstant();
        if (!leftConstant.isBoolean() || !rightConstant.isBoolean())
            throw new ParseException(getLocation(), format("can't reduce %s %s %s", leftConstant, op, rightConstant));
        return new BooleanConstantContext(getGlobal(), getLocation(), op.evaluate(leftConstant.asBoolean(), rightConstant.asBoolean()));
    }

    @Override
    public BinaryBooleanOperatorType getOp() {
        return op;
    }

    @Override
    public BooleanExpression getLeft() {
        return left;
    }

    @Override
    public BooleanExpression getRight() {
        return right;
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(op);
        builder.append('\n');
        builder.append(left.explain(level + 1));
        builder.append(right.explain(level + 1));
        builder.append(indent(level));
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("rtype", op)
                .add("lower", left)
                .add("upper", right)
                .toString();
    }

    @Override
    public Boolean canReduceToConstant() {
        return left.canReduceToConstant() && right.canReduceToConstant();
    }

    // parent interface returns the left and right as children
    @Override
    public List<Expression> getChildren() {
        return Arrays.asList(left, right);
    }

    @Override
    public int childCount() {
        return 2;
    }

    @Override
    public Expression getChild(int index) {
        switch (index) {
            case 0: return left;
            case 1: return right;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public Expression setChild(int index, Expression value) {
        Expression old;
        switch (index) {
            case 0:
                old = left;
                left = (BooleanExpression)value;
                return old;
            case 1:
                old = right;
                right = (BooleanExpression)value;
                return old;
            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
