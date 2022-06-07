/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.values;

import org.burstsys.motif.common.MotifGenerator;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.constant.NumberConstant;
import org.burstsys.motif.motif.tree.constant.context.LongConstantContext;

import static java.lang.String.format;

/**
 * Conversions of integer values to ms time values
 */
public enum DateTimeConversionOperatorType implements MotifGenerator {

    YEARS,
    MONTHS,
    WEEKS,
    DAYS,
    HOURS,
    MINUTES,
    SECONDS;

    @Override
    public String generateMotif(int level) {
        return this.toString();
    }

    public static DateTimeConversionOperatorType parse(NodeGlobal global, NodeLocation location, String text) {
        try {
            return DateTimeConversionOperatorType.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ParseException(location, format("unknown time quantum '%s' ", text));
        }
    }

    /**
     * TODO this is approx - do we need to do better?
     * @param location
     * @param value
     * @return
     */
    public NumberConstant evaluate(NodeGlobal global, NodeLocation location, Long value) {
        long result;
        switch (this) {
            case YEARS:
                result = value * 365 * 24 * 60 * 60 * 1000; // years, days, hours, minutes, seconds, milliseconds
                break;
            case MONTHS:
                result = value * 365/12 * 24 * 60 * 60 * 1000; // months, days, hours, minutes, seconds, milliseconds
                break;
            case WEEKS:
                result = value * 7 * 24 * 60 * 60 * 1000; // weeks, days, hours, minutes, seconds, milliseconds
                break;
            case DAYS:
                result = value * 24 * 60 * 60 * 1000; // days, hours, minutes, seconds, milliseconds
                break;
            case HOURS:
                result = value * 60 * 60 * 1000; // hours, minutes, seconds, milliseconds
                break;
            case MINUTES:
                result = value * 60 * 1000; // minutes, seconds, milliseconds
                break;
            case SECONDS:
                result = value *1000; // seconds, milliseconds
                break;
            default:
                throw new ParseException(location, format("can't eval time quantum '%s' ", this));
        }
        return new LongConstantContext(global, location, result);
    }

}
