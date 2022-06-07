/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.values.context;

import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.motif.tree.expression.context.ExpressionContext;
import org.burstsys.motif.motif.tree.constant.LongConstant;
import org.burstsys.motif.motif.tree.values.NowValueExpression;
import org.burstsys.motif.paths.Path;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static com.google.common.base.MoreObjects.toStringHelper;

public final class NowValueExpressionContext extends ExpressionContext implements NowValueExpression {

    private Path evalPath;

    public NowValueExpressionContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.NOW_VALUE);
    }

    public NowValueExpressionContext(NodeGlobal global, NodeLocation location) {
        super(global, location, NodeType.NOW_VALUE);
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        evalPath = pathSymbols.currentRootPath();
    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
    }

    @Override
    public DataType getDtype() {
        return DataType.DATETIME;
    }

    @Override
    public Path getLowestEvaluationPoint() {
        return evalPath;
    }

    @Override
    public Expression optimize(PathSymbols pathSymbols) {
        return this;
    }


    @Override
    public String generateMotif(int level) {
        StringBuilder builder = new StringBuilder();
        builder.append("NOW");
        builder.append(' ');
        return returnCleanString(builder);
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        return endExplain(builder);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .toString();
    }


    @Override
    public Boolean canReduceToConstant() {
        return false;
    }

    @Override
    public LongConstant getNow() {
        return null;
    }

    // parent interface returns no children
    @Override
    public List<Expression> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public int childCount() {
        return 0;
    }

    @Override
    public Expression getChild(int index) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public Expression setChild(int index, Expression value) {
        throw new IndexOutOfBoundsException();
    }
}
