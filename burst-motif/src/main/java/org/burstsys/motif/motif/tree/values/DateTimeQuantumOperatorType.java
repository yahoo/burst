/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.values;

import org.burstsys.motif.common.MotifGenerator;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.constant.NumberConstant;

import static java.lang.String.format;

public enum DateTimeQuantumOperatorType implements MotifGenerator {

    YEAR,
    HALF,
    QUARTER,
    MONTH,
    WEEK,
    DAY,
    HOUR,
    MINUTE,
    SECOND;

    @Override
    public String generateMotif(int level) {
        return this.toString();
    }

    public static DateTimeQuantumOperatorType parse(NodeLocation location, String text) {
        try {
            return DateTimeQuantumOperatorType.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ParseException(location, format("unknown time operator '%s' ", text));
        }
    }

    public NumberConstant evaluate(NodeLocation location, Long value, Long timezone) {
        switch (this) {
            case YEAR:
                return null;
            case HALF:
                return null;
            case QUARTER:
                return null;
            case MONTH:
                return null;
            case WEEK:
                return null;
            case DAY:
                return null;
            case HOUR:
                return null;
            case MINUTE:
                return null;
            case SECOND:
                return null;
            default:
                throw new ParseException(location, format("can't eval time operator '%s' ", this));
        }
    }
}
