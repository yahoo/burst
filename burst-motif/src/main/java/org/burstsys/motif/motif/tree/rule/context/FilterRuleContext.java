/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.rule.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.motif.tree.data.PathAccessor;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;
import org.burstsys.motif.motif.tree.rule.FilterRule;
import org.burstsys.motif.motif.tree.rule.FilterRuleType;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.paths.schemas.RelationType;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static java.lang.String.format;

public abstract class FilterRuleContext extends ExpressionContext implements FilterRule {

    @JsonProperty
    protected FilterRuleType type;

    PathAccessor target;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    protected BooleanExpression where;

    FilterRuleContext(NodeGlobal global, NodeLocation location, NodeType ntype, PathAccessor target,
                      BooleanExpression where, FilterRuleType type) {
        super(global, location, ntype);
        this.target = target;
        this.where = where;
        this.type = type;
    }

    FilterRuleContext(NodeGlobal global, NodeType ntype) {
        super(global, ntype);
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        stack.push(this);
        target.validate(pathSymbols, scope, stack);
        where.validate(pathSymbols, scope, stack);
        if (target.getBinding().getRelationType() != RelationType.INSTANCE) {
            throw new ParseException(getLocation(), format("target path '%s' must be an instance reference", target.fullPathWithKeyAsString()));
        }
        // validate the where clause
        if (where != null) {
            where.validate(pathSymbols, target.getLowestEvaluationPoint(), stack);
            if (!where.getDtype().equals(DataType.BOOLEAN))
                throw new ParseException(getLocation(),
                        format("%s '%s' filter clause where expression '%s' must be a boolean", type, target, where));
        }
        // must be compatible
        if  (Path.lowest(target.getLowestEvaluationPoint(), where.getLowestEvaluationPoint()) == null) {
            throw new ParseException(getLocation(), format("the target, '%s', and where, '%s', expressions are not on the same axis", target, where));
        }
        stack.pop();
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        stack.push(this);
        target.bind(pathSymbols, stack);
        where.bind(pathSymbols, stack);
        stack.pop();
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols) {
        // this cannot be reduced to a constant so don't bother checking
        // we guarantee that will will not make a new object.
        where = (BooleanExpression) where.optimize(pathSymbols);
        return this;
    }


    @Override
    public DataType getDtype() {
        return target.getDtype();
    }

    @Override
    public Path getLowestEvaluationPoint() {
        return target.getLowestEvaluationPoint();
    }

    @Override
    public final Boolean canReduceToConstant() {
        return false;
    }

    @Override
    public PathAccessor getTarget() {
        return target;
    }

    @Override
    public BooleanExpression getWhere() {
        return where;
    }

    @Override
    public FilterRuleType getType() {
        return type;
    }

    // parent interface returns the target and where as children
    @Override
    public List<Expression> getChildren() {
        return Arrays.asList(target, where);
    }

    @Override
    public int childCount() {
        return 2;
    }

    @Override
    public Expression getChild(int index) {
        switch (index) {
            case 0: return target;
            case 1: return where;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public Expression setChild(int index, Expression value) {
        Expression old;
        switch (index) {
            case 0:
                old = target;
                target = (PathAccessor) value;
                return old;
            case 1:
                old = where;
                where = (BooleanExpression)value;
                return old;
            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
