/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.logical;

import org.burstsys.motif.common.MotifGenerator;

public enum BoundsTestOperatorType implements MotifGenerator {
    BETWEEN("BETWEEN"), NOT_BETWEEN("NOT BETWEEN");

    String motif;

    @Override
    public String generateMotif(int level) {
        return motif;
    }

    BoundsTestOperatorType(String motif) {
        this.motif = motif;
    }

    public static BoundsTestOperatorType parse(String text) {
        switch (text.toLowerCase()) {
            case "between":
                return BETWEEN;
            case "notbetween":
                return NOT_BETWEEN;
            default:
                throw new RuntimeException("");
        }
    }
}
