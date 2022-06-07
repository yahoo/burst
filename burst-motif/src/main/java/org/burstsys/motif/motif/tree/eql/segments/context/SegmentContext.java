/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.segments.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.eql.common.Source;
import org.burstsys.motif.motif.tree.eql.common.context.SourcedStatementContext;
import org.burstsys.motif.motif.tree.eql.segments.Segment;
import org.burstsys.motif.motif.tree.eql.segments.SegmentDefinition;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.ParameterDefinition;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.symbols.Definition;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class SegmentContext extends SourcedStatementContext implements Segment {

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    List<ParameterDefinition> parameters;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private List<SegmentDefinition> definitions;

    @SuppressWarnings("unused")
    public SegmentContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.SEGMENT);
    }

    public SegmentContext(
            NodeGlobal global, NodeLocation location, String name,
            List<ParameterDefinition> parameters,
            List<Source> sources,
            List<SegmentDefinition> definitions) {
        super(global, location, NodeType.SEGMENT, sources);
        this.name = name;
        this.definitions = definitions;
        this.parameters = parameters;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<ParameterDefinition> getParameters() { return parameters; }

    @Override
    public DataType getDtype(UsageContext context) {
        return DataType.NULL;
    }

    @Override
    public List<SegmentDefinition> getDefinitions() {
        return definitions;
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        // bind parameters (must be done before the definitions so the parameters
        // are added to the parsingSymbols.
        for (ParameterDefinition i: parameters) {
            pathSymbols.addCurrentScopeDefinition(Definition.Context.PARAMETER, i);
        }

        stack.push(this);

        // bind sources
        super.bind(pathSymbols, stack);

        //
        for (SegmentDefinition i: definitions) {
            i.bind(pathSymbols, stack);
        }

        stack.pop();

        // add the funnel definition to the parent
        pathSymbols.addParentScopeDefinition(Context.SOURCE, this);

    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        stack.push(this);

        // validate sources
        super.validate(pathSymbols, scope, stack);

        for (SegmentDefinition i: definitions) {
            i.validate(pathSymbols, scope, stack);
        }

        stack.pop();
    }

    @Override
    public String generateMotif(int level) {
        String definitionString = definitions.stream().map(i -> i.generateMotif(level+1)).collect(Collectors.joining("\n"));
        return "segment " + name + "{\n" + definitionString + "\n} from " + getSchema();
    }
}
