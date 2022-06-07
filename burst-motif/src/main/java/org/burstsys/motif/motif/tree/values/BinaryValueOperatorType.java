/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.values;

import org.burstsys.motif.common.MotifGenerator;

public enum BinaryValueOperatorType implements MotifGenerator {
    ADD("+"), SUBTRACT("-"), MULTIPLY("*"), DIVIDE("/"), MODULO("%");

    String motif;

    @Override
    public String generateMotif(int level) {
        return motif;
    }

    BinaryValueOperatorType(String motif) {
        this.motif = motif;
    }

    public static BinaryValueOperatorType parse(String text) {
        switch (text) {
            case "+":
                return ADD;
            case "-":
                return SUBTRACT;
            case "*":
                return MULTIPLY;
            case "/":
                return DIVIDE;
            case "%":
                return MODULO;
            default:
                throw new RuntimeException("");
        }
    }
}
