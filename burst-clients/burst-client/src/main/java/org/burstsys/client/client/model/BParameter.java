/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.client.model;

import org.burstsys.gen.thrift.api.client.BTDataFormat;
import org.burstsys.gen.thrift.api.client.BTDataType;
import org.burstsys.gen.thrift.api.client.BTDatum;
import org.burstsys.gen.thrift.api.client.query.BTParameter;

import java.util.List;
import java.util.Map;

import static org.burstsys.gen.thrift.api.client.BTDatum._Fields.BOOL_VAL;
import static org.burstsys.gen.thrift.api.client.BTDatum._Fields.BOOL_VECTOR;
import static org.burstsys.gen.thrift.api.client.BTDatum._Fields.BYTE_VAL;
import static org.burstsys.gen.thrift.api.client.BTDatum._Fields.BYTE_VECTOR;
import static org.burstsys.gen.thrift.api.client.BTDatum._Fields.DOUBLE_VAL;
import static org.burstsys.gen.thrift.api.client.BTDatum._Fields.DOUBLE_VECTOR;
import static org.burstsys.gen.thrift.api.client.BTDatum._Fields.INT_VAL;
import static org.burstsys.gen.thrift.api.client.BTDatum._Fields.INT_VECTOR;
import static org.burstsys.gen.thrift.api.client.BTDatum._Fields.LONG_VAL;
import static org.burstsys.gen.thrift.api.client.BTDatum._Fields.LONG_VECTOR;
import static org.burstsys.gen.thrift.api.client.BTDatum._Fields.SHORT_VAL;
import static org.burstsys.gen.thrift.api.client.BTDatum._Fields.SHORT_VECTOR;
import static org.burstsys.gen.thrift.api.client.BTDatum._Fields.STRING_BOOL_MAP;
import static org.burstsys.gen.thrift.api.client.BTDatum._Fields.STRING_INT_MAP;
import static org.burstsys.gen.thrift.api.client.BTDatum._Fields.STRING_LONG_MAP;
import static org.burstsys.gen.thrift.api.client.BTDatum._Fields.STRING_STRING_MAP;
import static org.burstsys.gen.thrift.api.client.BTDatum._Fields.STRING_VAL;
import static org.burstsys.gen.thrift.api.client.BTDatum._Fields.STRING_VECTOR;

public abstract class BParameter {
    public static BoolVal boolVal(String name, Boolean value) {
        return new BoolVal(name, value);
    }
    public static ByteVal byteVal(String name, Byte value) {
        return new ByteVal(name, value);
    }
    public static ShortVal shortVal(String name, Short value) {
        return new ShortVal(name, value);
    }
    public static IntVal intVal(String name, Integer value) {
        return new IntVal(name, value);
    }
    public static LongVal longVal(String name, Long value) {
        return new LongVal(name, value);
    }
    public static DoubleVal doubleVal(String name, Double value) {
        return new DoubleVal(name, value);
    }
    public static StringVal stringVal(String name, String value) {
        return new StringVal(name, value);
    }
    public static BoolVector boolVector(String name, List<Boolean> value) {
        return new BoolVector(name, value);
    }
    public static ByteVector byteVector(String name, List<Byte> value) {
        return new ByteVector(name, value);
    }
    public static ShortVector shortVector(String name, List<Short> value) {
        return new ShortVector(name, value);
    }
    public static IntVector intVector(String name, List<Integer> value) {
        return new IntVector(name, value);
    }
    public static LongVector longVector(String name, List<Long> value) {
        return new LongVector(name, value);
    }
    public static DoubleVector doubleVector(String name, List<Double> value) {
        return new DoubleVector(name, value);
    }
    public static StringVector stringVector(String name, List<String> value) {
        return new StringVector(name, value);
    }
    public static StringBoolMap stringBoolMap(String name, Map<String, Boolean> value) {
        return new StringBoolMap(name, value);
    }
    public static StringIntMap stringIntMap(String name, Map<String, Integer> value) {
        return new StringIntMap(name, value);
    }
    public static StringLongMap stringLongMap(String name, Map<String, Long> value) {
        return new StringLongMap(name, value);
    }
    public static StringStringMap stringStringMap(String name, Map<String, String> value) {
        return new StringStringMap(name, value);
    }

    protected final String name;
    protected final BTDatum datum;
    protected final boolean isNull;

