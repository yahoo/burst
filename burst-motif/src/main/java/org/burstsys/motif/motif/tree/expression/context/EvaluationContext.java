/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.expression.context;

import org.burstsys.motif.common.NodeContext;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.symbols.PathSymbols;

public abstract class EvaluationContext extends NodeContext implements Evaluation {
    protected EvaluationContext(NodeGlobal global, NodeLocation location, NodeType ntype) {
        super(global, location, ntype);
    }

    protected EvaluationContext(NodeGlobal global, NodeType ntype) {
        super(global, ntype);
    }

    @Override
    public Evaluation optimize(PathSymbols pathSymbols) {
        return this;
    }
}
