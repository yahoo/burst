/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.JsonSerde;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.constant.context.BooleanConstantContext;
import org.burstsys.motif.motif.tree.data.PathAccessor;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.motif.tree.expression.context.EvaluationContext;
import org.burstsys.motif.motif.tree.logical.BinaryBooleanOperatorType;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;
import org.burstsys.motif.motif.tree.logical.UnaryBooleanOperatorType;
import org.burstsys.motif.motif.tree.logical.context.BinaryBooleanExpressionContext;
import org.burstsys.motif.motif.tree.logical.context.UnaryBooleanExpressionContext;
import org.burstsys.motif.motif.tree.rule.FilterRule;
import org.burstsys.motif.motif.tree.rule.FilterRuleType;
import org.burstsys.motif.motif.tree.rule.context.EditFilterRuleContext;
import org.burstsys.motif.motif.tree.rule.context.FilterRuleContext;
import org.burstsys.motif.paths.Path;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ViewContext extends EvaluationContext implements View {

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private Map<String, FilterRule> rules;

    public ViewContext() {
        super(null, NodeType.VIEW);
    }

    public ViewContext(NodeGlobal global, NodeLocation location, String name, ArrayList<FilterRule> rules) {
        super(global, location, NodeType.VIEW);
        this.rules = rules.stream().collect(Collectors.toMap(ru -> ru.getTarget().fullPathAsString(), Function.identity()));
        this.name = name;
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append('\n');
        for (FilterRule rule : rules.values()) {
            builder.append(rule.explain(level + 1));
        }
        builder.append(indent(level));
        return endExplain(builder);
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        stack.push(this);
        for (FilterRule rule : rules.values()) {
            rule.bind(pathSymbols, stack);
        }
        stack.pop();
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        stack.push(this);
        for (FilterRule rule : rules.values()) {
            rule.validate(pathSymbols, scope, stack);
        }
        stack.pop();
    }

    @Override
    public FilterRule[] getRules() {
        return rules.values().toArray(new FilterRule[0]);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public View unionView(View view) {
        ViewContext viewc = (ViewContext) view;

        List<String> allPaths = Stream.concat(this.rules.keySet().stream(), viewc.rules.keySet().stream()).distinct().collect(Collectors.toList());

        ViewContext nv = new ViewContext();
        nv.name = this.getName() + "⋃" + view.getName();
        nv.rules = new HashMap<>();

        for (String p: allPaths) {
            FilterRule l = this.rules.getOrDefault(p, null);
            FilterRule r = viewc.rules.getOrDefault(p, null);
            assert(l != null || r != null);
            PathAccessor target = l != null ? l.getTarget() : r.getTarget();

            BooleanExpression unionedPredicate;
            if (l == null)
                unionedPredicate = r.getWhere();
            else if (r == null)
                unionedPredicate = l.getWhere();
            else {
                if (l.getWhere().canReduceToConstant())
                    if (l.getWhere().reduceToConstant().asBoolean())
                        // this one will drag in this level no matter what the right says
                        unionedPredicate = l.getWhere();
                    else
                        // this one will always defer to the right
                        unionedPredicate = r.getWhere();
                else if (r.getWhere().canReduceToConstant())
                    if (r.getWhere().reduceToConstant().asBoolean())
                        // this one will drag in this level no matter what
                        unionedPredicate = r.getWhere();
                    else
                        // this one will always defer to the left
                        unionedPredicate = l.getWhere();
                else
                    unionedPredicate = new BinaryBooleanExpressionContext(getGlobal(), getLocation(), BinaryBooleanOperatorType.OR,
                            l.getWhere(), r.getWhere());
            }


            nv.rules.put(target.fullPathAsString(),
                    new EditFilterRuleContext(getGlobal(), getLocation(), target, unionedPredicate, FilterRuleType.INCLUDE));
        }
        return nv;
    }

    @Override
    public View intersectView(View view) {
        ViewContext viewc = (ViewContext) view;

        List<String> allPaths = Stream.concat(this.rules.keySet().stream(), viewc.rules.keySet().stream()).distinct().collect(Collectors.toList());

        ViewContext nv = new ViewContext();
        nv.name = this.getName() + "⋂" + view.getName();
        nv.rules = new HashMap<>();

        for (String p: allPaths) {
            FilterRule l = this.rules.getOrDefault(p, null);
            FilterRule r = viewc.rules.getOrDefault(p, null);
            assert(l != null || r != null);
            PathAccessor target = l != null ? l.getTarget() : r.getTarget();

            BooleanExpression intersectedPredicate;
            if (l == null)
                intersectedPredicate = r.getWhere();
            else if (r == null)
                intersectedPredicate = l.getWhere();
            else {
                if (l.getWhere().canReduceToConstant())
                    if (l.getWhere().reduceToConstant().asBoolean())
                        // this one will always defer to the right
                        intersectedPredicate = r.getWhere();
                    else
                        // this one will exclude this level no matter what
                        intersectedPredicate = l.getWhere();
                else if (r.getWhere().canReduceToConstant())
                    if (r.getWhere().reduceToConstant().asBoolean())
                        // this one will always defer to the left
                        intersectedPredicate = l.getWhere();
                    else
                        // this one will exclude this level no matter what
                        intersectedPredicate = r.getWhere();
                else
                    intersectedPredicate = new BinaryBooleanExpressionContext(getGlobal(), getLocation(), BinaryBooleanOperatorType.AND,
                            l.getWhere(), r.getWhere());
            }


            nv.rules.put(target.fullPathAsString(),
                    new EditFilterRuleContext(getGlobal(), getLocation(), target, intersectedPredicate, FilterRuleType.INCLUDE));
        }
        return nv;
    }

    @Override
    public View complementView() {
        List<String> allPaths = new ArrayList<>(this.rules.keySet());

        ViewContext nv = new ViewContext();
        nv.name = "¬" + this.getName();
        nv.rules = new HashMap<>();

        for (String p: allPaths) {
            FilterRule l = this.rules.getOrDefault(p, null);
            assert(l != null);
            PathAccessor target = l.getTarget();

            BooleanExpression complimentPredicate;
            if (l.getTarget().getLowestEvaluationPoint().isRoot()) {
                if (l.getWhere() == null ) {
                    // there is no restriction or the restriction evaluates to always true,  the compliment is a bit lame
                    complimentPredicate = new BooleanConstantContext(getGlobal(), getLocation(), false);
                } else if (l.getWhere().canReduceToConstant()) {
                    if (l.getWhere().reduceToConstant().asBoolean())
                        complimentPredicate = new BooleanConstantContext(getGlobal(), getLocation(), false);
                    else
                        complimentPredicate = new BooleanConstantContext(getGlobal(), getLocation(), true);
                } else {
                    complimentPredicate = new UnaryBooleanExpressionContext(getGlobal(), getLocation(), UnaryBooleanOperatorType.NOT, l.getWhere());
                }
            } else {
                // don't compliment the lower ones, just copy them
                complimentPredicate = l.getWhere();
            }

            nv.rules.put(target.fullPathAsString(),
                    new EditFilterRuleContext(getGlobal(), getLocation(), target, complimentPredicate, FilterRuleType.INCLUDE));
        }
        return nv;
    }

    private BooleanExpression compactPredicates(Predicate<FilterRule> predicate) {
        Map<FilterRuleType, List<FilterRule>> rules = Arrays.stream(getRules()).
                filter(predicate).
                collect(Collectors.groupingBy(FilterRule::getType));
        BooleanExpression includeWhere = rules.get(FilterRuleType.INCLUDE) != null ? (rules.get(FilterRuleType.INCLUDE).stream().map(FilterRule::getWhere).reduce(
                (left, right) ->
                        new BinaryBooleanExpressionContext(getGlobal(), getLocation(), BinaryBooleanOperatorType.OR, left, right)
        ).orElse(new BooleanConstantContext(getGlobal(), getLocation(), true))) : null;
        BooleanExpression excludeWhere = rules.get(FilterRuleType.EXCLUDE) != null ? (rules.get(FilterRuleType.EXCLUDE).stream().map(
                item -> (BooleanExpression)new UnaryBooleanExpressionContext(getGlobal(), getLocation(), UnaryBooleanOperatorType.NOT, item.getWhere())).reduce(
                (left, right) ->
                        new BinaryBooleanExpressionContext(getGlobal(), getLocation(), BinaryBooleanOperatorType.AND, left, right)
        ).orElse(new BooleanConstantContext(getGlobal(), getLocation(), true))) : null;
        if (includeWhere != null && excludeWhere != null)
            return new BinaryBooleanExpressionContext(getGlobal(), getLocation(), BinaryBooleanOperatorType.AND, includeWhere, excludeWhere);
        else if (includeWhere != null)
            return includeWhere;
        else if (excludeWhere != null)
            return excludeWhere;
        else
            return new BooleanConstantContext(getGlobal(), getLocation(), true);
    }

    public BooleanExpression rootFilterPredicate() {
        return compactPredicates(item -> item.getTarget().getLowestEvaluationPoint().isRoot());
    }

    @Override
    public String defaultTimeZoneName() {
        assert getGlobal() != null;
        return getGlobal().defaultTimeZoneName();
    }

    @Override
    public String generateMotif(int level) {
        StringBuilder builder = new StringBuilder();
        builder.append("VIEW \"");
        builder.append(name);
        builder.append("\" {\n");
        for (FilterRule rule : rules.values()) {
            builder.append(rule.generateMotif(level + 1));
            builder.append(";\n");
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public Evaluation optimize(PathSymbols pathSymbols) {
        for (Map.Entry<String, FilterRule> entry : rules.entrySet()) {
            entry.setValue((FilterRule) ((FilterRuleContext) entry.getValue()).optimize(pathSymbols));
        }
        return this;
    }

}
