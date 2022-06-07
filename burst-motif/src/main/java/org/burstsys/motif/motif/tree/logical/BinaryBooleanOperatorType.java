/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.logical;

import org.burstsys.motif.common.MotifGenerator;

public enum BinaryBooleanOperatorType implements MotifGenerator {
    AND("&&"), OR("||");

    String motif;

    @Override
    public String generateMotif(int level) {
        return motif;
    }

    BinaryBooleanOperatorType(String motif) {
        this.motif = motif;
    }

    public static BinaryBooleanOperatorType parse(String text) {
        switch (text.toLowerCase()) {
            case "or":
                return OR;
            case "||":
                return OR;
            case "and":
                return AND;
            case "&&":
                return AND;
            default:
                throw new RuntimeException("");
        }
    }

    public boolean evaluate(boolean left, boolean right) {
        if (this == AND) {
            return left && right;
        } else {
            return left || right;
        }
    }

}
