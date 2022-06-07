/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.util;

import org.burstsys.gen.thrift.api.client.BTDatum;
import org.burstsys.gen.thrift.api.client.query.BTParameter;

import java.util.function.Function;

import static org.burstsys.gen.thrift.api.client.BTDataFormat.Scalar;
import static org.burstsys.gen.thrift.api.client.BTDataType.BoolType;
import static org.burstsys.gen.thrift.api.client.BTDataType.ByteType;
import static org.burstsys.gen.thrift.api.client.BTDataType.DoubleType;
import static org.burstsys.gen.thrift.api.client.BTDataType.IntType;
import static org.burstsys.gen.thrift.api.client.BTDataType.LongType;
import static org.burstsys.gen.thrift.api.client.BTDataType.ShortType;
import static org.burstsys.gen.thrift.api.client.BTDataType.StringType;

public final class ParameterBuilder {
    private ParameterBuilder() {
    }

    private static <T> BTDatum datum(T value, Function<T, BTDatum> builder) {
        return value == null ? new BTDatum() : builder.apply(value);
    }

    public static BTParameter build(String name, Boolean value) {
        return new BTParameter(name, Scalar, BoolType, datum(value, BTDatum::boolVal), value == null);
    }

    public static BTParameter build(String name, Byte value) {
        return new BTParameter(name, Scalar, ByteType, datum(value, BTDatum::byteVal), value == null);
    }

    public static BTParameter build(String name, Short value) {
        return new BTParameter(name, Scalar, ShortType, datum(value, BTDatum::shortVal), value == null);
    }

    public static BTParameter build(String name, Integer value) {
        return new BTParameter(name, Scalar, IntType, datum(value, BTDatum::intVal), value == null);
    }

    public static BTParameter build(String name, Long value) {
        return new BTParameter(name, Scalar, LongType, datum(value, BTDatum::longVal), value == null);
    }

    public static BTParameter build(String name, Double value) {
        return new BTParameter(name, Scalar, DoubleType, datum(value, BTDatum::doubleVal), value == null);
    }

    public static BTParameter build(String name, String value) {
        return new BTParameter(name, Scalar, StringType, datum(value, BTDatum::stringVal), value == null);
    }
}
