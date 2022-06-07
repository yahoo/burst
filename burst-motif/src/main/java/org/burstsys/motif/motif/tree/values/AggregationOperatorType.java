/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.values;

import org.burstsys.motif.common.MotifGenerator;

public enum AggregationOperatorType implements MotifGenerator {
    SUM("SUM"), COUNT("COUNT"), MIN("MIN"), MAX("MAX"), UNIQUE("UNIQUE"), TOP("TOP");

    String motif;

    @Override
    public String generateMotif(int level) {
        return motif;
    }

    AggregationOperatorType(String motif) {
        this.motif = motif;
    }

    public static AggregationOperatorType parse(String text) {
        switch (text.toLowerCase()) {
            case "sum":
                return  SUM;
            case "count":
                return COUNT;
            case "min":
                return MIN;
            case "max":
                return  MAX;
            case "uniques":
            case "unique":
                return  UNIQUE;
            case "top":
                return  TOP;
            default:
                return null;
        }
    }
}
