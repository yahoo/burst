/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.common.context;

import org.burstsys.motif.common.NodeContext;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.eql.common.Statement;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.schema.model.MotifSchema;
import org.burstsys.motif.symbols.PathSymbols;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Stack;

public abstract class StatementContext extends NodeContext implements Statement {
    protected PathSymbols pathSymbols;

    public StatementContext(NodeGlobal global, NodeLocation location, NodeType ntype) {
        super(global, location, ntype);
    }

    public StatementContext(NodeGlobal global, NodeType ntype) {
        super(global, ntype);
    }

    public MotifSchema getSchema() {
        return null;
    }

    @OverridingMethodsMustInvokeSuper
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        this.pathSymbols = pathSymbols;
    }

    @Override
    public Evaluation optimize(PathSymbols pathSymbols) {
        return this;
    }
}
