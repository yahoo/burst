/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.queries.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.eql.queries.Select;
import org.burstsys.motif.motif.tree.eql.queries.Target;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.symbols.Definition;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.motif.tree.expression.context.EvaluationContext;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;
import org.burstsys.motif.paths.Path;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class SelectContext extends EvaluationContext implements Select {
    @JsonProperty
    private String name;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private List<Target> targets;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private BooleanExpression where;

    @JsonProperty
    private Integer limit;

    private PathSymbols.Scope symbolScope;

    @SuppressWarnings("unused")
    public SelectContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.SELECT);
    }

    public SelectContext(
            NodeGlobal global,
            NodeLocation location,
            String name,
            List<Target> targets,
            BooleanExpression where,
            Integer limit) {
        super(global, location, NodeType.SELECT);
        this.name = name;
        this.targets = targets;
        this.where = where;
        this.limit = limit;
    }

    @Override
    public String getName() { return name; }

    @Override
    public BooleanExpression getWhere() {
        return where;
    }

    @Override
    public List<Target> getTargets() {
        return targets;
    }

    @Override
    public Integer getLimit() {
        return limit;
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        // push a scope context for any definitions in this select
        pathSymbols.pushScope();
        // add the definition first
        for (Target t: targets) {
            pathSymbols.addCurrentScopeDefinition(Definition.Context.TARGET, t);
        }
        // then validate it
        stack.push(this);
        for (Target t: targets) {
            t.bind(pathSymbols, stack);
        }

        if (where != null)
            where.bind(pathSymbols, stack);
        stack.pop();

        // save away the scope for the later validate
        symbolScope = pathSymbols.popScope();
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        // restore the symbols scope for validation
        pathSymbols.restoreScope(symbolScope);

        stack.push(this);
        for (Target t: targets) {
            t.validate(pathSymbols, scope, stack);
        }
        if (where != null)
            where.validate(pathSymbols, scope, stack);

        if (limit != null && limit <= 0)
            throw new ParseException(getLocation(),
                    format("limit %d' is not a positive integer", limit));
        stack.pop();

        // remove our scope
        symbolScope = pathSymbols.popScope();
    }

    @Override
    public String generateMotif(int level) {
        String targetString = targets.stream().map(i -> i.generateMotif(level)).collect(Collectors.joining(", "));
        String limitString = limit != null ? " limit " + limit.toString() : "";
        String whereString = where != null ? " where " + where.generateMotif(level) : "";
        return "select " + targetString + whereString + limitString;
    }

    @Override
    public Evaluation optimize(PathSymbols pathSymbols) {
        // restore the symbols scope for validation
        pathSymbols.restoreScope(symbolScope);
        this.targets = this.targets.stream().map(s -> (Target)s.optimize(pathSymbols)).collect(Collectors.toList());
        if (this.where != null)
            this.where = (BooleanExpression)this.where.optimize(pathSymbols);
        symbolScope = pathSymbols.popScope();
        return this;
    }
}
