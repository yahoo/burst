/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.rule.context;

import static java.lang.String.format;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.data.PathAccessor;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.motif.tree.data.context.PathAccessorContext;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;
import org.burstsys.motif.motif.tree.rule.FilterRuleType;
import org.burstsys.motif.motif.tree.rule.PostsampleFilterRule;
import org.burstsys.motif.motif.tree.values.ValueExpression;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public final class PostsampleFilterRuleContext extends FilterRuleContext implements PostsampleFilterRule {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private ValueExpression maxByteCount;

    public PostsampleFilterRuleContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.POSTSAMPLE_RULE);

    }

    public PostsampleFilterRuleContext(NodeGlobal global, NodeLocation location, PathAccessorContext targetPath,
                                   BooleanExpression whereExpression, ValueExpression maxByteCount) {
        super(global, location, NodeType.POSTSAMPLE_RULE, targetPath, whereExpression, FilterRuleType.POSTSAMPLE);
        this.maxByteCount = maxByteCount;
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols) {
    	maxByteCount = (ValueExpression) maxByteCount.optimize(pathSymbols);
        return super.optimize(pathSymbols);
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        super.bind(pathSymbols, stack);
        stack.push(this);
        maxByteCount.bind(pathSymbols, stack);
        stack.pop();
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        super.validate(pathSymbols, scope, stack);
        stack.push(this);
        maxByteCount.validate(pathSymbols, scope, stack);
        stack.pop();
        if (!maxByteCount.canReduceToConstant() || maxByteCount.getDtype().notNumeric())
            throw new ParseException(getLocation(),
                    format("Postsample max byte count '%s' must evaluate to a numeric constant", maxByteCount));
        if (getMaxByteCount() < 0L)
            throw new ParseException(getLocation(),
                    format("Postsample max byte count '%s' must evaluate to a numeric constant >= 0", maxByteCount));
    }

    @Override
    public String generateMotif(int level) {
        String builder = "POSTSAMPLE" +
                '(' +
                maxByteCount.generateMotif(level) +
                ')';
        return builder;
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(type);
        builder.append('\n');
        builder.append(maxByteCount.explain(level + 1));
        builder.append(indent(level));
        return endExplain(builder);
    }

    @Override
    public long getMaxByteCount() {
        return maxByteCount.reduceToConstant().asLong();
    }

    // parent interface returns the target, where, bytecount as children
    @Override
    public List<Expression> getChildren() {
        return Arrays.asList(target, where, maxByteCount);
    }

    @Override
    public int childCount() {
        return 3;
    }

    @Override
    public Expression getChild(int index) {
        switch (index) {
            case 0: return target;
            case 1: return where;
            case 2: return maxByteCount;
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
            case 2:
                old = maxByteCount;
                maxByteCount = (ValueExpression)value;
                return old;
            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
