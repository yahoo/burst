/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.funnels.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.eql.funnels.FunnelMatchDefinitionStepId;
import org.burstsys.motif.motif.tree.eql.funnels.StepDefinition;

import java.util.Map;

import static java.lang.String.format;

public class FunnelMatchDefinitionStepIdContext extends FunnelMatchDefinitionBase implements FunnelMatchDefinitionStepId {
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private long stepId;

    @SuppressWarnings("unused")
    public FunnelMatchDefinitionStepIdContext() {
        super(NodeType.FUNNEL_DEFINITION_STEPID);
    }

    public FunnelMatchDefinitionStepIdContext(NodeGlobal global, NodeLocation location, long stepId) {
        super(global, location, NodeType.FUNNEL_DEFINITION_STEPID);
        this.stepId = stepId;
    }

    public long getStepId() {
        return this.stepId;
    }

    public String generateMotif() {
        return Long.toString(stepId);
    }

    @Override
    public void validate(Map<Long, StepDefinition> steps) {
        if (!steps.containsKey(stepId))
            throw new ParseException(getLocation(),
                format("Step %d isn't defined", stepId));

    }
}
