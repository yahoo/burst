/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.funnels.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.data.PathAccessor;
import org.burstsys.motif.motif.tree.eql.funnels.BoundaryBooleanExpression;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.paths.schemas.RelationType;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.List;
import java.util.Stack;

public class BoundaryBooleanExpressionContext extends ExpressionContext implements BoundaryBooleanExpression {

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Type type;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private PathAccessor target;

    @SuppressWarnings("unused")
    public BoundaryBooleanExpressionContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.BOUNDARY_BOOLEAN);
    }

    public BoundaryBooleanExpressionContext(
            NodeGlobal global, NodeLocation location,
            Type type, PathAccessor target) {
        super(global, location, NodeType.BOUNDARY_BOOLEAN);
        this.type = type;
        this.target = target;
    }

    @Override
    public PathAccessor getTarget() {
        return target;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        target.bind(pathSymbols, stack);
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        target.validate(pathSymbols, scope, stack);
        if ( target.getBinding().getRelationType() != RelationType.INSTANCE ) {
            throw new ParseException(getLocation(), "boundary must be a valid relation path");
        }
    }

    @Override
    public String generateMotif(int level) {
        return type + " OF " + target;
    }

    @Override
    public DataType getDtype() {
        return DataType.BOOLEAN;
    }

    @Override
    public Path getLowestEvaluationPoint() {
        return target.getLowestEvaluationPoint();
    }

    @Override
    public List<Expression> getChildren() {
        return null;
    }

    @Override
    public int childCount() {
        return 0;
    }

    @Override
    public Expression getChild(int index) {
        return null;
    }

    @Override
    public Expression setChild(int index, Expression value) {
        return null;
    }
}
