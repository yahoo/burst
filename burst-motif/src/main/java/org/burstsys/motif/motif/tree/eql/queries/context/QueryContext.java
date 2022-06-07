/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.queries.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.eql.common.Source;
import org.burstsys.motif.motif.tree.eql.common.context.SourcedStatementContext;
import org.burstsys.motif.motif.tree.eql.queries.Query;
import org.burstsys.motif.motif.tree.eql.queries.Select;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.ParameterDefinition;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.symbols.Definition;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class QueryContext extends SourcedStatementContext implements Query {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private List<ParameterDefinition> parameters;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private List<Select> selects;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private BooleanExpression where;

    @JsonProperty
    private Integer limit;

    @SuppressWarnings("unused")
    public QueryContext()  {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.QUERY);
    }

    public QueryContext(
            NodeGlobal global, NodeLocation location,
            List<ParameterDefinition> parameters,
            List<Select> selects,
            List<Source> sources,
            BooleanExpression globalWhere,
            Integer limit) {
        super(global, location, NodeType.QUERY, sources);
        this.parameters = parameters;
        this.selects = selects;
        this.limit = limit;
        this.where = globalWhere;
    }

    @Override
    public List<ParameterDefinition> getParameters() {
        return parameters;
    }

    @Override
    public List<Select> getSelects() {
        return selects;
    }

    @Override
    public BooleanExpression getWhere() {
        return where;
    }

    @Override
    public Integer getLimit() {
        return limit;
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        // bind parameters (must be done before the sources, selects and where clauses so the parameters
        // are added to the parsingSymbols.
        for (ParameterDefinition i: parameters) {
            pathSymbols.addCurrentScopeDefinition(Definition.Context.PARAMETER, i);
        }

        // source bind
        super.bind(pathSymbols, stack);

        // now validate the sources individually after they have all been registered
        stack.push(this);
        getSources().forEach(s -> s.bind(pathSymbols, stack));

        //bind the global where
        if (where != null)
            where.bind(pathSymbols, stack);

        // bind selects
        for (Select i: selects) {
            i.bind(pathSymbols, stack);
        }
        stack.pop();

    }

    @Override
    public Evaluation optimize(PathSymbols pathSymbols) {
        // optimize source
        super.optimize(pathSymbols);
        this.selects = this.selects.stream().map(s -> (Select)s.optimize(pathSymbols)).collect(Collectors.toList());
        if (this.where != null)
            this.where = (BooleanExpression)this.where.optimize(pathSymbols);
        return this;
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {

        stack.push(this);

        // validate the sources
        super.validate(pathSymbols, scope, stack);

        // validate the selects
        for (Select i : selects) {
            i.validate(pathSymbols, scope, stack);
        }

        // validate the global where
        if (where != null)
            where.validate(pathSymbols, scope, stack);

        stack.pop();

        if (limit != null)
            if (limit <= 0)
                throw new ParseException(getLocation(),
                        format("limit %d' is not a positive integer", limit));
    }

    @Override
    public String generateMotif(int level) {
        String parametersString = parameters.stream().map(i -> i.generateMotif(level)).collect(Collectors.joining(","));
        String queriesString = selects.stream().map(i -> i.generateMotif(level)).collect(Collectors.joining(" beside "));
        queriesString = queriesString.replace("select", "select " + parametersString);
        String sourcesString = getSources().stream().map(i -> i.generateMotif(level)).collect(Collectors.joining(", "));
        String limitString = limit != null ? " limit " + limit : "";
        return queriesString + " from " + sourcesString + limitString;
    }
}
