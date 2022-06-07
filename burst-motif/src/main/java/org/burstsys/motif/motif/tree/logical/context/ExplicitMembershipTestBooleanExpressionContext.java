/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.logical.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.constant.BooleanConstant;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.motif.tree.constant.NumberConstant;
import org.burstsys.motif.motif.tree.constant.StringConstant;
import org.burstsys.motif.motif.tree.constant.context.BooleanConstantContext;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.motif.tree.logical.ExplicitMembershipTestBooleanExpression;
import org.burstsys.motif.motif.tree.logical.MembershipTestOperatorType;
import org.burstsys.motif.motif.tree.values.BinaryValueComparisonOperator;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;

import java.util.*;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.lang.String.format;

/**
 * a clause that captures a test for whether a left expression expression is in (or not in) a members of right expression expressions.
 */
public final class ExplicitMembershipTestBooleanExpressionContext extends ExpressionContext implements ExplicitMembershipTestBooleanExpression {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private ValueExpression left;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private ArrayList<ValueExpression> members;

    @JsonProperty
    private MembershipTestOperatorType op;

    public ExplicitMembershipTestBooleanExpressionContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.MEMBERSHIP_TEST_EXPLICIT);
    }

    public ExplicitMembershipTestBooleanExpressionContext(NodeGlobal global, NodeLocation location, ValueExpression left,
                                                          ArrayList<ValueExpression> members,
                                                          MembershipTestOperatorType op) {
        super(global, location, NodeType.MEMBERSHIP_TEST_EXPLICIT);
        this.left = left;
        this.members = members;
        this.op = op;
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        stack.push(this);
        left.bind(pathSymbols, stack);
        for (ValueExpression member : members) {
            member.bind(pathSymbols, stack);
        }
        stack.pop();
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        stack.push(this);
        left.validate(pathSymbols, scope, stack);
        for (ValueExpression member : members) {
            member.validate(pathSymbols, scope, stack);
            // all the member must be constants and they must be compatible with the left
            if (DataType.findCommonDtype(left.getDtype(), member.getDtype()) == null)
                throw new ParseException(format("member datatype '%s' isn't compatible with target datatype '%s'", member.getDtype(), left.getDtype()));
            if (getLowestEvaluationPoint() == null)
                throw new ParseException(getLocation(), format("the left, '%s', and member expressions are not on the same axis", left));
            // the bounds cannot have an evaluation point lower than expression
            if (left.getLowestEvaluationPoint().higher(member.getLowestEvaluationPoint()))
                throw new ParseException(getLocation(),
                        format("the member '%s' requires evaluation below the left '%s' expression", member, left));
        }
        stack.pop();
    }

    @Override
    public DataType getDtype() {
        return DataType.BOOLEAN;
    }

    @Override
    public Path getLowestEvaluationPoint() {
        return Path.lowest(left.getLowestEvaluationPoint(), Path.lowest(members));
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols) {
        if (canReduceToConstant()) {
            return (Expression) this.reduceToConstant().optimize(pathSymbols);
        } else {
            left = (ValueExpression) left.optimize(pathSymbols);
            for (final ListIterator<ValueExpression> i = members.listIterator(); i.hasNext();) {
               final ValueExpression e = (ValueExpression)i.next().optimize(pathSymbols);
               i.set(e);
            }
            return this;
        }
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
        for (ValueExpression member : members) {
            builder.append(member.generateMotif(level + 1));
            builder.append(',');
            builder.append(' ');
        }
        if (!members.isEmpty())
            builder.deleteCharAt(builder.lastIndexOf(","));
        builder.append(')');
        return returnCleanString(builder);
    }

    @Override
    public Constant reduceToConstant() {
        checkCanReduceToConstant();
        Constant leftConstant = left.reduceToConstant();
        for (ValueExpression value : members) {
            Constant memberConstant = value.reduceToConstant();
            switch (memberConstant.getDtype()) {
                case BOOLEAN:
                    if (!leftConstant.isBoolean())
                        throw new ParseException(getLocation(), "");
                    Boolean rightValue = ((BooleanConstant) memberConstant).getBooleanValue();
                    Boolean memberValue = ((BooleanConstant) memberConstant).getBooleanValue();
                    if (op == MembershipTestOperatorType.IN && rightValue == memberValue)
                        return new BooleanConstantContext(getGlobal(), getLocation(), true);
                    if (op == MembershipTestOperatorType.NOT_IN && rightValue == memberValue)
                        return new BooleanConstantContext(getGlobal(), getLocation(), false);
                    break;
                case BYTE:
                case SHORT:
                case INTEGER:
                case LONG:
                case DOUBLE:
                    if (!leftConstant.isNumber())
                        throw new ParseException(getLocation(), "");
                    NumberConstant leftNumberConstant = (NumberConstant) leftConstant;
                    NumberConstant memberNumberConstant = (NumberConstant) memberConstant;
                    boolean match = leftNumberConstant.binaryBooleanCompare(BinaryValueComparisonOperator.EQ, memberNumberConstant).getBooleanValue();
                    if (op == MembershipTestOperatorType.IN && match)
                        return new BooleanConstantContext(getGlobal(), getLocation(), true);
                    if (op == MembershipTestOperatorType.NOT_IN && match)
                        return new BooleanConstantContext(getGlobal(), getLocation(), false);
                    break;
                case STRING:
                    if (!leftConstant.isString())
                        throw new ParseException(getLocation(), "");
                    String rightStringValue = ((StringConstant) memberConstant).getStringValue();
                    String memberStringValue = ((StringConstant) memberConstant).getStringValue();
                    if (op == MembershipTestOperatorType.IN && rightStringValue.equals(memberStringValue))
                        return new BooleanConstantContext(getGlobal(), getLocation(), true);
                    if (op == MembershipTestOperatorType.NOT_IN && rightStringValue.equals(memberStringValue))
                        return new BooleanConstantContext(getGlobal(), getLocation(), false);
                case NULL:
                    throw new ParseException(getLocation(), "NOT IMPLEMENTED");
            }
        }
        return new BooleanConstantContext(getGlobal(), getLocation(), op == MembershipTestOperatorType.NOT_IN);
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(op);
        builder.append('\n');
        builder.append(left.explain(level + 1));
        builder.append(indent(level + 1));
        builder.append("LIST(\n");
        for (ValueExpression node : members) {
            builder.append(node.explain(level + 2));
        }
        builder.append(indent(level + 1));
        builder.append(")\n");
        builder.append(indent(level));
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("op", op)
                .add("lower", left)
                .add("members", members)
                .toString();
    }


    @Override
    public ValueExpression getLeft() {
        return left;
    }

    @Override
    public List<ValueExpression> getMembers() {
        return Collections.unmodifiableList(members);
    }

    @Override
    public MembershipTestOperatorType getOp() {
        return op;
    }

    @Override
    public Boolean canReduceToConstant() {
        for (Expression expression : members)
            if (!expression.canReduceToConstant())
                return false;
        return left.canReduceToConstant();
    }

    // parent interface returns the left and path as children
    @Override
    public List<Expression> getChildren() {
        List<Expression> nw = new ArrayList<>(Collections.singletonList(left));
        nw.addAll(members);
        return nw;
    }

    @Override
    public int childCount() {
        return members.size() + 1;
    }

    @Override
    public Expression getChild(int index) {
        if (index == 0)
            return left;
        else if (index >= 1 && index <= members.size())
            return members.get(index - 1);
        else
            throw new IndexOutOfBoundsException();
    }

    @Override
    public Expression setChild(int index, Expression value) {
        Expression old;
        if (index == 0) {
            old = left;
            left = (ValueExpression)value;
        } else if (index >= 1 && index <= members.size()) {
            old = members.get(index -1);
            members.set(index - 1, (ValueExpression)value);
        } else
            throw new IndexOutOfBoundsException();
        return old;
    }
}
