/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.paths.funnels;

import org.burstsys.motif.motif.tree.eql.funnels.Funnel;
import org.burstsys.motif.paths.Path;

public class FunnelPath extends FunnelPathBase {

    public FunnelPath(Funnel f) {
       super(f);
    }

    @Override
    public String getPathAsString() {
        return funnel.getName();
    }

    @Override
    public boolean isRoot() {
        return true;
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
        // we are above steps
        return sameOnPath(p) && (p instanceof FunnelPathsPath || p instanceof FunnelStepsPath);
    }

    @Override
    public boolean sameLower(Path p) {
        // nothing can be above us right now
        return p.isRoot() || (sameOnPath(p) && p instanceof FunnelPath);
    }
}
