/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.values;

import org.burstsys.motif.common.MotifGenerator;

/**
 * all the comparison operators
 */
public enum BinaryValueComparisonOperator implements MotifGenerator {
    EQ("=="), NEQ("!="), LT("<"), LTE("<="), GT(">"), GTE(">=");


    String motif;

    @Override
    public String generateMotif(int level) {
        return motif;
    }

    BinaryValueComparisonOperator(String motif) {
        this.motif = motif;
    }

    public static BinaryValueComparisonOperator parse(String text) {
        switch (text) {
            case "==":
                return EQ;
            case "!=":
                return NEQ;
            case "<>":
                return NEQ;
            case "<":
                return LT;
            case "<=":
                return LTE;
            case ">":
                return GT;
            case ">=":
                return GTE;
            default:
                throw new RuntimeException("");
        }
    }
}
