/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.logical;

import org.burstsys.motif.common.MotifGenerator;

public enum NullTestOperatorType implements MotifGenerator {
    IS_NULL("IS NULL"), IS_NOT_NULL("IS NOT NULL");

    String motif;

    @Override
    public String generateMotif(int level) {
        return motif;
    }

    NullTestOperatorType(String motif) {
        this.motif = motif;
    }

    public static NullTestOperatorType parse(String text) {
        switch (text.toLowerCase()) {
            case "isnull":
                return IS_NULL;
            case "isnotnull":
                return IS_NOT_NULL;
            default:
                throw new RuntimeException("");
        }
    }
}
