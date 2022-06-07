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
import org.burstsys.motif.motif.tree.rule.PresampleFilterRule;
import org.burstsys.motif.motif.tree.values.ValueExpression;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public final class PresampleFilterRuleContext extends FilterRuleContext implements PresampleFilterRule {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private ValueExpression sampleRatio;

    public PresampleFilterRuleContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.PRESAMPLE_RULE);
    }

    public PresampleFilterRuleContext(NodeGlobal global, NodeLocation location, PathAccessorContext targetPath,
                                   BooleanExpression whereExpression, ValueExpression sampleRatio) {
        super(global, location, NodeType.PRESAMPLE_RULE, targetPath, whereExpression, FilterRuleType.PRESAMPLE);
        this.sampleRatio = sampleRatio;
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols) {
        sampleRatio = (ValueExpression) sampleRatio.optimize(pathSymbols);
        return super.optimize(pathSymbols);
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        super.bind(pathSymbols, stack);
        stack.push(this);
        sampleRatio.bind(pathSymbols, stack);
        stack.pop();
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        super.validate(pathSymbols, scope, stack);
        stack.push(this);
        sampleRatio.validate(pathSymbols, scope, stack);
        stack.pop();
        if (!sampleRatio.canReduceToConstant() || sampleRatio.getDtype().notNumeric())
            throw new ParseException(getLocation(),
                    format("Presample ratio '%s' must evaluate to a numeric constant", sampleRatio));
        if (getSampleRatio() > 1.0 || getSampleRatio() < 0.0)
            throw new ParseException(getLocation(),
                    format("Presample ratio '%s' must evaluate to a numeric constant <= 1.0 and >= 0.0", sampleRatio));
    }

    @Override
    public String generateMotif(int level) {
        String builder = "PRESAMPLE" +
                '(' +
                sampleRatio.generateMotif(level) +
                ')';
        return builder;
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append(type);
        builder.append('\n');
        builder.append(sampleRatio.explain(level + 1));
        builder.append(indent(level));
        return endExplain(builder);
    }

    @Override
    public double getSampleRatio() {
        return sampleRatio.reduceToConstant().asDouble();
    }

    // parent interface returns the target, where, sample ratio as children
    @Override
    public List<Expression> getChildren() {
        return Arrays.asList(target, where, sampleRatio);
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
            case 2: return sampleRatio;
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
                old = sampleRatio;
                sampleRatio = (ValueExpression)value;
                return old;
            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
