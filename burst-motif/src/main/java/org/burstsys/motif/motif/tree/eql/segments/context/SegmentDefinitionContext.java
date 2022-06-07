/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.segments.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.eql.segments.SegmentDefinition;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.motif.tree.expression.context.EvaluationContext;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;
import org.burstsys.motif.paths.Path;

import java.util.Stack;

import static java.lang.String.format;

public class SegmentDefinitionContext extends EvaluationContext implements SegmentDefinition {

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Long name;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private BooleanExpression where;

    @SuppressWarnings("unused")
    public SegmentDefinitionContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.SEGMENT_DEFINITION);
    }

    public SegmentDefinitionContext(
            NodeGlobal global, NodeLocation location,
            String name,
            BooleanExpression where) {
        super(global, location, NodeType.SEGMENT_DEFINITION);
        try {
            this.name = Long.parseLong(name);
        } catch (NumberFormatException nfe){
            throw new ParseException(getLocation(), format("segment identifier '%s' must be a long", name));
        }
        this.where = where;
    }

    @Override
    public Long getName() {
        return name;
    }

    @Override
    public BooleanExpression getWhere() {
        return where;
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        // bind where
        if (where != null) {
            stack.push(this);
            where.bind(pathSymbols, stack);
            stack.pop();
        }
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        // validate name as integer

        // validate where
        if (where != null) {
            stack.push(this);
            where.validate(pathSymbols, scope, stack);
            stack.pop();
        }
    }

    @Override
    public String generateMotif(int level) {
        return "segment " + name + " when " + where.generateMotif(level);
    }
}
