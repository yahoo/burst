/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.constant;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.Node;
import org.burstsys.motif.motif.tree.values.BinaryValueComparisonOperator;
import org.burstsys.motif.motif.tree.values.ValueExpression;

/**
 * A value expression that has simple representation in a primitive datatyped value
 */
public interface Constant extends Node, ValueExpression {

    /**
     * get the underlying primitive value as a java object
     *
     * @return
     */
    Object getDataValue();

    /**
     * compare this constant to another constant. Return a {@link BooleanConstant} with the result.
     *
     * @param comparisonType
     * @param rightConstant
     * @return
     */
    BooleanConstant binaryBooleanCompare(BinaryValueComparisonOperator comparisonType, Constant rightConstant);

    /**
     * true if this constant is byte, short, integer, long, or double
     *
     * @return
     */
    boolean isNumber();

    /**
     * true if this constant is a boolean
     *
     * @return
     */
    boolean isBoolean();

    /**
     * true if this constant is a null
     *
     * @return
     */
    boolean isNull();

    /**
     * true if this constant is a string
     *
     * @return
     */
    boolean isString();

    /**
     * cast this constant to another type of constant. Throws a {@link scala.reflect.macros.ParseException}
     * if the cast fails
     *
     * @param dataType
     * @return
     */
    Constant castTo(DataType dataType);

    /**
     * return a primitive string if possible. Throws a {@link scala.reflect.macros.ParseException}
     * if not
     *
     * @return
     */
    String asString();

    /**
     * return a primitive string if possible. Throws a {@link scala.reflect.macros.ParseException}
     * if not
     *
     * @return
     */
    boolean asBoolean();

    /**
     * checks to see if this can be converted to a null. Throws a {@link scala.reflect.macros.ParseException}
     * if not
     *
     * @return
     */
    void asNull();

    /**
     * return a primitive byte if possible. Throws a {@link scala.reflect.macros.ParseException}
     * if not
     *
     * @return
     */
    byte asByte();

    /**
     * return a primitive short if possible. Throws a{@link scala.reflect.macros.ParseException}
     * if not
     *
     * @return
     */
    short asShort();

    /**
     * return a primitive integer if possible. Throws a {@link scala.reflect.macros.ParseException}
     * if not
     *
     * @return
     */
    int asInteger();

    /**
     * return a primitive long if possible. Throws a {@link scala.reflect.macros.ParseException}
     * if not
     *
     * @return
     */
    long asLong();

    /**
     * return a primitive long if possible. Throws a {@link scala.reflect.macros.ParseException}
     * if not
     * @return
     */
    long asDatetime();

    /**
     * return a primitive double if possible. Throws a {@link scala.reflect.macros.ParseException}
     * if not
     *
     * @return
     */
    double asDouble();

}
