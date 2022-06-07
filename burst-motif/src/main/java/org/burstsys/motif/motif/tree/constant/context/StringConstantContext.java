/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.constant.context;

import org.burstsys.motif.common.*;
import org.burstsys.motif.motif.tree.constant.BooleanConstant;
import org.burstsys.motif.motif.tree.constant.Constant;
import org.burstsys.motif.motif.tree.constant.StringConstant;
import org.burstsys.motif.motif.tree.values.BinaryValueComparisonOperator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Objects;

public final class StringConstantContext extends ConstantContext implements StringConstant {

    public StringConstantContext() {
        super(NodeGlobal.defaultNodeGlobal(), NodeType.STRING_CONSTANT);
    }

    public StringConstantContext(NodeGlobal global, NodeLocation location, String value) {
        super(global, location, NodeType.STRING_CONSTANT, DataType.STRING, normalizeString(value));
    }

   public static String normalizeString(String text) {
        String result;
        switch (text.charAt(0)) {
            case '\'': {
                result = text.replaceFirst("'", "");
                int endIndex = result.lastIndexOf("'");
                if (endIndex != -1)
                    result = result.substring(0, endIndex);
                // convert escape sequence
                result = result.replace("''", "'");
            }
            break;
            case '"': {
                result = text.replaceFirst("\"", "");
                int endIndex = result.lastIndexOf("\"");
                if (endIndex != -1)
                    result = result.substring(0, endIndex);
                // convert escape sequence
                result = result.replace("\"\"", "\"");
            }
            break;
            default :
                result = text;
        }
        return result;
    }

    private static String reescapeStringForMotif(String text) {
        // We should not re-escape double quotes because generated motif is normalized to single quoted strings
        return text.replace("'", "''");
    }

    @Override
    public String generateMotif(int level) {
        return "'" + reescapeStringForMotif(getStringValue()) + "'";
    }

    @Override
    public String getStringValue() {
        return (String) this.getDataValue();
    }

    @Override
    public BooleanConstant binaryBooleanCompare(BinaryValueComparisonOperator comparison, Constant rightConstant) {
        checkConstantIsString(rightConstant);
        String leftValue = this.getStringValue();
        String rightValue = (String) rightConstant.getDataValue();
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
        throw new ParseException(getLocation(), "");
    }

    @Override
    public boolean asBoolean() {
        return Boolean.parseBoolean(getStringValue());
    }

    @Override
    public byte asByte() {
        return Byte.decode(getStringValue());
    }

    @Override
    public short asShort() {
        return Short.decode(getStringValue());
    }

    @Override
    public int asInteger() {
        return Integer.decode(getStringValue());
    }

    @Override
    public long asLong() {
        return Long.decode(getStringValue());
    }

    @Override
    public double asDouble() {
        return Double.parseDouble(getStringValue());
    }

    @Override
    public long asDatetime() {
        return new DateTime(this.getStringValue(), DateTimeZone.forID(Objects.requireNonNull(getGlobal()).defaultTimeZoneName())).getMillis();
    }

    @Override
    public String asString() {
        return getStringValue();
    }

}
