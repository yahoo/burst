/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.constant.context;

import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.constant.BooleanConstant;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.motif.tree.values.BinaryValueComparisonOperator;

import static java.lang.String.format;

public final class BooleanConstantContext extends ConstantContext implements BooleanConstant {

    public BooleanConstantContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.BOOLEAN_CONSTANT);
    }

    public BooleanConstantContext(NodeGlobal global, NodeLocation location, String value) {
        super(global, location, NodeType.BOOLEAN_CONSTANT, DataType.BOOLEAN, value);
    }

    public BooleanConstantContext(NodeGlobal global, NodeLocation location, Boolean value) {
        super(global, location, NodeType.BOOLEAN_CONSTANT, DataType.BOOLEAN, value);
    }

    public BooleanConstantContext(Boolean value) {
        super(NodeGlobal.defaultNodeGlobal(), null, NodeType.BOOLEAN_CONSTANT, DataType.BOOLEAN, value);
    }

    @Override
    public BooleanConstant binaryBooleanCompare(BinaryValueComparisonOperator comparison, Constant rightConstant) {
        checkConstantIsBoolean(rightConstant);
        Boolean leftValue = this.getBooleanValue();
        Boolean rightValue = (Boolean) rightConstant.getDataValue();
        switch (comparison) {
            case EQ:
                return new BooleanConstantContext(getGlobal(), getLocation(), leftValue.equals(rightValue));
            case NEQ:
                return new BooleanConstantContext(getGlobal(), getLocation(), !leftValue.equals(rightValue));
            case LT:
                return new BooleanConstantContext(getGlobal(), getLocation(), leftValue.compareTo(rightValue) < 0);
            case LTE:
                return new BooleanConstantContext(getGlobal(), getLocation(), leftValue.compareTo(rightValue) > 0 || leftValue.equals(rightValue));
            case GT:
                return new BooleanConstantContext(getGlobal(), getLocation(), leftValue.compareTo(rightValue) > 0);
            case GTE:
                return new BooleanConstantContext(getGlobal(), getLocation(), leftValue.compareTo(rightValue) > 0 || leftValue.equals(rightValue));
        }
        throw new ParseException(getLocation(), format(""));
    }

    @Override
    public String asString() {
        return getBooleanValue().toString();
    }

    @Override
    public boolean asBoolean() {
        return getBooleanValue();
    }

    @Override
    public Boolean getBooleanValue() {
        return (Boolean) getDataValue();
    }

}
