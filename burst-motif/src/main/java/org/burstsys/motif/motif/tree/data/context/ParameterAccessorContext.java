/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.data.context;

import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.data.ParameterAccessor;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.symbols.Definition;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.expression.ParameterDefinition;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.paths.Path;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.lang.String.format;

public final class ParameterAccessorContext extends ExpressionContext implements ParameterAccessor, Definition.UsageContext {

    protected Path evalPath;

    protected String name;

    protected DataType dataType;

    public ParameterAccessorContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.PARAMETER);
    }

    public ParameterAccessorContext(NodeGlobal global, NodeLocation location, String name) {
        super(global, location, NodeType.PARAMETER);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DataType getDataType() {
        return dataType;
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        evalPath = pathSymbols.currentRootPath();
        Definition d = pathSymbols.getDefinition(Definition.Context.PARAMETER, this.getName());
        if (d == null)
            throw new ParseException(getLocation(), format("the parameter, '%s', is not defined", this.getName()));
        else if (d instanceof ParameterDefinition)
            dataType = d.getDtype(this);
        else
            throw new ParseException(getLocation(), format("'%s', is not defined a parameter", this.getName()));
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        Definition d = pathSymbols.getDefinition(Definition.Context.PARAMETER, this.getName());
        if (d == null)
            throw new ParseException(getLocation(), format("the parameter, '%s', is not defined", this.getName()));
        else if (!(d instanceof ParameterDefinition))
            throw new ParseException(getLocation(), format("'%s', is not defined a parameter", this.getName()));
    }

    @Override
    public DataType getDtype() {
        return dataType;
    }

    @Override
    public Path getLowestEvaluationPoint() {
        return evalPath;
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols) {
        return this;
    }


    @Override
    public String generateMotif(int level) {
        StringBuilder builder = new StringBuilder();
        builder.append('$');
        builder.append(getName());
        return returnCleanString(builder);
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .toString();
    }


    @Override
    public Boolean canReduceToConstant() {
        return false;
    }

    // parent interface returns no children
    @Override
    public List<Expression> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public int childCount() {
        return 0;
    }

    @Override
    public Expression getChild(int index) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public Expression setChild(int index, Expression value) {
        throw new IndexOutOfBoundsException();
    }

}
