/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.logical;

import org.burstsys.motif.common.MotifGenerator;

public enum MembershipTestOperatorType implements MotifGenerator {
    IN("IN"), NOT_IN("NOT IN");

    String motif;

    @Override
    public String generateMotif(int level) {
        return motif;
    }

    MembershipTestOperatorType(String motif) {
        this.motif = motif;
    }

    public static MembershipTestOperatorType parse(String text) {
        switch (text.toLowerCase()) {
            case "in":
                return IN;
            case "notin":
                return NOT_IN;
            default:
                throw new RuntimeException("");
        }
    }
}
