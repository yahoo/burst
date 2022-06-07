/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.funnels.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.eql.funnels.FunnelMatchDefinitionStepList;
import org.burstsys.motif.motif.tree.eql.funnels.FunnelMatchDefinition;
import org.burstsys.motif.motif.tree.eql.funnels.FunnelMatchDefinitionStepId;
import org.burstsys.motif.motif.tree.eql.funnels.StepDefinition;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FunnelMatchDefinitionStepListContext extends FunnelMatchDefinitionBase implements FunnelMatchDefinitionStepList {
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private boolean negating = true;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private List<FunnelMatchDefinitionStepId> stepList;

    @SuppressWarnings("unused")
    public FunnelMatchDefinitionStepListContext() {
        super(NodeType.FUNNEL_BRACKET_LIST);
    }

    public FunnelMatchDefinitionStepListContext(NodeGlobal global, NodeLocation location, boolean negating,
                                                List<FunnelMatchDefinitionStepId> steps) {
        super(global, location, NodeType.FUNNEL_BRACKET_LIST);
        this.negating = negating;
        this.stepList = steps;
    }

    @Override
    public Boolean isNegating() {
        return negating;
    }

    public List<FunnelMatchDefinitionStepId> getSteps() {
        return stepList;
    }

    public String generateMotif() {
        return (negating ? "!" : "") + "(" + stepList.stream().map(Object::toString).collect(Collectors.joining(" : ")) + ")";
    }

    @Override
    public void validate(Map<Long, StepDefinition> steps) {
        stepList.forEach(s->s.validate(steps));
    }

    @Override
    public void setNonCapture() {
        super.setNonCapture();
        stepList.forEach(FunnelMatchDefinition::setNonCapture);
    }
}
