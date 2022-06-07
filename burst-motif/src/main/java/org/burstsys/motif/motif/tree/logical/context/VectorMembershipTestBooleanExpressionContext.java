/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.logical.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.data.PathAccessor;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.motif.tree.logical.MembershipTestOperatorType;
import org.burstsys.motif.motif.tree.logical.VectorMembershipTestBooleanExpression;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.lang.String.format;

/**
 * a clause that captures a test for whether a left expression expression is in (or not in) a members of right expression expressions.
 */
public final class VectorMembershipTestBooleanExpressionContext extends ExpressionContext implements VectorMembershipTestBooleanExpression {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private ValueExpression left;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private PathAccessor path;

    @JsonProperty
    private MembershipTestOperatorType op;

    public VectorMembershipTestBooleanExpressionContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.MEMBERSHIP_TEST_VECTOR);
    }

    public VectorMembershipTestBooleanExpressionContext(NodeGlobal global, NodeLocation location, ValueExpression left,
                                                        PathAccessor path,
                                                        MembershipTestOperatorType op) {
        super(global, location, NodeType.MEMBERSHIP_TEST_VECTOR);
        this.left = left;
        this.path = path;
        this.op = op;
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        stack.push(this);
        left.bind(pathSymbols, stack);
        path.bind(pathSymbols, stack);
        stack.pop();
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        stack.push(this);
        left.validate(pathSymbols, scope, stack);
        path.validate(pathSymbols, scope, stack);
        stack.pop();
        if (DataType.findCommonDtype(path.getDtype(), left.getDtype()) == null)
            throw new ParseException(format("member datatype '%s' isn't compatible with target datatype '%s'",left.getDtype(), path.getDtype()));
        if (getLowestEvaluationPoint() == null)
            throw new ParseException(getLocation(), format("the value, '%s', and path, '%s', expressions are not on the same axis", left, path));
    }

    @Override
    public DataType getDtype() {
        return DataType.BOOLEAN;
    }

    @Override
    public Path getLowestEvaluationPoint() {
        return Path.lowest(left.getLowestEvaluationPoint(), path.getLowestEvaluationPoint());
    }

    @Override
    public String generateMotif(int level) {
        StringBuilder builder = new StringBuilder();
        builder.append(' ');
        builder.append(left.generateMotif(level + 1));
        builder.append(' ');
        builder.append(op.generateMotif(level + 1));
        builder.append(' ');
        builder.append('(');
        builder.append(path.generateMotif(level + 1));
        builder.append(')');
        return returnCleanString(builder);
    }


    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(op);
        builder.append('\n');
        builder.append(left.explain(level + 1));
        builder.append(indent(level + 1));
        builder.append(path.explain(level + 1));
        builder.append(indent(level + 1));
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("op", op)
                .add("left", left)
                .add("path", path)
                .toString();
    }

    @Override
    public ValueExpression getLeft() {
        return left;
    }

    @Override
    public PathAccessor getPath() {
        return path;
    }

    @Override
    public MembershipTestOperatorType getOp() {
        return op;
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols) {
        left.optimize(pathSymbols);
        path.optimize(pathSymbols);
        return this;
    }

    @Override
    public Boolean canReduceToConstant() {
        return false;
    }

    // parent interface returns the left and path as children
    @Override
    public List<Expression> getChildren() {
        return Arrays.asList(left, path);
    }

    @Override
    public int childCount() {
        return 2;
    }

    @Override
    public Expression getChild(int index) {
        switch (index) {
            case 0: return left;
            case 1: return path;
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
                old = path;
                path = (PathAccessor)value;
                return old;
            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
