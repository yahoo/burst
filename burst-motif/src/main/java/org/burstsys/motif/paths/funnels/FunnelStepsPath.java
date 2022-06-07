/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.paths.funnels;

import org.burstsys.motif.motif.tree.eql.funnels.Funnel;
import org.burstsys.motif.paths.Path;

public class FunnelStepsPath extends FunnelPathBase {

    final protected FunnelPathsPath parent;

    protected FunnelStepsPath(Funnel f) {
       super(f);
       parent = new FunnelPathsPath(f);
    }

    @Override
    public String getPathAsString() {
        return funnel.getName() + ".paths.steps";
    }

    @Override
    public Path getEnclosingStructure() {
        return this;
    }

    @Override
    public Path getParentStructure() {
        return parent;
    }

    @Override
    public boolean sameHigher(Path p) {
        // nothing can be below us right now
        return false;
    }

    @Override
    public boolean sameLower(Path p) {
        // we are below paths
        return p.isRoot() || (sameOnPath(p) && p instanceof FunnelStepsPath);
    }
}
