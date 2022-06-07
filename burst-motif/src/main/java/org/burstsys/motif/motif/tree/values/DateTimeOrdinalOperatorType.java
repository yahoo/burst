/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.values;

import org.burstsys.motif.common.MotifGenerator;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.constant.NumberConstant;

import static java.lang.String.format;

public enum DateTimeOrdinalOperatorType implements MotifGenerator {

    SECONDOFMINUTE,
    MINUTEOFHOUR,
    HOUROFDAY,
    DAYOFWEEK,
    DAYOFMONTH,
    DAYOFYEAR,
    WEEKOFYEAR,
    MONTHOFYEAR,
    THEYEAR;

    @Override
    public String generateMotif(int level) {
        return this.toString();
    }

    public static DateTimeOrdinalOperatorType parse(NodeLocation location, String text) {
        try {
            return DateTimeOrdinalOperatorType.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ParseException(location, format("unknown time operator '%s' ", text));
        }
    }

    public NumberConstant evaluate(NodeLocation location, Long expr, Long timezone) {
        switch (this) {
            case SECONDOFMINUTE:
                return null;
            case MINUTEOFHOUR:
                return null;
            case HOUROFDAY:
                return null;
            case DAYOFWEEK:
                return null;
            case DAYOFMONTH:
                return null;
            case DAYOFYEAR:
                return null;
            case WEEKOFYEAR:
                return null;
            case MONTHOFYEAR:
                return null;
            case THEYEAR:
                return null;
            default:
                throw new ParseException(location, format("can't eval time operator '%s' ", this));
        }
    }
}
