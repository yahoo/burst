/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.expression;

import org.burstsys.motif.common.MotifGenerator;
import org.burstsys.motif.common.Node;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.paths.Path;

import java.util.Stack;

/**
 * The core type of any motif statment
 */
public interface Evaluation extends Node, MotifGenerator {
    /**
     * Apply any static optimizations. Implementing classes are expected to return an object with the same type
     * or a subtype of themselves. i.e.
     * <pre>
     *     For all T where T implements Evaluation:
     *     T#optimize -> T' where T' == T or T' is a subtype of T
     * </pre>
     */
    Evaluation optimize(PathSymbols pathSymbols);

    /**
     * bind this node to a schema
     */
    void bind(PathSymbols pathSymbols, Stack<Evaluation> stack);

    /**
     * validate this node against a schema
     */
    void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack);
}
