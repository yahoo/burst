/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.model;

/**
 * Todo
 */
public enum SchemaClassifierType {

    key,
    ordinal;

    public static SchemaClassifierType parse(String text) {
        switch (text) {
            case "key":
                return key;
            case "ordinal":
                return ordinal;
            default:
                throw new RuntimeException("bad classifier " + text);
        }
    }

    public String asString() {
       return this.toString();
    }


}
