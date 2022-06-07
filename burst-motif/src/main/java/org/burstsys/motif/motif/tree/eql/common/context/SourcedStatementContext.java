/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.common.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.eql.common.Source;
import org.burstsys.motif.motif.tree.eql.common.SourcedStatement;
import org.burstsys.motif.motif.tree.eql.funnels.Funnel;
import org.burstsys.motif.motif.tree.eql.funnels.context.FunnelSourceContext;
import org.burstsys.motif.motif.tree.eql.segments.Segment;
import org.burstsys.motif.motif.tree.eql.segments.context.SegmentSourceContext;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.schema.model.MotifSchema;
import org.burstsys.motif.symbols.Definition;
import org.burstsys.motif.symbols.PathSymbols;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import static java.lang.String.format;

public abstract class SourcedStatementContext extends StatementContext implements SourcedStatement {

    public SourcedStatementContext(NodeGlobal global, NodeLocation location, NodeType ntype, List<Source> sources) {
        super(global, location, ntype);
        this.sources = sources;
    }

    public SourcedStatementContext(NodeGlobal global, NodeType ntype) {
        super(global, ntype);
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private List<Source> sources;

    @Override
    public String getSchemaName() {
        Source source = getSchemaSource();
        if (source == null)
            return null;
        else
            return source.getDeclaredName();
    }

    @Override
    public MotifSchema getSchema() {
        return pathSymbols.lookupSchema(getSchemaName());
    }

    protected Source getSchemaSource() {
        List<SchemaSourceContext> sources =
                getSources().stream().filter(s -> s instanceof SchemaSourceContext).map(s -> (SchemaSourceContext) s).collect(Collectors.toList());
        if (sources.size() != 1)
            return null;
        else
            return sources.get(0);
    }

    @Override
    public List<Source> getSources() {
        return sources;
    }

    @OverridingMethodsMustInvokeSuper
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        super.bind(pathSymbols, stack);

        // bind the sources to the symbols
        List<SchemaSourceContext> schemaSources = getSources().stream().filter(s -> s instanceof SchemaSourceContext).
                map(s -> (SchemaSourceContext) s).collect(Collectors.toCollection(ArrayList::new));

        if (schemaSources.size() == 1) {
            // check the schema exists (we did it already in bind perhaps, but make sure)
            if (pathSymbols.lookupSchema(getSchemaName()) == null)
                throw new ParseException(getLocation(), format("schema %s not found", getSchemaName()));
        } else {
            throw new ParseException(getLocation(), "query needs exactly one schema source");
        }

        // set the schema scope for the statement
        Source schemaSource = getSchemaSource();
        // we have to validate the schema here since we need it for path binding
        if (schemaSource == null)
            throw new ParseException(getLocation(), "no schema source found");
        MotifSchema schem = pathSymbols.lookupSchema(schemaSource.getDeclaredName());
        if (schem == null)
            throw new ParseException(getLocation(), format("schema %s not found", schemaSource.getName()));
        pathSymbols.setCurrentRootPath(schemaSource.getDeclaredName());

        // for all sources including the schema source,  bind the root name and alias name to the symbol table
        sources.forEach(s -> {
            s.register(pathSymbols);
            if (!s.getDeclaredName().equals(s.getName())) {
                Definition prev = pathSymbols.getDefinition(Definition.Context.SOURCE, s.getName());
                Definition decl = pathSymbols.getDefinition(Definition.Context.SOURCE, s.getDeclaredName());
                if (prev != decl) {
                    if (prev != null)
                        throw new ParseException(getLocation(), format("name %s is already defined for source %s", s.getName(), prev.getName()));
                    pathSymbols.addAlias(Definition.Context.SOURCE, s.getName(), pathSymbols.getDefinition(Definition.Context.SOURCE, s.getDeclaredName()));
                }
            }
        });
    }

    @Override
    public Evaluation optimize(PathSymbols pathSymbols) {
        this.sources = this.sources.stream().map(s -> (Source) s.optimize(pathSymbols)).collect(Collectors.toList());
        return this;
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        assert (pathSymbols == this.pathSymbols);

        // source schema
        String sName = this.getSchemaName();

        // validate the sources individually
        getSources().forEach(s -> s.validate(pathSymbols, scope, stack));

        getSources().stream().filter(s -> s instanceof FunnelSourceContext).forEach(s -> {
            // is the funnel defined
            // is the funnel schema source on the same schema as our schema source
            // check only one transaction funnel
            Funnel fun = (Funnel) pathSymbols.getDefinition(Definition.Context.SOURCE, s.getDeclaredName());
            if (fun == null)
                throw new ParseException(getLocation(), format("funnel %s not found", s.getName()));
            else if (!fun.getSchemaName().equals(sName))
                throw new ParseException(getLocation(), format("funnel schema %s does not match query schema %s", s.getName(), sName));
        });

        getSources().stream().filter(s -> s instanceof SegmentSourceContext).forEach(s -> {
            // is the segment defined
            // eventually check segment validity
            // is the segment schema source on the same schema as our schema source
            Segment seg = (Segment) pathSymbols.getDefinition(Definition.Context.SOURCE, s.getName());
            if (seg == null)
                throw new ParseException(getLocation(), format("segment %s not found", s.getName()));
            else if (!seg.getSchemaName().equals(sName))
                throw new ParseException(getLocation(), format("segment schema %s does not match query schema %s", s.getName(), sName));
        });

    }
}
