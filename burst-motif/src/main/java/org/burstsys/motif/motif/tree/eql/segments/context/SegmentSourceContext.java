/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.segments.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.data.ParameterAccessor;
import org.burstsys.motif.motif.tree.eql.common.context.BaseSourceContext;
import org.burstsys.motif.motif.tree.eql.segments.Segment;
import org.burstsys.motif.motif.tree.eql.segments.SegmentSource;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.symbols.Definition;
import org.burstsys.motif.symbols.PathSymbols;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.stream.IntStream;

import static java.lang.String.format;

public class SegmentSourceContext extends BaseSourceContext implements SegmentSource, Definition.UsageContext {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    @Nonnull
    List<ValueExpression> parameters = Collections.emptyList();

    public SegmentSourceContext() {
        super(NodeType.SEGMENT_SOURCE);
    }

    public SegmentSourceContext(NodeGlobal global, NodeLocation location, String name, String alias,
                                List<ValueExpression> params) {
        super(global, location, NodeType.SEGMENT_SOURCE, name, alias);
        if (params != null)
            this.parameters = params;
    }

    @Override
    public List<ValueExpression> getParms() {
        return parameters;
    }

    @Override
    public Definition register(PathSymbols pathSymbols) {
        Definition def = super.register(pathSymbols);
        if (!(def instanceof Segment))
            throw new ParseException(getLocation(), format("definition for %s is not a segment", getDeclaredName()));
        return def;
    }


    public String generateMotif(int level) {
        return "segment " + super.generateMotif(level);
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        super.bind(pathSymbols, stack);
        Segment segment = (Segment)pathSymbols.getDefinition(Definition.Context.SOURCE, getDeclaredName());
        if (segment.getParameters().size() != parameters.size())
            throw new ParseException(this.getLocation(), "argument count mismatch to segment " + this.getName());

        IntStream.range(0, parameters.size()).forEach(i -> {
            if (!(parameters.get(i) instanceof ParameterAccessor) && !parameters.get(i).canReduceToConstant())
                throw new ParseException(this.getLocation(), "arguments to segment " + this.getName() + " must be parameters or constants");
            parameters.get(i).bind(pathSymbols, null);

            if (!DataType.hasCommonDtype(parameters.get(i).getDtype(), segment.getParameters().get(i).getDtype(this)))
                throw new ParseException(this.getLocation(), "argument type mismatch for " +
                        segment.getParameters().get(i).getName() + " in segment " + this.getName());

        });
    }
}
