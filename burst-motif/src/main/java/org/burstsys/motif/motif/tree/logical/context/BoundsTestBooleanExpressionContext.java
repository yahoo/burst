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
import org.burstsys.motif.motif.tree.logical.BoundsTestBooleanExpression;
import org.burstsys.motif.motif.tree.logical.BoundsTestOperatorType;
import org.burstsys.motif.motif.tree.values.BinaryValueComparisonOperator;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static org.burstsys.motif.motif.tree.logical.BoundsTestOperatorType.BETWEEN;
import static com.google.common.base.MoreObjects.toStringHelper;
import static java.lang.String.format;

/**
 * a clause that captures is an expression is null (or not null)
 */
public final class BoundsTestBooleanExpressionContext extends ExpressionContext implements BoundsTestBooleanExpression {

    @JsonProperty
    private BoundsTestOperatorType op;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private ValueExpression left;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private ValueExpression lower;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private ValueExpression upper;

    public BoundsTestBooleanExpressionContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.BOUNDS_TEST);
    }

    public BoundsTestBooleanExpressionContext(NodeGlobal global, NodeLocation location, ValueExpression left,
                                              ValueExpression lower, ValueExpression upper,
                                              BoundsTestOperatorType op) {
        super(global, location, NodeType.BOUNDS_TEST);
        this.left = left;
        this.lower = lower;
        this.upper = upper;
        this.op = op;
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols) {
        // look for big optimization
        if (canReduceToConstant()) {
            return (Expression) this.reduceToConstant().optimize(pathSymbols);
        } else {
            left = (ValueExpression) left.optimize(pathSymbols);
            lower = (ValueExpression) lower.optimize(pathSymbols);
            upper = (ValueExpression) upper.optimize(pathSymbols);
            return this;
        }
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        stack.push(this);
        left.bind(pathSymbols, stack);
        lower.bind(pathSymbols, stack);
        upper.bind(pathSymbols, stack);
        stack.pop();
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        stack.push(this);
        left.validate(pathSymbols, scope, stack);
        lower.validate(pathSymbols, scope, stack);
        upper.validate(pathSymbols, scope, stack);
        stack.pop();
        // check that the types are all compatible
        if (DataType.findCommonDtype(left.getDtype(), lower.getDtype()) == null)
            throw new ParseException(format("lower bound datatype '%s' isn't compatible with target datatype '%s'", lower.getDtype(), left.getDtype()));
        if (DataType.findCommonDtype(left.getDtype(), upper.getDtype()) == null)
            throw new ParseException(format("upper bound datatype '%s' isn't compatible with target datatype '%s'", upper.getDtype(), left.getDtype()));
        if (getLowestEvaluationPoint() == null)
            throw new ParseException(getLocation(), format("the left, '%s', upper, '%s', or lower '%s' expressions are not on the same axis", left, upper, lower));
        // the bounds cannot have an evaluation point lower than expression
        if (left.getLowestEvaluationPoint().higher(Path.lowest(upper.getLowestEvaluationPoint(), lower.getLowestEvaluationPoint())))
            throw new ParseException(getLocation(),
                    format("the upper '%s' or lower bounds '%s' require evaluation below the left '%s' expression", upper, lower, left));
    }

    @Override
    public DataType getDtype() {
        return DataType.BOOLEAN;
    }

    @Override
    public Path getLowestEvaluationPoint() {
        return Path.lowest(left.getLowestEvaluationPoint(), upper.getLowestEvaluationPoint(), lower.getLowestEvaluationPoint());
    }

    @Override
    public String generateMotif(int level) {
        StringBuilder builder = new StringBuilder();
        builder.append(' ');
        builder.append(left.generateMotif(level + 1));
        builder.append(' ');
        builder.append(op.generateMotif(level + 1));
        builder.append(' ');
        builder.append(lower.generateMotif(level + 1));
        builder.append(' ');
        builder.append("AND");
        builder.append(' ');
        builder.append(upper.generateMotif(level + 1));
        builder.append(' ');
        return returnCleanString(builder);
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(op);
        builder.append('\n');
        builder.append(left.explain(level + 1));
        builder.append(indent(level + 1));
        builder.append("BOUNDARIES(");
        builder.append('\n');
        builder.append(lower.explain(level + 2));
        builder.append(upper.explain(level + 2));
        builder.append(indent(level + 1));
        builder.append(")");
        builder.append('\n');
        builder.append(indent(level));
        return endExplain(builder);
    }

    @Override
    public Constant reduceToConstant() {
        checkCanReduceToConstant();
        Constant leftConstant = left.reduceToConstant();
        Constant lowerConstant = lower.reduceToConstant();
        Constant upperConstant = upper.reduceToConstant();
        boolean lowerBoundTest = leftConstant.binaryBooleanCompare(BinaryValueComparisonOperator.GTE, lowerConstant).getBooleanValue();
        boolean upperBoundTest = leftConstant.binaryBooleanCompare(BinaryValueComparisonOperator.LT, upperConstant).getBooleanValue();
        if (op == BETWEEN)
            return new BooleanConstantContext(getGlobal(), getLocation(), lowerBoundTest && upperBoundTest);
        else
            return new BooleanConstantContext(getGlobal(), getLocation(), !(lowerBoundTest && upperBoundTest));
    }

    @Override
    public Boolean canReduceToConstant() {
        return left.canReduceToConstant() && lower.canReduceToConstant() && upper.canReduceToConstant();
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("op", op)
                .add("left", left)
                .add("lower", lower)
                .add("upper", upper)
                .toString();
    }

    @Override
    public BoundsTestOperatorType getOp() {
        return op;
    }

    @Override
    public ValueExpression getLeft() {
        return left;
    }

    @Override
    public ValueExpression getLower() {
        return lower;
    }

    @Override
    public ValueExpression getUpper() {
        return upper;
    }

    // parent interface returns the left and right as children
    @Override
    public List<Expression> getChildren() {
        return Arrays.asList(left, lower, upper);
    }

    @Override
    public int childCount() {
        return 3;
    }

    @Override
    public Expression getChild(int index) {
        switch (index) {
            case 0: return left;
            case 1: return lower;
            case 2: return upper;
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
                old = lower;
                lower = (ValueExpression)value;
                return old;
            case 2:
                old = upper;
                upper = (ValueExpression)value;
                return old;
            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
