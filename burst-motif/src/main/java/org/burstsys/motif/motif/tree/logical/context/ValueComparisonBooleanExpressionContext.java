/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.logical.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.motif.tree.logical.ValueComparisonBooleanExpression;
import org.burstsys.motif.motif.tree.values.BinaryValueComparisonOperator;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.lang.String.format;

/**
 * a clause that captures a value comparison between two expressions, left and right.
 */
public final class ValueComparisonBooleanExpressionContext extends ExpressionContext implements ValueComparisonBooleanExpression {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    protected ValueExpression left;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private ValueExpression right;

    @JsonProperty
    private BinaryValueComparisonOperator op;

    @SuppressWarnings("unused")
    public ValueComparisonBooleanExpressionContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.VALUE_COMPARE);

    }

    public ValueComparisonBooleanExpressionContext(NodeGlobal global, NodeLocation location, ValueExpression left,
                                                   ValueExpression right, BinaryValueComparisonOperator op) {
        super(global, location, NodeType.VALUE_COMPARE);
        this.left = left;
        this.right = right;
        this.op = op;
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols) {
        if (canReduceToConstant()) {
            return (Expression) this.reduceToConstant().optimize(pathSymbols);
        } else {
            left = (ValueExpression) left.optimize(pathSymbols);
            right = (ValueExpression) right.optimize(pathSymbols);
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
        // check for invalid null usage
        DataType t = DataType.findCommonDtype(left.getDtype(), right.getDtype());
        if (t == null)
            throw new ParseException(format("left side datatype '%s' isn't compatible with right side datatype '%s'",left.getDtype(), right.getDtype()));
        switch (op) {
            case GT:
            case LT:
            case GTE:
            case LTE:
                if (t.equals(DataType.NULL))
                    throw new ParseException(getLocation(), format("NULL type not allowed in '%s' comparison", op));
                break;
            case NEQ:
            case EQ:
        }

        if (getLowestEvaluationPoint() == null)
            throw new ParseException(getLocation(), format("the left, '%s', and right, '%s', expressions are not on the same axis", left, right));
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
        builder.append(' ');
        builder.append('(');
        builder.append(left.generateMotif(level + 1));
        builder.append(' ');
        builder.append(op.generateMotif(level + 1));
        builder.append(' ');
        builder.append(right.generateMotif(level + 1));
        builder.append(')');
        builder.append(' ');
        return returnCleanString(builder);
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
                .add("op", op)
                .add("left", left)
                .add("right", right)
                .toString();
    }

    @Override
    public Boolean canReduceToConstant() {
        return left.canReduceToConstant() && right.canReduceToConstant();
    }

    @Override
    public Constant reduceToConstant() {
        checkCanReduceToConstant();
        Constant leftConstant = left.reduceToConstant();
        Constant rightConstant = right.reduceToConstant();
        return leftConstant.binaryBooleanCompare(op, rightConstant);
    }

    @Override
    public BinaryValueComparisonOperator getOp() {
        return op;
    }

    @Override
    public ValueExpression getLeft() {
        return left;
    }

    @Override
    public ValueExpression getRight() {
        return right;
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
                left = (ValueExpression) value;
                return old;
            case 1:
                old = right;
                right = (ValueExpression)value;
                return old;
            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
