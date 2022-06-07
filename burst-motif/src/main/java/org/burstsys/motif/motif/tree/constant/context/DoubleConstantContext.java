/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.constant.context;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.constant.BooleanConstant;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.motif.tree.constant.DoubleConstant;
import org.burstsys.motif.motif.tree.constant.NumberConstant;
import org.burstsys.motif.motif.tree.values.BinaryValueComparisonOperator;
import org.burstsys.motif.motif.tree.values.BinaryValueOperatorType;

public final class DoubleConstantContext extends NumberContext implements DoubleConstant {

    public DoubleConstantContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.DOUBLE_CONSTANT);

    }

    public DoubleConstantContext(NodeGlobal global, NodeLocation location, String value) {
        super(global, location, NodeType.DOUBLE_CONSTANT, DataType.DOUBLE, value);
    }

    public DoubleConstantContext(NodeGlobal global, NodeLocation location, Double value) {
        super(global, location, NodeType.DOUBLE_CONSTANT, DataType.DOUBLE, value);
    }

    @Override
    public NumberConstant negate() {
        return new DoubleConstantContext(getGlobal(), getLocation(), (double)(-this.getDoubleValue()));
    }

    @Override
    public Double getDoubleValue() {
        return (Double) getDataValue();
    }

    @Override
    public NumberConstant binaryOperate(BinaryValueOperatorType operator, NumberConstant rightLiteral) {
        Double leftValue = getDoubleValue();
        Double result = null;
        switch (rightLiteral.getDtype()) {
            case BYTE:
                switch (operator) {
                    case ADD:
                        result = leftValue + (byte) rightLiteral.getDataValue();
                        break;
                    case SUBTRACT:
                        result = leftValue - (byte) rightLiteral.getDataValue();
                        break;
                    case MULTIPLY:
                        result = leftValue * (byte) rightLiteral.getDataValue();
                        break;
                    case DIVIDE:
                        result = leftValue / (byte) rightLiteral.getDataValue();
                        break;
                    case MODULO:
                        result = leftValue % (byte) rightLiteral.getDataValue();
                        break;
                }
                break;
            case SHORT:
                switch (operator) {
                    case ADD:
                        result = leftValue + (short) rightLiteral.getDataValue();
                        break;
                    case SUBTRACT:
                        result = leftValue - (short) rightLiteral.getDataValue();
                        break;
                    case MULTIPLY:
                        result = leftValue * (short) rightLiteral.getDataValue();
                        break;
                    case DIVIDE:
                        result = leftValue / (short) rightLiteral.getDataValue();
                        break;
                    case MODULO:
                        result = leftValue % (short) rightLiteral.getDataValue();
                        break;
                }
                break;
            case INTEGER:
                switch (operator) {
                    case ADD:
                        result = leftValue + (int) rightLiteral.getDataValue();
                        break;
                    case SUBTRACT:
                        result = leftValue - (int) rightLiteral.getDataValue();
                        break;
                    case MULTIPLY:
                        result = leftValue * (int) rightLiteral.getDataValue();
                        break;
                    case DIVIDE:
                        result = leftValue / (int) rightLiteral.getDataValue();
                        break;
                    case MODULO:
                        result = leftValue % (int) rightLiteral.getDataValue();
                        break;
                }
                break;
            case LONG:
                switch (operator) {
                    case ADD:
                        result = leftValue + (long) rightLiteral.getDataValue();
                        break;
                    case SUBTRACT:
                        result = leftValue - (long) rightLiteral.getDataValue();
                        break;
                    case MULTIPLY:
                        result = leftValue * (long) rightLiteral.getDataValue();
                        break;
                    case DIVIDE:
                        result = leftValue / (long) rightLiteral.getDataValue();
                        break;
                    case MODULO:
                        result = leftValue % (long) rightLiteral.getDataValue();
                        break;
                }
                break;
            case DOUBLE:
                switch (operator) {
                    case ADD:
                        result = leftValue + (double) rightLiteral.getDataValue();
                        break;
                    case SUBTRACT:
                        result = leftValue - (double) rightLiteral.getDataValue();
                        break;
                    case MULTIPLY:
                        result = leftValue * (double) rightLiteral.getDataValue();
                        break;
                    case DIVIDE:
                        result = leftValue / (double) rightLiteral.getDataValue();
                        break;
                    case MODULO:
                        result = leftValue % (double) rightLiteral.getDataValue();
                        break;
                }
                break;
        }
        return new DoubleConstantContext(getGlobal(), getLocation(), result);
    }

    @Override
    public BooleanConstant binaryBooleanCompare(BinaryValueComparisonOperator comparison, Constant rightConstant) {
        checkConstantIsNumber(rightConstant);
        Double thisValue = this.getDoubleValue();
        Boolean result = null;
        switch (rightConstant.getDtype()) {
            case BYTE:
                switch (comparison) {
                    case EQ:
                        result = thisValue == rightConstant.asByte();
                        break;
                    case NEQ:
                        result = thisValue != rightConstant.asByte();
                        break;
                    case LT:
                        result = thisValue < rightConstant.asByte();
                        break;
                    case LTE:
                        result = thisValue <= rightConstant.asByte();
                        break;
                    case GT:
                        result = thisValue > rightConstant.asByte();
                        break;
                    case GTE:
                        result = thisValue >= rightConstant.asByte();
                        break;
                }
            case SHORT:
                switch (comparison) {
                    case EQ:
                        result = thisValue == rightConstant.asShort();
                        break;
                    case NEQ:
                        result = thisValue != rightConstant.asShort();
                        break;
                    case LT:
                        result = thisValue < rightConstant.asShort();
                        break;
                    case LTE:
                        result = thisValue <= rightConstant.asShort();
                        break;
                    case GT:
                        result = thisValue > rightConstant.asShort();
                        break;
                    case GTE:
                        result = thisValue >= rightConstant.asShort();
                        break;
                }
            case INTEGER:
                switch (comparison) {
                    case EQ:
                        result = thisValue == rightConstant.asInteger();
                        break;
                    case NEQ:
                        result = thisValue != rightConstant.asInteger();
                        break;
                    case LT:
                        result = thisValue < rightConstant.asInteger();
                        break;
                    case LTE:
                        result = thisValue <= rightConstant.asInteger();
                        break;
                    case GT:
                        result = thisValue > rightConstant.asInteger();
                        break;
                    case GTE:
                        result = thisValue >= rightConstant.asInteger();
                        break;
                }
            case LONG:
                switch (comparison) {
                    case EQ:
                        result = thisValue == rightConstant.asLong();
                        break;
                    case NEQ:
                        result = thisValue != rightConstant.asLong();
                        break;
                    case LT:
                        result = thisValue < rightConstant.asLong();
                        break;
                    case LTE:
                        result = thisValue <= rightConstant.asLong();
                        break;
                    case GT:
                        result = thisValue > rightConstant.asLong();
                        break;
                    case GTE:
                        result = thisValue >= rightConstant.asLong();
                        break;
                }
            case DOUBLE:
                switch (comparison) {
                    case EQ:
                        result = thisValue == rightConstant.asDouble();
                        break;
                    case NEQ:
                        result = thisValue != rightConstant.asDouble();
                        break;
                    case LT:
                        result = thisValue < rightConstant.asDouble();
                        break;
                    case LTE:
                        result = thisValue <= rightConstant.asDouble();
                        break;
                    case GT:
                        result = thisValue > rightConstant.asDouble();
                        break;
                    case GTE:
                        result = thisValue >= rightConstant.asDouble();
                        break;
                }
        }
        assert result != null;
        return new BooleanConstantContext(getGlobal(), getLocation(), result);
    }

    @Override
    public String asString() {
        return getDoubleValue().toString();
    }

    @Override
    public byte asByte() {
        return (byte) ((double) getDoubleValue());
    }

    @Override
    public short asShort() {
        return (short) ((double) getDoubleValue());
    }

    @Override
    public int asInteger() {
        return (int) ((double) getDoubleValue());
    }

    @Override
    public long asLong() {
        return (long) ((double) getDoubleValue());
    }

    @Override
    public double asDouble() {
        return getDoubleValue();
    }

}
