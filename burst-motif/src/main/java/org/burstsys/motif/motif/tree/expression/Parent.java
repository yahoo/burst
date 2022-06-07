/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.expression;

import java.util.List;

/**
 * The child interface allows tree walking routines to move through and modify a motif expression tree without having to
 * know the specific fields that refer to interesting children of this node
 */
public interface Parent {
    /**
     * Return an ordered list of children for this node.  The children are always returned in the same order
     * @return list of the children in this node.  The list will be empty if this is a leaf
     */
    List<Expression> getChildren();

    /**
     * The number of children for this node
     * @return count of children
     */
    int childCount();

    /**
     * Returned a specific child in the child list without actually returning the entire list.
     * @param index index into the child list
     * @return the expression node for the child
     */
    Expression getChild(int index);

    /**
     * Change the value of a specific child for this
     * @param index index into the child list
     * @param value the new value for the child
     * @return the value being replaced
     */
    Expression setChild(int index, Expression value);

    /**
     * Walk the expression nodes of a tree and give
     * each node to a checker function
     * @param checker the checker
     */
    void walkTree(NodeWalker checker);


    /**
     * Node checker interface
     */
    interface NodeWalker {
       void check(Expression node);
    }
}
