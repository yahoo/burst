/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.expression.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.motif.tree.expression.ParameterDefinition;

public class ParameterDefinitionContext  implements ParameterDefinition {
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    @JsonProperty
    private DataType dType;

    public ParameterDefinitionContext() {
    }

    public ParameterDefinitionContext(NodeGlobal global, NodeLocation location, String name, DataType type) {
        this.name = name;
        this.dType = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DataType getDtype(UsageContext context) {
        return dType;
    }

    @Override
    public Context getContext() {
        return Context.PARAMETER;
    }

    @Override
    public String generateMotif(int level) {
        return name + ":" + dType.toString();
    }
}
