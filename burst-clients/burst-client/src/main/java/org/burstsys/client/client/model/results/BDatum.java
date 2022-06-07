/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.client.model.results;

import org.burstsys.gen.thrift.api.client.BTDataType;
import org.burstsys.gen.thrift.api.client.query.BTCell;

import java.util.List;
import java.util.Map;

public class BDatum {
    private final Object value;
    private final Format format;
    private final DataType primaryType;
    private final DataType secondaryType;

    private BDatum(Object value, Format format, DataType primaryType, DataType secondaryType) {
        this.value = value;
        this.format = format;
        this.primaryType = primaryType;
        this.secondaryType = secondaryType;
    }

    public static BDatum fromThriftCell(BTCell cell, DataType type) {
        Object value;
        if (cell.datum.isSetBoolVal()) {
            value = cell.datum.getBoolVal();
        } else if (cell.datum.isSetByteVal()) {
            value = cell.datum.getByteVal();
        } else if (cell.datum.isSetShortVal()) {
            value = cell.datum.getShortVal();
        } else if (cell.datum.isSetIntVal()) {
            value = cell.datum.getIntVal();
        } else if (cell.datum.isSetLongVal()) {
            value = cell.datum.getLongVal();
        } else if (cell.datum.isSetDoubleVal()) {
            value = cell.datum.getDoubleVal();
        } else if (cell.datum.isSetStringVal()) {
            value = cell.datum.getStringVal();
        } else { // Cells can only return scalar values
            throw new IllegalStateException("Unable to unmarshall cell. type=" + cell.dType + " datum=" + cell.datum);
        }
        return new BDatum(value, Format.SCALAR, type, null);
    }

    public Object value() {
        return value;
    }

    public boolean boolVal() {
        return getScalar(DataType.BOOL_TYPE);
    }

    public byte byteVal() {
        return getScalar(DataType.BYTE_TYPE);
    }

    public short shortVal() {
        return getScalar(DataType.SHORT_TYPE);
    }

    public int intVal() {
        return getScalar(DataType.INT_TYPE);
    }

    public long longVal() {
        return getScalar(DataType.LONG_TYPE);
    }

    public double doubleVal() {
        return getScalar(DataType.DOUBLE_TYPE);
    }

    public String stringVal() {
        return getScalar(DataType.STRING_TYPE);
    }

    @SuppressWarnings("unchecked")
    private <T> T getScalar(DataType desiredType) {
        if (format != Format.SCALAR) {
            throw new IllegalStateException("Datum is not a scalar value");
        } else if (primaryType != desiredType) {
            throw new IllegalStateException(
                    "Datum has the wrong type. Wanted=" + desiredType + ", found=" + primaryType);
        }
        return (T) value;
    }

    public List<Boolean> boolVector() {
        return getListValue(DataType.BOOL_TYPE);
    }

    public List<Byte> byteVector() {
        return getListValue(DataType.BYTE_TYPE);
    }

    public List<Short> shortVector() {
        return getListValue(DataType.SHORT_TYPE);
    }

    public List<Integer> intVector() {
        return getListValue(DataType.INT_TYPE);
    }

    public List<Long> longVector() {
        return getListValue(DataType.LONG_TYPE);
    }

    public List<Double> doubleVector() {
        return getListValue(DataType.DOUBLE_TYPE);
    }

    public List<String> stringVector() {
        return getListValue(DataType.STRING_TYPE);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getListValue(DataType desiredType) {
        if (format != Format.VECTOR) {
            throw new IllegalStateException("Datum is not a list value");
        } else if (primaryType != desiredType) {
            throw new IllegalStateException(
                    "Datum has the wrong type. Wanted=" + desiredType + ", found=" + primaryType);
        }
        return (List<T>) value;
    }

    // map values
    public Map<String, Boolean> stringBoolMap() {
        return getMapValue(DataType.STRING_TYPE, DataType.BOOL_TYPE);
    }

    public Map<String, Integer> stringIntMap() {
        return getMapValue(DataType.STRING_TYPE, DataType.INT_TYPE);
    }

    public Map<String, Long> stringLongMap() {
        return getMapValue(DataType.STRING_TYPE, DataType.LONG_TYPE);
    }

    public Map<String, String> stringStringMapData() {
        return getMapValue(DataType.STRING_TYPE, DataType.STRING_TYPE);
    }

    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> getMapValue(DataType keys, DataType values) {
        if (format != Format.VECTOR) {
            throw new IllegalStateException("Datum is not a list value");
        } else if (primaryType != keys || secondaryType != values) {
            throw new IllegalStateException(
                    "Datum has the wrong type. Wanted=<" + keys + "," + values + "> found=<" + primaryType + "," + secondaryType + ">");
        }
        return (Map<K, V>) value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Datum<");
        if (format == Format.SCALAR) {
            sb.append(primaryType.getTypeName()).append("=");
        } else if (format == Format.VECTOR) {
            sb.append(primaryType.getTypeName()).append("[]=");
        } else if (format == Format.MAP) {
            sb.append("map{").append(primaryType.getTypeName()).append(",")
              .append(secondaryType.getTypeName()).append("}=");
        }
        sb.append(value).append(">");
        return sb.toString();
    }

    public enum DataType {
        BOOL_TYPE, BYTE_TYPE, SHORT_TYPE, INT_TYPE, LONG_TYPE, DOUBLE_TYPE, STRING_TYPE;

        public static DataType fromThrift(BTDataType type) {
            switch (type) {
                case BoolType:
                    return BOOL_TYPE;
                case ByteType:
                    return BYTE_TYPE;
                case ShortType:
                    return SHORT_TYPE;
                case IntType:
                    return INT_TYPE;
                case LongType:
                    return LONG_TYPE;
                case DoubleType:
                    return DOUBLE_TYPE;
                case StringType:
                    return STRING_TYPE;
                default:
                    throw new IllegalStateException("Unknown thrift datatype=" + type);
            }
        }

        public String getTypeName() {
            switch (this) {
                case BOOL_TYPE:
                    return "bool";
                case BYTE_TYPE:
                    return "byte";
                case SHORT_TYPE:
                    return "short";
                case INT_TYPE:
                    return "int";
                case LONG_TYPE:
                    return "long";
                case DOUBLE_TYPE:
                    return "double";
                case STRING_TYPE:
                    return "string";
                default:
                    return "??(" + this + ")";
            }
        }
    }

    public enum Format {
        SCALAR, VECTOR, MAP
    }
}
