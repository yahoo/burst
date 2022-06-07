/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.funnels;

import java.util.List;

/**
 */
public interface FunnelMatchDefinitionStepList extends FunnelMatchDefinition {
    Boolean isNegating();

    List<FunnelMatchDefinitionStepId> getSteps();
}

