/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.constant.context;

import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.constant.BooleanConstant;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.motif.tree.constant.NullConstant;
import org.burstsys.motif.motif.tree.values.BinaryValueComparisonOperator;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.lang.String.format;

public final class NullConstantContext extends ConstantContext implements NullConstant {

    public NullConstantContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.NULL_CONSTANT);
    }

    public NullConstantContext(NodeGlobal global, NodeLocation location) {
        super(global, location, NodeType.NULL_CONSTANT, DataType.NULL, null);
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
    public BooleanConstant binaryBooleanCompare(BinaryValueComparisonOperator comparisonType, Constant rightConstant) {
        switch (comparisonType) {
            case EQ:
                return new BooleanConstantContext(getGlobal(), getLocation(), rightConstant.isNull());
            case NEQ:
                return new BooleanConstantContext(getGlobal(), getLocation(), !rightConstant.isNull());
            case LT:
            case LTE:
            case GT:
            case GTE:
            default:
                throw new ParseException(getLocation(), format("can't perform %s on nulls", comparisonType));
        }
    }

    @Override
    public void asNull() {
    }

}
