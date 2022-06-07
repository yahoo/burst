/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.funnels.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.burstsys.motif.common.NodeContext;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.eql.funnels.FunnelMatchDefinition;

abstract public class FunnelMatchDefinitionBase extends NodeContext implements FunnelMatchDefinition {
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private boolean capturing = true;

    public FunnelMatchDefinitionBase(NodeType type) {
        super(NodeGlobal.defaultNodeGlobal(), type);
    }

    public FunnelMatchDefinitionBase(NodeGlobal global, NodeLocation location, NodeType type) {
        super(global, location, type);
    }

    @Override
    public void setNonCapture() {
        capturing = false;
    }

    @Override
    public boolean isCapture() {
        return capturing;
    }
}
