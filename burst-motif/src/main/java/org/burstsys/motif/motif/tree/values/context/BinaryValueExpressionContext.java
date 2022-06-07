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
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.motif.tree.values.BinaryValueExpression;
import org.burstsys.motif.motif.tree.values.BinaryValueOperatorType;
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
public final class BinaryValueExpressionContext extends ExpressionContext implements BinaryValueExpression {

    @JsonProperty
    private BinaryValueOperatorType op;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private ValueExpression left;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private ValueExpression right;

    @SuppressWarnings("unused")
    public BinaryValueExpressionContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.BINARY_VALUE);
    }

    public BinaryValueExpressionContext(NodeGlobal global, NodeLocation location, ValueExpression left,
                                        ValueExpression right, BinaryValueOperatorType op) {
        super(global, location, NodeType.BINARY_VALUE);
        this.op = op;
        this.left = left;
        this.right = right;
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

        if (left.getDtype().notNumeric())
            throw new ParseException(getLocation(), "left hand side must be a numeric");
        if (right.getDtype().notNumeric())
            throw new ParseException(getLocation(), "right hand side must be a numeric");
        if (getDtype().notNumeric())
            throw new ParseException(getLocation(), "common type must be a numeric");
        if (getLowestEvaluationPoint() == null)
            throw new ParseException(getLocation(), format("the left, '%s', and right, '%s', expressions are not on the same axis", left, right));
        stack.pop();
    }

    @Override
    public Path getLowestEvaluationPoint() {
        return Path.lowest(left.getLowestEvaluationPoint(), right.getLowestEvaluationPoint());
    }

    @Override
    public DataType getDtype() {
        return DataType.findCommonDtype(left.getDtype(), right.getDtype());
    }

    @Override
    public String generateMotif(int level) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(left.generateMotif(level + 1));
        builder.append(' ');
        builder.append(op.generateMotif(level + 1));
        builder.append(' ');
        builder.append(right.generateMotif(level + 1));
        builder.append(')');
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
                .add("type", op)
                .add("left", left)
                .add("right", right)
                .toString();
    }

    @Override
    public Constant reduceToConstant() {
        checkCanReduceToConstant();
        Constant leftConstant = left.reduceToConstant();
        Constant rightConstant = right.reduceToConstant();
        // we only do this on numbers
        if (!(leftConstant instanceof NumberConstant) || !(rightConstant instanceof NumberConstant)) {
            throw new ParseException(getLocation(),
                    format("can't reduce expression (%s %s %s)",
                            leftConstant.getDtype(), op, rightConstant.getDtype()));
        }
        return ((NumberConstant) leftConstant).binaryOperate(op, (NumberConstant) rightConstant);
    }

    @Override
    public BinaryValueOperatorType getOp() {
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
                left = (ValueExpression)value;
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
