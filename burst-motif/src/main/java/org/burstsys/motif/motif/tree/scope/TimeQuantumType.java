/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.scope;

import org.burstsys.motif.common.MotifGenerator;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.ParseException;

import static java.lang.String.format;

public enum TimeQuantumType implements MotifGenerator {
    SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS;


    public static TimeQuantumType parse(NodeLocation location, String text) {
        String ucText = text.toUpperCase();
        try {
            return TimeQuantumType.valueOf(ucText);
        } catch (IllegalArgumentException e) {
            throw new ParseException(location, format("unknown time quantum '%s'", ucText));
        }
    }

    @Override
    public String generateMotif(int level) {
        return this.toString();
    }
}
