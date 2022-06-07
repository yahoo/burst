/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.funnels.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.JsonSerde;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.eql.funnels.StepDefinition;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.context.EvaluationContext;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.Stack;

abstract
public class StepDefinitionContext extends EvaluationContext implements StepDefinition {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private BooleanExpression when;

    protected StepDefinitionContext(NodeGlobal global, NodeType type) {
        super(global, type);
    }

    public StepDefinitionContext(
            NodeGlobal global, NodeLocation location, NodeType type,
            BooleanExpression when) {
        super(global, location, type);
        this.when = when;
    }

    @Override
    public BooleanExpression getWhen() {
        return when;
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        stack.push(this);
        when.bind(pathSymbols, stack);
        stack.pop();
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        // validate
        stack.push(this);
        when.validate(pathSymbols, scope, stack);
        stack.pop();
    }

    @Override
    public String generateMotif(int level) {
        return "when " + when.generateMotif(level);
    }
}
