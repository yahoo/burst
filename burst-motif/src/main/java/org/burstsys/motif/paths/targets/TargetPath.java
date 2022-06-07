/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.paths.targets;

import org.burstsys.motif.motif.tree.eql.queries.Target;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.paths.UniversalPathBase;

/**
 * Help with paths
 */
public class TargetPath extends UniversalPathBase {
    protected Target target;

    protected TargetPath(Target t) {
        assert(t != null);
        this.target = t;
    }

    @Override
    public String getPathAsString() {
        return target.getName();
    }

    @Override
    public Path getEnclosingStructure() {
        return this;
    }

    @Override
    public Path getParentStructure() {
        return this;
    }

    @Override
    public boolean sameHigher(Path p) {
        return sameOnPath(p);
    }

    @Override
    public boolean sameLower(Path p) {
        return  false;
    }

    public boolean isRoot() {
        return  false;
    }

    public boolean sameOnPath(Path p) {
        if (p.isRoot())
            // the schema root is always on path for funnels
            return true;
        if (!(p instanceof TargetPath))
            return false;
        else return this.target.getName().equals(((TargetPath) p).target.getName());
    }


    public Target getTarget() {
        return target;
    }

    @Override
    public String toString() {
       return getPathAsString();
    }

    // Static Helpers
    @SuppressWarnings("unused")
    static public TargetPath formPath(Target target) {
        assert(target != null);
        // steps field reference
        return new TargetPath(target);
    }

}
