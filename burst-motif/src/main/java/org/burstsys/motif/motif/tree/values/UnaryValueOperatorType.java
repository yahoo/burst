/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.values;

import org.burstsys.motif.common.MotifGenerator;

public enum UnaryValueOperatorType implements MotifGenerator {
    NORMAL(""), NEGATE("-");

    String motif;

    @Override
    public String generateMotif(int level) {
        return motif;
    }

    UnaryValueOperatorType(String motif) {
        this.motif = motif;
    }

    public static UnaryValueOperatorType parse(String text) {
        switch (text) {
            case "+":
                return NORMAL;
            case "-":
                return NEGATE;
            default:
                throw new RuntimeException("");
        }
    }
}
