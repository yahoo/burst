/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.funnels;

import java.util.List;

/**
 */
public interface FunnelMatchDefinitionList extends FunnelMatchDefinition {
    enum Op {
        AND(1), OR(2);
        private final int opCode;

        Op(int opCode) {
            this.opCode = opCode;
        }

        public int getOpCode() {
            return opCode;
        }
    }

    Op getOp();

    List<FunnelMatchDefinition> getSteps();

    boolean isStartCapture();

    boolean isEndCapture();
}

