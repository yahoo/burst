/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.funnels;

/**
 */
public interface FunnelMatchDefinitionRepeat extends FunnelMatchDefinition {
    int getMin();

    int getMax();

    FunnelMatchDefinition getStep();

    Integer UNLIMITED = -1;
}