    protected BParameter(String name, BTDatum datum, boolean isNull) {
        this.name = name;
        this.datum = datum;
        this.isNull = isNull;
    }

    protected abstract void setTypeAndFormat(BTParameter parameter);

    public BTParameter toThrift() {
        BTParameter parameter = new BTParameter();
        parameter.setName(name);
        parameter.setDatum(datum);
        parameter.setIsNull(isNull);
        setTypeAndFormat(parameter);
        return parameter;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BParameter{");
        sb.append("name='").append(name).append('\'');
        sb.append(", datum=").append(datum);
        sb.append('}');
        return sb.toString();
    }

    /**
     * BoolVal is a parameter of type Bool
     */
    public static class BoolVal extends BParameter {
        public BoolVal(String name, Boolean value) {
            super(name, new BTDatum(BOOL_VAL, value), value == null);
        }

        @Override
        protected void setTypeAndFormat(BTParameter parameter) {
            parameter.setFormat(BTDataFormat.Scalar).setPrimaryType(BTDataType.BoolType);
        }
    }

    /**
     * ByteVal is a parameter of type Byte
     */
    public static class ByteVal extends BParameter {
        public ByteVal(String name, Byte value) {
            super(name, new BTDatum(BYTE_VAL, value), value == null);
        }

        @Override
        protected void setTypeAndFormat(BTParameter parameter) {
            parameter.setFormat(BTDataFormat.Scalar).setPrimaryType(BTDataType.ByteType);
        }
    }

    /**
     * ShortVal is a parameter of type Short
     */
    public static class ShortVal extends BParameter {
        public ShortVal(String name, Short value) {
            super(name, new BTDatum(SHORT_VAL, value), value == null);
        }

        @Override
        protected void setTypeAndFormat(BTParameter parameter) {
            parameter.setFormat(BTDataFormat.Scalar).setPrimaryType(BTDataType.ShortType);
        }
    }

    /**
     * IntVal is a parameter of type Int
     */
    public static class IntVal extends BParameter {
        public IntVal(String name, Integer value) {
            super(name, new BTDatum(INT_VAL, value), value == null);
        }

        @Override
        protected void setTypeAndFormat(BTParameter parameter) {
            parameter.setFormat(BTDataFormat.Scalar).setPrimaryType(BTDataType.IntType);
        }
    }

    /**
     * LongVal is a parameter of type Long
     */
    public static class LongVal extends BParameter {
        public LongVal(String name, Long value) {
            super(name, new BTDatum(LONG_VAL, value), value == null);
        }

        @Override
        protected void setTypeAndFormat(BTParameter parameter) {
            parameter.setFormat(BTDataFormat.Scalar).setPrimaryType(BTDataType.LongType);
        }
    }

    /**
     * DoubleVal is a parameter of type Double
     */
    public static class DoubleVal extends BParameter {
        public DoubleVal(String name, Double value) {
            super(name, new BTDatum(DOUBLE_VAL, value), value == null);
        }

        @Override
        protected void setTypeAndFormat(BTParameter parameter) {
            parameter.setFormat(BTDataFormat.Scalar).setPrimaryType(BTDataType.DoubleType);
        }
    }

    /**
     * StringVal is a parameter of type String
     */
    public static class StringVal extends BParameter {
        public StringVal(String name, String value) {
            super(name, new BTDatum(STRING_VAL, value), value == null);
        }

        @Override
        protected void setTypeAndFormat(BTParameter parameter) {
            parameter.setFormat(BTDataFormat.Scalar).setPrimaryType(BTDataType.StringType);
        }
    }

    /**
     * BoolVector is a parameter of type List&lt;Boolean&gt;
     */
    public static class BoolVector extends BParameter {
        public BoolVector(String name, List<Boolean> value) {
            super(name, new BTDatum(BOOL_VECTOR, value), value == null);
        }

        @Override
        protected void setTypeAndFormat(BTParameter parameter) {
            parameter.setFormat(BTDataFormat.Vector).setPrimaryType(BTDataType.BoolType);
        }
    }

    /**
     * ByteVector is a parameter of type List&lt;Byte&gt;
     */
    public static class ByteVector extends BParameter {
        public ByteVector(String name, List<Byte> value) {
            super(name, new BTDatum(BYTE_VECTOR, value), value == null);
        }

        @Override
        protected void setTypeAndFormat(BTParameter parameter) {
            parameter.setFormat(BTDataFormat.Vector).setPrimaryType(BTDataType.ByteType);
        }
    }

    /**
     * ShortVector is a parameter of type List&lt;Short&gt;
     */
    public static class ShortVector extends BParameter {
        public ShortVector(String name, List<Short> value) {
            super(name, new BTDatum(SHORT_VECTOR, value), value == null);
        }

        @Override
        protected void setTypeAndFormat(BTParameter parameter) {
            parameter.setFormat(BTDataFormat.Vector).setPrimaryType(BTDataType.ShortType);
        }
    }

    /**
     * IntVector is a parameter of type List&lt;Int&gt;
     */
    public static class IntVector extends BParameter {
        public IntVector(String name, List<Integer> value) {
            super(name, new BTDatum(INT_VECTOR, value), value == null);
        }

        @Override
        protected void setTypeAndFormat(BTParameter parameter) {
            parameter.setFormat(BTDataFormat.Vector).setPrimaryType(BTDataType.IntType);
        }
    }

    /**
     * LongVector is a parameter of type List&lt;Long&gt;
     */
    public static class LongVector extends BParameter {
        public LongVector(String name, List<Long> value) {
            super(name, new BTDatum(LONG_VECTOR, value), value == null);
        }

        @Override
        protected void setTypeAndFormat(BTParameter parameter) {
            parameter.setFormat(BTDataFormat.Vector).setPrimaryType(BTDataType.LongType);
        }
    }

    /**
     * DoubleVector is a parameter of type List&lt;Double&gt;
     */
    public static class DoubleVector extends BParameter {
        public DoubleVector(String name, List<Double> value) {
            super(name, new BTDatum(DOUBLE_VECTOR, value), value == null);
        }

        @Override
        protected void setTypeAndFormat(BTParameter parameter) {
            parameter.setFormat(BTDataFormat.Vector).setPrimaryType(BTDataType.DoubleType);
        }
    }

    /**
     * StringVector is a parameter of type List&lt;String&gt;
     */
    public static class StringVector extends BParameter {
        public StringVector(String name, List<String> value) {
            super(name, new BTDatum(STRING_VECTOR, value), value == null);
        }

        @Override
        protected void setTypeAndFormat(BTParameter parameter) {
            parameter.setFormat(BTDataFormat.Vector).setPrimaryType(BTDataType.StringType);
        }
    }

    /**
     * StringBoolMap is a parameter of type Map&lt;String, Bool&gt;
     */
    public static class StringBoolMap extends BParameter {
        public StringBoolMap(String name, Map<String, Boolean> value) {
            super(name, new BTDatum(STRING_BOOL_MAP, value), value == null);
        }

        @Override
        protected void setTypeAndFormat(BTParameter parameter) {
            parameter.setFormat(BTDataFormat.Map)
                    .setPrimaryType(BTDataType.StringType).setSecondaryType(BTDataType.BoolType);
        }
    }

    /**
     * StringIntMap is a parameter of type Map&lt;String, Int&gt;
     */
    public static class StringIntMap extends BParameter {
        public StringIntMap(String name, Map<String, Integer> value) {
            super(name, new BTDatum(STRING_INT_MAP, value), value == null);
        }

        @Override
        protected void setTypeAndFormat(BTParameter parameter) {
            parameter.setFormat(BTDataFormat.Map)
                    .setPrimaryType(BTDataType.StringType).setSecondaryType(BTDataType.IntType);
        }
    }

    /**
     * StringLongMap is a parameter of type Map&lt;String, Long&gt;
     */
    public static class StringLongMap extends BParameter {
        public StringLongMap(String name, Map<String, Long> value) {
            super(name, new BTDatum(STRING_LONG_MAP, value), value == null);
        }

        @Override
        protected void setTypeAndFormat(BTParameter parameter) {
            parameter.setFormat(BTDataFormat.Map)
                    .setPrimaryType(BTDataType.StringType).setSecondaryType(BTDataType.LongType);
        }
    }

    /**
     * StringStringMap is a parameter of type Map&lt;String, String&gt;
     */
    public static class StringStringMap extends BParameter {
        public StringStringMap(String name, Map<String, String> value) {
            super(name, new BTDatum(STRING_STRING_MAP, value), value == null);
        }

        @Override
        protected void setTypeAndFormat(BTParameter parameter) {
            parameter.setFormat(BTDataFormat.Map)
                    .setPrimaryType(BTDataType.StringType).setSecondaryType(BTDataType.StringType);
        }
    }

}
