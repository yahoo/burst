/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.constant.context;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.constant.*;
import org.burstsys.motif.motif.tree.values.BinaryValueComparisonOperator;
import org.burstsys.motif.motif.tree.values.BinaryValueOperatorType;

public final class ByteConstantContext extends NumberContext implements ByteConstant {

    public ByteConstantContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.BYTE_CONSTANT);

    }

    public ByteConstantContext(NodeGlobal global, NodeLocation location, String valueText) {
        super(global, location, NodeType.BYTE_CONSTANT, DataType.BYTE, valueText);
    }

    public ByteConstantContext(NodeGlobal global, NodeLocation location, Byte value) {
        super(global, location, NodeType.BYTE_CONSTANT, DataType.BYTE, value);
    }

    @Override
    public BooleanConstant binaryBooleanCompare(BinaryValueComparisonOperator comparison, Constant rightConstant) {
        checkConstantIsNumber(rightConstant);
        Byte thisValue = this.getByteValue();
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
        Byte leftValue = getByteValue();
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
    public NumberConstant negate() {
        return new ByteConstantContext(getGlobal(), getLocation(), (byte)(-this.getByteValue()));
    }

    @Override
    public String asString() {
        return getByteValue().toString();
    }

    @Override
    public byte asByte() {
        return getByteValue();
    }

    @Override
    public short asShort() {
        return (short) ((byte) getByteValue());
    }

    @Override
    public int asInteger() {
        return (int) ((byte) getByteValue());
    }

    @Override
    public long asLong() {
        return (long) ((byte) getByteValue());
    }

    @Override
    public double asDouble() {
        return (double) ((byte) getByteValue());
    }

    @Override
    public Byte getByteValue() {
        // JSON deserializers can make this on Int so check first
        Object v = getDataValue();
        if (v instanceof Integer)
            return ((Integer)v).byteValue();
        else
            return (Byte) getDataValue();
    }

}
