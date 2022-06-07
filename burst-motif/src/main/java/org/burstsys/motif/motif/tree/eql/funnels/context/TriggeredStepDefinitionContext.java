/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.funnels.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.motif.tree.constant.context.LongConstantContext;
import org.burstsys.motif.motif.tree.data.context.PathAccessorContext;
import org.burstsys.motif.motif.tree.eql.funnels.TriggeredStepDefinition;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.paths.schemas.StructurePath;
import org.burstsys.motif.schema.model.SchemaClassifierType;
import org.burstsys.motif.schema.model.SchemaRelation;
import org.burstsys.motif.schema.model.SchemaStructure;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.*;

import static java.lang.String.format;

public class TriggeredStepDefinitionContext extends StepDefinitionContext implements TriggeredStepDefinition {

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Long id;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private ValueExpression timingExpression;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ValueExpression within;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ValueExpression after;

    @SuppressWarnings("unused")
    public TriggeredStepDefinitionContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.TRIGGERED_STEP_DEFINITION);
    }

    public TriggeredStepDefinitionContext(
            NodeGlobal global, NodeLocation location,
            Long id,
            ValueExpression timingExpression,
            ValueExpression within,
            ValueExpression after,
            BooleanExpression when) {
        super(global, location, NodeType.TRIGGERED_STEP_DEFINITION, when);
        this.id = id;
        this.timingExpression = timingExpression;
        this.after = after == null ? new LongConstantContext(global, location, 0L): after;
        this.within = within == null ? new LongConstantContext(global, location, 0L): within;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public ValueExpression getTimingExpression() {
        return this.timingExpression;
    }

    @Override
    public ValueExpression getWithin() {
        return this.within;
    }

    @Override
    public ValueExpression getAfter() {
        return this.after;
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        super.bind(pathSymbols, stack);
        stack.push(this);
        if (getTimingExpression() == null) {
            // try and infer the timing expression for the evaluation location of the when clause
            Path evalPath = getWhen().getLowestEvaluationPoint();
            SchemaStructure struct = ((StructurePath)evalPath.getEnclosingStructure()).getStructure();
            Optional<SchemaRelation> keyField = struct.getRelationNumberMap().values().stream().filter(r -> r.getClassifiers().contains(SchemaClassifierType.key)).findFirst();
            Optional<SchemaRelation> ordinalField = struct.getRelationNumberMap().values().stream().filter(r -> r.getClassifiers().contains(SchemaClassifierType.ordinal)).findFirst();
            if (keyField.isPresent()) {
                timingExpression = new PathAccessorContext(getGlobal(), getLocation(), new ArrayList<>(Arrays.asList(evalPath.toString(), ordinalField.get().getFieldName())));
            } else
                throw new ParseException(format("cannont infer a key field for step timing at evaluation location '%s'", evalPath));
        }
        getTimingExpression().bind(pathSymbols, stack);
        getAfter().bind(pathSymbols, stack);
        getWithin().bind(pathSymbols, stack);
        stack.pop();
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
        super.validate(pathSymbols, scope, stack);

        stack.push(this);
        getTimingExpression().validate(pathSymbols, scope, stack);
        getAfter().validate(pathSymbols, scope, stack);
        getWithin().validate(pathSymbols, scope, stack);
        stack.pop();

        // check that the types are all compatible
        if (DataType.findCommonDtype(getWithin().getDtype(), DataType.LONG) == null)
            throw new ParseException(format("WITHIN datatype '%s' must be compatible with long", getWithin().getDtype()));
        if (DataType.findCommonDtype(getAfter().getDtype(), DataType.LONG) == null)
            throw new ParseException(format("AFTER datatype '%s' must be compatible with long", getAfter().getDtype()));
        if (DataType.findCommonDtype(getTimingExpression().getDtype(), DataType.LONG) == null)
            throw new ParseException(format("TIMING datatype '%s' must be compatible with long", getTimingExpression().getDtype()));
        if (getTimingExpression().getLowestEvaluationPoint().notOnPath(getWhen().getLowestEvaluationPoint()))
            throw new ParseException(getLocation(),
                    format("the timing '%s' requires evaluation off-axis from the when '%s' expression", getTimingExpression(), getWhen().generateMotif(0)));
        if (getAfter() != null && !getAfter().canReduceToConstant())
            throw new ParseException(getLocation(), format("the after expression '%s' must reduce to a constant", getAfter()));
        else
            after = after.reduceToConstant();
        if (getWithin() != null && !getWithin().canReduceToConstant())
            throw new ParseException(getLocation(), format("the within expression '%s' must reduce to a constant", getWithin()));
        else
            within = within.reduceToConstant();

        if (getWithinValue() < 0)
            throw new ParseException(getLocation(), format("the within value '%s' must be greater than zero", getWithinValue()));
        if (getAfterValue() < 0)
            throw new ParseException(getLocation(), format("the within value '%s' must be greater than zero", getAfterValue()));

    }

    public long getAfterValue() {
        return ((Constant)after).asLong();
    }

    public long getWithinValue() {
        return ((Constant)within).asLong();
    }

    @Override
    public Evaluation optimize(PathSymbols pathSymbols) {
        this.timingExpression = (ValueExpression) timingExpression.optimize(pathSymbols);
        return this;
    }

    @Override
    public String generateMotif(int level) {
        StringBuilder sb = new StringBuilder("step ");
        sb.append(id).append(" on ").append(timingExpression);
        if (getAfterValue() > 0)
            sb.append(" after ").append(getAfter());
        if (getWithinValue() > 0)
            sb.append(" within ").append(getWithin());
        sb.append(" ").append(super.generateMotif(level));
        return sb.toString();
    }

    @Override
    public String toString() {
        return "step(" + getId() + ")";
    }
}
