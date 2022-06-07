/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.common;

public interface Node {

    /**
     * explain this expression as a string
     *
     * @return
     */
    String explain();

    /**
     * explain where the string is offset by a level
     *
     * @param level
     * @return
     */
    String explain(int level);


    String exportAsJson();


    NodeType getNodeType();

}
