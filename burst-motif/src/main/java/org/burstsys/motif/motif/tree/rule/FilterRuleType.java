/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.rule;

import org.burstsys.motif.common.MotifGenerator;

/**
 *
 */
public enum FilterRuleType implements MotifGenerator {

    INCLUDE("INCLUDE"), EXCLUDE("EXCLUDE"), SAMPLE("SAMPLE"), PRESAMPLE("PRESAMPLE"), POSTSAMPLE("POSTSAMPLE");

    String motif;

    @Override
    public String generateMotif(int level) {
        return motif;
    }

    FilterRuleType(String motif) {
        this.motif = motif;
    }

    public static FilterRuleType parse(String text) {
        switch (text.toLowerCase()) {
            case "include":
                return INCLUDE;
            case "exclude":
                return EXCLUDE;
            case "sample":
                return SAMPLE;
            case "presample":
                return PRESAMPLE;
            case "postsample":
                return POSTSAMPLE;
            default:
                throw new RuntimeException("");
        }
    }

}
