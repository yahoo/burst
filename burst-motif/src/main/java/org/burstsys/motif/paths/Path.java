/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.paths;

import org.burstsys.motif.motif.tree.expression.Expression;

import java.util.Arrays;
import java.util.List;

public interface Path {
    String getPathAsString();

    /**
     * This path is the root of the schema
     */
    boolean isRoot() ;

    /**
     * Return the path that represents a visit in the schema.  It could be itself
     */
    Path getEnclosingStructure();

    /**
     * Return the parent visit of this path, or itself if this path is the root.
     */
    Path getParentStructure();

    /**
     * Argument is on same path as this path
     */
    boolean notOnPath(Path p);

    /**
     * This path is strictly higher (ancestor) in the DFS of the schema than the argument.
     */
    boolean higher(Path p);

    /**
     * This path is lower (descendant) or equal in the DFS of the schema than the argument.
     */
    boolean lower(Path p);

    static Path lowest(Expression... expressions) {
        List<? extends Expression> l = Arrays.asList(expressions);
        return lowest(l);
    }

    static Path lowest(List<? extends Expression> expressions) {
        return lowest(expressions.stream().map(Expression::getLowestEvaluationPoint).toArray(Path[]::new));
    }

    static Path lowest(Path... paths) {
        Path low = null;

        for (Path p: paths) {
            if (p == null)
                continue;
            if (low == null)
                low = p.getEnclosingStructure();
            else if (low.higher(p))
                low = p;
            else if (!low.lower(p))
                //  this is neither higher nor lower so it's not on axis and we give up
                return null;
        }
        return low;
    }

    static Path getRoot(Path p) {
        if (p == null)
            throw new RuntimeException("Null path");
        while (!p.isRoot())
            p = p.getParentStructure();
        return p;
    }
}
