/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.common;

import org.burstsys.motif.motif.tree.constant.NumberConstant;
import org.burstsys.motif.motif.tree.constant.context.ByteConstantContext;
import org.burstsys.motif.motif.tree.constant.context.IntegerConstantContext;
import org.burstsys.motif.motif.tree.constant.context.LongConstantContext;
import org.burstsys.motif.motif.tree.constant.context.ShortConstantContext;

import static java.lang.String.format;

public enum DataType implements MotifGenerator {

    BOOLEAN, BYTE, SHORT, INTEGER, LONG, DOUBLE, STRING, DATETIME, NULL, STRUCTURE;

    @Override
    public String generateMotif(int level) {
        return this.toString();
    }

    public boolean notNumeric() {
        switch (this) {
            case NULL:
            case BOOLEAN:
            case STRING:
                return true;
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
            case DATETIME:
            case DOUBLE:
                return false;
            default:
                throw new ParseException(format("datatype '%s' is not understood", this.name()));
        }

    }

    public static DataType parse(String text) {
        if (text == null) return NULL;
        switch (text.toLowerCase()) {
            case "boolean":
                return BOOLEAN;
            case "byte":
                return BYTE;
            case "short":
                return SHORT;
            case "integer":
                return INTEGER;
            case "long":
                return LONG;
            case "double":
                return DOUBLE;
            case "string":
                return STRING;
            case "null":
                return NULL;
            case "datetime":
                return DATETIME;
            default:
                throw new RuntimeException("bad datatype " + text);
        }
    }
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Boolean canCoerce(String text) {
        try {
            switch (this) {
                case BOOLEAN:
                    Boolean.parseBoolean(text);
                    return true;
                case BYTE:
                    Byte.decode(text);
                    return true;
                case SHORT:
                    Short.decode(text);
                    return true;
                case INTEGER:
                    Integer.decode(text);
                    return true;
                case LONG:
                case DATETIME:
                    Long.decode(text);
                    return true;
                case DOUBLE:
                    Double.parseDouble(text);
                    return true;
                case NULL:
                    return text.toLowerCase().equals("null");
                case STRING:
                    return true;
                default:
                    throw new ParseException(format("'%s' could not be converted to a %s", text, this));
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public Object coerce(String text) {
        switch (this) {
            case BOOLEAN:
                return Boolean.parseBoolean(text);
            case BYTE:
                return Byte.decode(text);
            case SHORT:
                return Short.decode(text);
            case INTEGER:
                return Integer.decode(text);
            case DATETIME:
            case LONG:
                return Long.decode(text);
            case DOUBLE:
                return Double.parseDouble(text);
            case STRING:
                return text;
            case NULL:
                return null;
            default:
                throw new ParseException(format("'%s' could not be converted to a %s", text, this));
        }
    }

    public static NumberConstant findSmallestNumberConstant(NodeGlobal global, NodeLocation location, String value) {
        if (BYTE.canCoerce(value))
            return new ByteConstantContext(global, location, (Byte) BYTE.coerce(value));
        if (SHORT.canCoerce(value))
            return new ShortConstantContext(global, location, (Short) SHORT.coerce(value));
        if (INTEGER.canCoerce(value))
            return new IntegerConstantContext(global, location, (Integer) INTEGER.coerce(value));
        if (LONG.canCoerce(value))
            return new LongConstantContext(global, location, (Long) LONG.coerce(value));
        throw new ParseException(format("'%s' no integer rep", value));
    }

    /*  Comparison helper routines */
    public static DataType findCommonDtype(DataType one, DataType two) {
        switch (one) {
            case BYTE:
                switch (two) {
                    case NULL:
                        return NULL;
                    case BYTE:
                        return BYTE;
                    case SHORT:
                        return SHORT;
                    case INTEGER:
                        return INTEGER;
                    case LONG:
                        return LONG;
                    case DATETIME:
                        return LONG;
                    case DOUBLE:
                        return DOUBLE;
                    case BOOLEAN:
                    case STRING:
                    default:
                }
                break;
            case SHORT:
                switch (two) {
                    case NULL:
                        return NULL;
                    case BYTE:
                        return SHORT;
                    case SHORT:
                        return SHORT;
                    case INTEGER:
                        return INTEGER;
                    case LONG:
                        return LONG;
                    case DATETIME:
                        return LONG;
                    case DOUBLE:
                        return DOUBLE;
                    case BOOLEAN:
                    case STRING:
                    default:
                }
                break;
            case INTEGER:
                switch (two) {
                    case NULL:
                        return NULL;
                    case BYTE:
                        return INTEGER;
                    case SHORT:
                        return INTEGER;
                    case INTEGER:
                        return INTEGER;
                    case LONG:
                        return LONG;
                    case DATETIME:
                        return LONG;
                    case DOUBLE:
                        return DOUBLE;
                    case BOOLEAN:
                    case STRING:
                    default:
                }
                break;
            case LONG:
                switch (two) {
                    case NULL:
                        return NULL;
                    case BYTE:
                        return LONG;
                    case SHORT:
                        return LONG;
                    case INTEGER:
                        return LONG;
                    case LONG:
                        return LONG;
                    case DATETIME:
                        return LONG;
                    case DOUBLE:
                        return DOUBLE;
                    case BOOLEAN:
                    case STRING:
                    default:
                }
                break;
            case DATETIME:
                switch (two) {
                    case NULL:
                        return NULL;
                    case BYTE:
                        return LONG;
                    case SHORT:
                        return LONG;
                    case INTEGER:
                        return LONG;
                    case LONG:
                        return LONG;
                    case DATETIME:
                        return LONG;
                    case DOUBLE:
                        return DOUBLE;
                    case BOOLEAN:
                    case STRING:
                }
                break;
            case DOUBLE:
                switch (two) {
                    case NULL:
                        return NULL;
                    case BYTE:
                        return DOUBLE;
                    case SHORT:
                        return DOUBLE;
                    case INTEGER:
                        return DOUBLE;
                    case LONG:
                        return DOUBLE;
                    case DATETIME:
                        return DOUBLE;
                    case DOUBLE:
                        return DOUBLE;
                    case BOOLEAN:
                    case STRING:
                }
                break;
            case BOOLEAN:
                switch (two) {
                    case NULL:
                        return NULL;
                    case BOOLEAN:
                        return BOOLEAN;
                    case BYTE:
                    case SHORT:
                    case INTEGER:
                    case LONG:
                    case DATETIME:
                    case DOUBLE:
                    case STRING:
                    default:
                }
                break;
            case STRING:
                switch (two) {
                    case NULL:
                        return NULL;
                    case STRING:
                        return STRING;
                    case BOOLEAN:
                    case BYTE:
                    case SHORT:
                    case INTEGER:
                    case LONG:
                    case DATETIME:
                    case DOUBLE:
                    default:
                }
                break;
            case STRUCTURE:
                switch (two) {
                    case NULL:
                        return NULL;
                    case STRUCTURE:
                        throw new ParseException("structures are not compatible");
                    case BOOLEAN:
                    case BYTE:
                    case SHORT:
                    case INTEGER:
                    case LONG:
                    case DATETIME:
                    case DOUBLE:
                    default:
                }
                break;
            case NULL:
                return NULL;
            default:
                return null;

        }
        return null;
    }

    public static boolean hasCommonDtype(DataType one, DataType two) {
        return findCommonDtype(one, two) != null;
    }
}
