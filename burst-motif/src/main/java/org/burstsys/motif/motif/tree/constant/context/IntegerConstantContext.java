/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.constant.context;

import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.constant.*;
import org.burstsys.motif.motif.tree.values.BinaryValueComparisonOperator;
import org.burstsys.motif.motif.tree.values.BinaryValueOperatorType;

import static java.lang.Long.valueOf;

public final class IntegerConstantContext extends NumberContext implements IntegerConstant {

    public IntegerConstantContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.INTEGER_CONSTANT);
    }

    public IntegerConstantContext(NodeGlobal global, NodeLocation location, String valueText) {
        super(global, location, NodeType.INTEGER_CONSTANT, DataType.INTEGER, valueText);
    }

    public IntegerConstantContext(NodeGlobal global, NodeLocation location, Integer value) {
        super(global, location, NodeType.INTEGER_CONSTANT, DataType.INTEGER, value);
    }

    @Override
    public NumberConstant negate() {
        return new IntegerConstantContext(getGlobal(), getLocation(), (int)(-this.getIntegerValue()));
    }

    @Override
    public BooleanConstant binaryBooleanCompare(BinaryValueComparisonOperator comparison, Constant rightConstant) {
        checkConstantIsNumber(rightConstant);
        Long thisValue = Long.valueOf(this.getIntegerValue());
        Boolean result = null;
        switch (rightConstant.getDtype()) {
            case BYTE:
                long rightValue1 = (Byte) rightConstant.getDataValue();
                switch (comparison) {
                    case EQ:
                        result = thisValue == rightValue1;
                        break;
                    case NEQ:
                        result = thisValue != rightValue1;
                        break;
                    case LT:
                        result = thisValue < rightValue1;
                        break;
                    case LTE:
                        result = thisValue <= rightValue1;
                        break;
                    case GT:
                        result = thisValue > rightValue1;
                        break;
                    case GTE:
                        result = thisValue >= rightValue1;
                        break;
                }
                break;
            case SHORT:
                short rightValue2 = (Short) rightConstant.getDataValue();
                switch (comparison) {
                    case EQ:
                        result = thisValue == rightValue2;
                        break;
                    case NEQ:
                        result = thisValue != rightValue2;
                        break;
                    case LT:
                        result = thisValue < rightValue2;
                        break;
                    case LTE:
                        result = thisValue <= rightValue2;
                        break;
                    case GT:
                        result = thisValue > rightValue2;
                        break;
                    case GTE:
                        result = thisValue >= rightValue2;
                        break;
                }
                break;
            case INTEGER:
                int rightValue3 = (Integer) rightConstant.getDataValue();
                switch (comparison) {
                    case EQ:
                        result = thisValue == rightValue3;
                        break;
                    case NEQ:
                        result = thisValue != rightValue3;
                        break;
                    case LT:
                        result = thisValue < rightValue3;
                        break;
                    case LTE:
                        result = thisValue <= rightValue3;
                        break;
                    case GT:
                        result = thisValue > rightValue3;
                        break;
                    case GTE:
                        result = thisValue >= rightValue3;
                        break;
                }
                break;
            case LONG:
                long rightValue4 = (Long) rightConstant.getDataValue();
                switch (comparison) {
                    case EQ:
                        result = thisValue == rightValue4;
                        break;
                    case NEQ:
                        result = thisValue != rightValue4;
                        break;
                    case LT:
                        result = thisValue < rightValue4;
                        break;
                    case LTE:
                        result = thisValue <= rightValue4;
                        break;
                    case GT:
                        result = thisValue > rightValue4;
                        break;
                    case GTE:
                        result = thisValue >= rightValue4;
                        break;
                }
                break;
            case DOUBLE:
                double rightValue5 = (Double) rightConstant.getDataValue();
                switch (comparison) {
                    case EQ:
                        result = thisValue == rightValue5;
                        break;
                    case NEQ:
                        result = thisValue != rightValue5;
                        break;
                    case LT:
                        result = thisValue < rightValue5;
                        break;
                    case LTE:
                        result = thisValue <= rightValue5;
                        break;
                    case GT:
                        result = thisValue > rightValue5;
                        break;
                    case GTE:
                        result = thisValue >= rightValue5;
                        break;
                }
                break;
        }
        assert result != null;
        return new BooleanConstantContext(getGlobal(), getLocation(), result);
    }


    @Override
    public NumberConstant binaryOperate(BinaryValueOperatorType operator, NumberConstant rightLiteral) {
        Integer leftValue = getIntegerValue();
        switch (rightLiteral.getDtype()) {
            case BYTE:
                Integer r1 = null;
                switch (operator) {
                    case ADD:
                        r1 = leftValue + rightLiteral.asByte();
                        break;
                    case SUBTRACT:
                        r1 = leftValue - rightLiteral.asByte();
                        break;
                    case MULTIPLY:
                        r1 = leftValue * rightLiteral.asByte();
                        break;
                    case DIVIDE:
                        r1 = leftValue / rightLiteral.asByte();
                        break;
                    case MODULO:
                        r1 = leftValue % rightLiteral.asByte();
                        break;
                }
                return new IntegerConstantContext(getGlobal(), getLocation(), r1);
            case SHORT:
                Integer r2 = null;
                switch (operator) {
                    case ADD:
                        r2 = leftValue + rightLiteral.asShort();
                        break;
                    case SUBTRACT:
                        r2 = leftValue - rightLiteral.asShort();
                        break;
                    case MULTIPLY:
                        r2 = leftValue * rightLiteral.asShort();
                        break;
                    case DIVIDE:
                        r2 = leftValue / rightLiteral.asShort();
                        break;
                    case MODULO:
                        r2 = leftValue % rightLiteral.asShort();
                        break;
                }
                return new IntegerConstantContext(getGlobal(), getLocation(), r2);
            case INTEGER:
                Integer r3 = null;
                switch (operator) {
                    case ADD:
                        r3 = leftValue + rightLiteral.asInteger();
                        break;
                    case SUBTRACT:
                        r3 = leftValue - rightLiteral.asInteger();
                        break;
                    case MULTIPLY:
                        r3 = leftValue * rightLiteral.asInteger();
                        break;
                    case DIVIDE:
                        r3 = leftValue / rightLiteral.asInteger();
                        break;
                    case MODULO:
                        r3 = leftValue % rightLiteral.asInteger();
                        break;
                }
                return new IntegerConstantContext(getGlobal(), getLocation(), r3);
            case LONG:
                Long r4 = null;
                switch (operator) {
                    case ADD:
                        r4 = leftValue + rightLiteral.asLong();
                        break;
                    case SUBTRACT:
                        r4 = leftValue - rightLiteral.asLong();
                        break;
                    case MULTIPLY:
                        r4 = leftValue * rightLiteral.asLong();
                        break;
                    case DIVIDE:
                        r4 = leftValue / rightLiteral.asLong();
                        break;
                    case MODULO:
                        r4 = leftValue % rightLiteral.asLong();
                        break;
                }
                return new LongConstantContext(getGlobal(), getLocation(), r4);
            case DOUBLE:
                Double r5 = null;
                switch (operator) {
                    case ADD:
                        r5 = leftValue + rightLiteral.asDouble();
                        break;
                    case SUBTRACT:
                        r5 = leftValue - rightLiteral.asDouble();
                        break;
                    case MULTIPLY:
                        r5 = leftValue * rightLiteral.asDouble();
                        break;
                    case DIVIDE:
                        r5 = leftValue / rightLiteral.asDouble();
                        break;
                    case MODULO:
                        r5 = leftValue % rightLiteral.asDouble();
                        break;
                }
                return new DoubleConstantContext(getGlobal(), getLocation(), r5);
        }
        throw new ParseException(getLocation(), "incompatible operations");
    }

    @Override
    public Integer getIntegerValue() {
        return (Integer) getDataValue();
    }


    @Override
    public String asString() {
        return getIntegerValue().toString();
    }

    @Override
    public byte asByte() {
        return (byte) ((int) getIntegerValue());
    }

    @Override
    public short asShort() {
        return (short) ((int) getIntegerValue());
    }

    @Override
    public int asInteger() {
        return getIntegerValue();
    }

    @Override
    public long asLong() {
        return (long) ((int) getIntegerValue());
    }

    @Override
    public double asDouble() {
        return (double) ((int) getIntegerValue());
    }


}
