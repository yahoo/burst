/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.util;

import org.burstsys.gen.thrift.api.client.BTDataType;
import org.burstsys.gen.thrift.api.client.BTDatum;

import java.util.List;

public class DatumValue {
    private DatumValue() {
    }

    public static Object extractVal(BTDataType dType, BTDatum datum) {
        switch (dType) {
            case BoolType:
                return datum.getBoolVal();
            case ByteType:
                return datum.getByteVal();
            case ShortType:
                return datum.getShortVal();
            case IntType:
                return datum.getIntVal();
            case LongType:
                return datum.getLongVal();
            case DoubleType:
                return datum.getDoubleVal();
            case StringType:
                return datum.getStringVal();
        }
        throw new IllegalStateException("Unknown datatype '" + dType + "'");
    }

    public static List extractVector(BTDataType dType, BTDatum datum) {
        switch (dType) {
            case BoolType:
                return datum.getBoolVector();
            case ByteType:
                return datum.getByteVector();
            case ShortType:
                return datum.getShortVector();
            case IntType:
                return datum.getIntVector();
            case LongType:
                return datum.getLongVector();
            case DoubleType:
                return datum.getDoubleVector();
            case StringType:
                return datum.getStringVector();
        }
        throw new IllegalStateException("Unknown datatype '" + dType + "'");
    }

}
