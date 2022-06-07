/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.paths.segments;

import org.burstsys.motif.motif.tree.eql.segments.Segment;
import org.burstsys.motif.paths.Path;

public class SegmentMembersPath extends SegmentPathBase {

    public SegmentMembersPath(Segment t) {
       super(t);
    }

    @Override
    public String getPathAsString() {
        return segment.getName() + ".members";
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
        return sameOnPath(p) && p instanceof SegmentMembersPath;
    }

    @Override
    public boolean sameLower(Path p) {
        // nothing can be above us right now
        return p.isRoot() || (sameOnPath(p) && p instanceof SegmentMembersPath);
    }
}
