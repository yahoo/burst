/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.funnels.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.eql.funnels.FunnelMatchDefinition;
import org.burstsys.motif.motif.tree.eql.funnels.FunnelMatchDefinitionList;
import org.burstsys.motif.motif.tree.eql.funnels.StepDefinition;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class FunnelMatchDefinitionListContext extends FunnelMatchDefinitionBase implements FunnelMatchDefinitionList {
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Op op;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private List<FunnelMatchDefinition> definitionList;

    @SuppressWarnings("unused")
    public FunnelMatchDefinitionListContext() {
        super(NodeType.FUNNEL_DEFINITION_LIST);
    }

    public FunnelMatchDefinitionListContext(NodeGlobal global, NodeLocation location, Op op, List<FunnelMatchDefinition> steps) {
        super(global, location, NodeType.FUNNEL_DEFINITION_LIST);
        this.op = op;
        this.definitionList = steps;
    }

    public Op getOp() {
        return op;
    }

    public List<FunnelMatchDefinition> getSteps() {
        return definitionList;
    }

    @Override
    public boolean isStartCapture() {
        return definitionList.get(0).isCapture();
    }

    @Override
    public boolean isEndCapture() {
        return definitionList.get(definitionList.size()-1).isCapture();
    }

    public String generateMotif() {
        return op + "(" + definitionList.stream().map(Object::toString).collect(Collectors.joining(" : ")) + ")";
    }

    @Override
    public void validate(Map<Long, StepDefinition> steps) {
        definitionList.forEach(s->s.validate(steps));
        Map<Boolean, List<FunnelMatchDefinition>> captures = definitionList.stream().collect(Collectors.partitioningBy(FunnelMatchDefinition::isCapture));
        boolean hasCapturing = captures.containsKey(Boolean.TRUE) && captures.get(Boolean.TRUE).size() > 0;
        boolean hasNonCapturing = captures.containsKey(Boolean.FALSE) && captures.get(Boolean.FALSE).size() > 0;
        if (getOp() == Op.OR) {
            // every defintion in an OR list must be all capturing or non capturing...no mixing
            if (hasCapturing && hasNonCapturing)
                throw new ParseException(getLocation(), format("%s list does not support mixing of non-capturing and capturing", getOp()));
            if (hasNonCapturing)
                setNonCapture();
        } else if (getOp() == Op.AND) {
            if (!hasCapturing && hasNonCapturing)
                setNonCapture();
        }
    }

    @Override
    public void setNonCapture() {
        super.setNonCapture();
        definitionList.forEach(FunnelMatchDefinition::setNonCapture);
    }
}
