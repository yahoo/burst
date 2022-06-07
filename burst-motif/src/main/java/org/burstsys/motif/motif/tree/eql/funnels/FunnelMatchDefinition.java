/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.funnels;

import java.util.Map;

public interface FunnelMatchDefinition {
    String generateMotif();

    void validate(Map<Long, StepDefinition> steps);

    void setNonCapture();

    boolean isCapture();
}

