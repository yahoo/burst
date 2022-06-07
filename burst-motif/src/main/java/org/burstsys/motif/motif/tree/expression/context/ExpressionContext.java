/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.expression.context;

import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.motif.tree.expression.Expression;

public abstract class ExpressionContext extends EvaluationContext implements Expression {

    protected ExpressionContext(NodeGlobal global, NodeLocation location, NodeType ntype) {
        super(global, location, ntype);
    }

    protected ExpressionContext(NodeGlobal global, NodeType ntype) {
        super(global, ntype);
    }

    public Constant reduceToConstant() {
        throw new RuntimeException("not static resolvable");
    }

    protected void checkCanReduceToConstant() {
        if (!canReduceToConstant())
            throw new RuntimeException("not static resolvable");
    }

    @Override
    public Boolean canReduceToConstant() {
        return false;
    }

    @Override
    public void walkTree(NodeWalker checker) {
        if (checker == null)
            return;
        for (Expression e: this.getChildren()) {
            if (e != null)
                e.walkTree(checker);
        }
        checker.check(this);
    }
}
