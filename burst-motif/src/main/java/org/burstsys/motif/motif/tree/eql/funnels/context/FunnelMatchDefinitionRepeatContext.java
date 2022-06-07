/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.funnels.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.eql.funnels.FunnelMatchDefinition;
import org.burstsys.motif.motif.tree.eql.funnels.FunnelMatchDefinitionRepeat;
import org.burstsys.motif.motif.tree.eql.funnels.StepDefinition;

import java.util.Map;

import static java.lang.String.format;

public class FunnelMatchDefinitionRepeatContext extends FunnelMatchDefinitionBase implements FunnelMatchDefinitionRepeat {
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private int min;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private int max;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty
    private FunnelMatchDefinition definition;

    @SuppressWarnings("unused")
    public FunnelMatchDefinitionRepeatContext() {
        super(NodeType.FUNNEL_DEFINITION_REPEAT);
    }

    public FunnelMatchDefinitionRepeatContext(NodeGlobal global, NodeLocation location, int min, int max, FunnelMatchDefinition step) {
        super(global, location, NodeType.FUNNEL_DEFINITION_REPEAT);
        this.min = min;
        this.max = max;
        this.definition = step;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public FunnelMatchDefinition getStep() {
        return definition;
    }

    public String generateMotif() {
        String post = "[" + min + ":" + max + "]";
        if (isNoneOrMore())
            post = "*";
        else if (isOneOrMore())
            post = "+";
        else if (isOptional())
            post = "?";
        return  "(" + definition + ")" + post;
    }

    @Override
    public void validate(Map<Long, StepDefinition> steps) {
        definition.validate(steps);
        if (min < 0)
            throw new ParseException(getLocation(), "minimum repeat value must be positive");
        if (max != UNLIMITED && max < 0)
            throw new ParseException(getLocation(), "maximum repeat value must be positive");
        if (max != UNLIMITED && max < min)
            throw new ParseException(getLocation(), format("maximum repeat value %d must equal to or greater than minimum %d", max, min));
        if (!definition.isCapture())
            setNonCapture();

    }

    @Override
    public void setNonCapture() {
        super.setNonCapture();
        definition.setNonCapture();
    }

    protected boolean isNoneOrMore() {
        return min ==0 && max == UNLIMITED;
    }

    protected boolean isOneOrMore() {
        return min ==1 && max == UNLIMITED;
    }

    protected boolean isOptional() {
        return min ==0 && max == 1;
    }
}
