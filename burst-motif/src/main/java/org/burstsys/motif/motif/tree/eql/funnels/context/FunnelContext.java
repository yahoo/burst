/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.funnels.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.motif.tree.constant.context.LongConstantContext;
import org.burstsys.motif.motif.tree.eql.common.Source;
import org.burstsys.motif.motif.tree.eql.common.context.SourcedStatementContext;
import org.burstsys.motif.motif.tree.eql.funnels.*;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.ParameterDefinition;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.symbols.Definition;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class FunnelContext extends SourcedStatementContext implements Funnel {

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Type type;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    List<ParameterDefinition> parameters;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private Map<Long, StepDefinition> steps;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private FunnelMatchDefinition definition;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ValueExpression within;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Integer limit;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    ArrayList<String> tags;

    @SuppressWarnings("unused")
    public FunnelContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.FUNNEL);
    }

    public FunnelContext(
            NodeGlobal global, NodeLocation location, String name, Type type,
            List<ParameterDefinition> parameters,
            List<Source> sources,
            List<StepDefinition> steps,
            FunnelMatchDefinition definition,
            ValueExpression within,
            Integer limit,
            ArrayList<String> tags
            ) {
        super(global, location, NodeType.FUNNEL, sources);
        this.name = name;
        this.type = type;
        this.parameters = parameters;
        this.definition = definition;
        this.tags = tags;
        AtomicLong gId = new AtomicLong(0);
        this.steps = steps.stream().collect(Collectors.toMap(s -> {
            if (s instanceof TriggeredStepDefinition)
                return s.getId();
            else
                return gId.decrementAndGet();
        }, s -> s));
        this.within = within == null ? new LongConstantContext(global, location, 0L): within;
        this.limit = limit;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public List<ParameterDefinition> getParameters() {
        return parameters;
    }

    @Override
    public Collection<StepDefinition> getSteps() {
        return steps.values();
    }

    @Override
    public StepDefinition getStep(Long id) {
        return steps.get(id);
    }

    @Override
    public FunnelMatchDefinition getDefinition() {
        return definition;
    }

    @Override
    public ValueExpression getWithin() {
        return this.within;
    }

    public long getWithinValue() {
        return ((Constant)within).asLong();
    }

    @Override
    public Integer getLimit() {
        return this.limit;
    }

    @Override
    public ArrayList<String> getTags() {
        return this.tags;
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        // bind parameters (must be done before the steps so the parameters
        // are added to the parsingSymbols.
        for (ParameterDefinition i: parameters) {
            pathSymbols.addCurrentScopeDefinition(Definition.Context.PARAMETER, i);
        }

        // set the schema scope for the statement
        stack.push(this);
        // check sources
        super.bind(pathSymbols, stack);

        for (StepDefinition s: steps.values()) {
            s.bind(pathSymbols, stack);
        }

        getWithin().bind(pathSymbols, stack);
        stack.pop();

        // add the funnel definition to the parent
        pathSymbols.addParentScopeDefinition(Context.SOURCE, this);
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        stack.push(this);
        // check the sources
        super.validate(pathSymbols, scope, stack);

        // check the steps
        for (StepDefinition s: steps.values()) {
            s.validate(pathSymbols, scope, stack);
        }

        // validate the definition with the steps
        definition.validate(steps);
        // a definition cannot be all non- capture
        if (!definition.isCapture())
            throw new ParseException(getLocation(), "A funnel cannot have an all non capturing definition");
        else if (definition instanceof FunnelMatchDefinitionList) {
            if (!((FunnelMatchDefinitionList)definition).isStartCapture())
                throw new ParseException(getLocation(), "A funnel definition cannot start with a non capturing group");
            if (!((FunnelMatchDefinitionList)definition).isEndCapture())
                throw new ParseException(getLocation(), "A funnel definition cannot end with a non capturing group");

        }

        if (getWithin() != null && !getWithin().canReduceToConstant())
            throw new ParseException(getLocation(), format("the within expression '%s' must reduce to a constant", getWithin()));
        else
            within = within.reduceToConstant();

        if (limit != null) {
            if (limit <= 0) {
                throw new ParseException(getLocation(),
                        format("limit %d' is not a positive integer", limit));
            }
        }


        stack.pop();
    }

    @Override
    public String generateMotif(int level) {
        String stepString = steps.values().stream().map(i -> i.generateMotif(level+1)).collect(Collectors.joining("\n"));
        StringBuilder sb = new StringBuilder("funnel ");
        sb.append(name);
        sb.append(" ").append(type.name());
        if (getWithinValue() > 0)
            sb.append(" within ").append(getWithin());
        if(getLimit() > 0)
            sb.append(" limit ").append(getLimit());
        sb.append(" {\n");
        sb.append(stepString);
        sb.append("\n").append(definition.generateMotif());
        sb.append("\n} from ").append(getSchema());
        return sb.toString();
    }

    @Override
    public Evaluation optimize(PathSymbols pathSymbols) {
        // optimize sources
        super.optimize(pathSymbols);

        this.steps.replaceAll((k, v) -> (StepDefinition)v.optimize(pathSymbols));
        return this;
    }
}
