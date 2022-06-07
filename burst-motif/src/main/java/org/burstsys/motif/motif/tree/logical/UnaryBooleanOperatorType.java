/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.logical;

import org.burstsys.motif.common.MotifGenerator;

public enum UnaryBooleanOperatorType implements MotifGenerator {
    NOT("NOT");


    String motif;

    @Override
    public String generateMotif(int level) {
        return motif;
    }

    UnaryBooleanOperatorType(String motif) {
        this.motif = motif;
    }

    public static UnaryBooleanOperatorType parse(String text) {
        switch (text.toLowerCase()) {
            case "not":
                return NOT;
            default:
                throw new RuntimeException("");
        }
    }
}
