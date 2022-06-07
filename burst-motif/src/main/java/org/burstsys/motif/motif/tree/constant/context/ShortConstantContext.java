/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.constant.context;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.constant.*;
import org.burstsys.motif.motif.tree.values.BinaryValueComparisonOperator;
import org.burstsys.motif.motif.tree.values.BinaryValueOperatorType;

public final class ShortConstantContext extends NumberContext implements ShortConstant {

    public ShortConstantContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.SHORT_CONSTANT);
    }

    public ShortConstantContext(NodeGlobal global, NodeLocation location, String valueText) {
        super(global, location, NodeType.SHORT_CONSTANT, DataType.SHORT, valueText);
    }

    public ShortConstantContext(NodeGlobal global, NodeLocation location, Short value) {
        super(global, location, NodeType.SHORT_CONSTANT, DataType.SHORT, value);
    }

    @Override
    public NumberConstant negate() {
        return new ShortConstantContext(getGlobal(), getLocation(), (short)(-this.getShortValue()));
    }

    @Override
    public BooleanConstant binaryBooleanCompare(BinaryValueComparisonOperator comparison, Constant rightConstant) {
        checkConstantIsNumber(rightConstant);
        Short thisValue = this.getShortValue();
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
    public NumberConstant binaryOperate(BinaryValueOperatorType operator, NumberConstant rightLiteral) {
        Short leftValue = getShortValue();
        Long result = null;
        switch (rightLiteral.getDtype()) {
            case BYTE:
                switch (operator) {
                    case ADD:
                        result = leftValue + rightLiteral.asLong();
                        break;
                    case SUBTRACT:
                        result = leftValue - rightLiteral.asLong();
                        break;
                    case MULTIPLY:
                        result = leftValue * rightLiteral.asLong();
                        break;
                    case DIVIDE:
                        result = leftValue / rightLiteral.asLong();
                        break;
                    case MODULO:
                        result = leftValue % rightLiteral.asLong();
                        break;
                }
                break;
            case SHORT:
                switch (operator) {
                    case ADD:
                        result = leftValue + rightLiteral.asLong();
                        break;
                    case SUBTRACT:
                        result = leftValue - rightLiteral.asLong();
                        break;
                    case MULTIPLY:
                        result = leftValue * rightLiteral.asLong();
                        break;
                    case DIVIDE:
                        result = leftValue / rightLiteral.asLong();
                        break;
                    case MODULO:
                        result = leftValue % rightLiteral.asLong();
                        break;
                }
                break;
            case INTEGER:
                switch (operator) {
                    case ADD:
                        result = leftValue + rightLiteral.asLong();
                        break;
                    case SUBTRACT:
                        result = leftValue - rightLiteral.asLong();
                        break;
                    case MULTIPLY:
                        result = leftValue * rightLiteral.asLong();
                        break;
                    case DIVIDE:
                        result = leftValue / rightLiteral.asLong();
                        break;
                    case MODULO:
                        result = leftValue % rightLiteral.asLong();
                        break;
                }
                break;
            case LONG:
                switch (operator) {
                    case ADD:
                        result = leftValue + rightLiteral.asLong();
                        break;
                    case SUBTRACT:
                        result = leftValue - rightLiteral.asLong();
                        break;
                    case MULTIPLY:
                        result = leftValue * rightLiteral.asLong();
                        break;
                    case DIVIDE:
                        result = leftValue / rightLiteral.asLong();
                        break;
                    case MODULO:
                        result = leftValue % rightLiteral.asLong();
                        break;
                }
                break;
            case DOUBLE:
                switch (operator) {
                    case ADD:
                        result = leftValue + rightLiteral.asLong();
                        break;
                    case SUBTRACT:
                        result = leftValue - rightLiteral.asLong();
                        break;
                    case MULTIPLY:
                        result = leftValue * rightLiteral.asLong();
                        break;
                    case DIVIDE:
                        result = leftValue / rightLiteral.asLong();
                        break;
                    case MODULO:
                        result = leftValue % rightLiteral.asLong();
                        break;
                }
                break;
        }
        return new LongConstantContext(getGlobal(), getLocation(), result);
    }

    @Override
    public Short getShortValue() {
        return (Short) getDataValue();
    }

    @Override
    public String asString() {
        return getShortValue().toString();
    }

    @Override
    public byte asByte() {
        return (byte) ((short) getShortValue());
    }

    @Override
    public short asShort() {
        return (short) ((short) getShortValue());
    }

    @Override
    public int asInteger() {
        return (int) ((short) getShortValue());
    }

    @Override
    public long asLong() {
        return (long) ((short) getShortValue());
    }

    @Override
    public double asDouble() {
        return (double) ((short) getShortValue());
    }


}
