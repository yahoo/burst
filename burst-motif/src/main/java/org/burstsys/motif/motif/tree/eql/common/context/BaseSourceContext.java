/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.common.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.eql.common.Source;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.symbols.Definition;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static java.lang.String.format;

public class BaseSourceContext extends NodeContext implements Source {
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String declaredName;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected String name;

    public BaseSourceContext(NodeType type) {
        super(NodeGlobal.defaultNodeGlobal(), type);
    }

    public BaseSourceContext(NodeGlobal global, NodeLocation location, NodeType type, String name, String alias) {
        super(global, location, type);
        if (alias != null)
            this.name = alias;

        this.declaredName = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<ValueExpression> getParms() {
        return Collections.emptyList();
    }

    @Override
    public String getDeclaredName() {
        return declaredName;
    }

    public String generateMotif(int level) {
        return getName();
    }

    @Override
    public Definition register(PathSymbols pathSymbols) {
        // add this to the symbol tables
        Definition def = pathSymbols.getDefinition(Definition.Context.SOURCE, getDeclaredName());
        if (def == null)
            throw new ParseException(getLocation(), format("no definition found for source %s", getDeclaredName()));
        if (this.name == null) {
            this.name = declaredName;
        }
        return def;
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        // bind and optimize where
    }

    @Override
    public Evaluation optimize(PathSymbols pathSymbols) {
        return this;
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
    }
}
