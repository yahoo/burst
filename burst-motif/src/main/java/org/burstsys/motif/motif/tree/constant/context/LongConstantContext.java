/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.constant.context;

import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.constant.*;
import org.burstsys.motif.motif.tree.values.BinaryValueComparisonOperator;
import org.burstsys.motif.motif.tree.values.BinaryValueOperatorType;

import static java.lang.String.format;

public final class LongConstantContext extends NumberContext implements LongConstant {

    public LongConstantContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.LONG_CONSTANT);

    }

    public LongConstantContext(NodeGlobal global, NodeLocation location, String valueText) {
        super(global, location, NodeType.LONG_CONSTANT, DataType.LONG, valueText);
    }

    public LongConstantContext(NodeGlobal global, NodeLocation location, Long value) {
        super(global, location, NodeType.LONG_CONSTANT, DataType.LONG, value);
    }

    @Override
    public NumberConstant negate() {
        return new LongConstantContext(getGlobal(), getLocation(), (long)(-this.getLongValue()));
    }

    @Override
    public BooleanConstant binaryBooleanCompare(BinaryValueComparisonOperator comparison, Constant rightConstant) {
        checkConstantIsNumber(rightConstant);
        Long thisValue = this.getLongValue();
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
        Long leftValue = getLongValue();
        Long result = null;
        switch (rightLiteral.getDtype()) {
            case BYTE:
                switch (operator) {
                    case ADD:
                        result = leftValue + rightLiteral.asByte();
                        break;
                    case SUBTRACT:
                        result = leftValue - rightLiteral.asByte();
                        break;
                    case MULTIPLY:
                        result = leftValue * rightLiteral.asByte();
                        break;
                    case DIVIDE:
                        result = leftValue / rightLiteral.asByte();
                        break;
                    case MODULO:
                        result = leftValue % rightLiteral.asByte();
                        break;
                }
                break;
            case SHORT:
                switch (operator) {
                    case ADD:
                        result = leftValue + rightLiteral.asShort();
                        break;
                    case SUBTRACT:
                        result = leftValue - rightLiteral.asShort();
                        break;
                    case MULTIPLY:
                        result = leftValue * rightLiteral.asShort();
                        break;
                    case DIVIDE:
                        result = leftValue / rightLiteral.asShort();
                        break;
                    case MODULO:
                        result = leftValue % rightLiteral.asShort();
                        break;
                }
                break;
            case INTEGER:
                switch (operator) {
                    case ADD:
                        result = leftValue + rightLiteral.asInteger();
                        break;
                    case SUBTRACT:
                        result = leftValue - rightLiteral.asInteger();
                        break;
                    case MULTIPLY:
                        result = leftValue * rightLiteral.asInteger();
                        break;
                    case DIVIDE:
                        result = leftValue / rightLiteral.asInteger();
                        break;
                    case MODULO:
                        result = leftValue % rightLiteral.asInteger();
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
    public Long getLongValue() {
        Object v = getDataValue();
        if (v instanceof Integer)
            return ((Integer)v).longValue();
        else
            return (Long) getDataValue();
    }

    @Override
    public String asString() {
        return getLongValue().toString();
    }

    @Override
    public byte asByte() {
        throw new ParseException(getLocation(), format("can't convert to byte"));
    }

    @Override
    public short asShort() {
        throw new ParseException(getLocation(), format("can't convert to short"));
    }

    @Override
    public int asInteger() {
        throw new ParseException(getLocation(), format("can't convert to integer"));
    }

    @Override
    public long asLong() {
        return (getLongValue());
    }

    @Override
    public double asDouble() {
        return (double) (getLongValue());
    }
}
