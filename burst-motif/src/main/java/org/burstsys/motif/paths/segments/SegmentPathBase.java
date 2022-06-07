/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.paths.segments;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.eql.segments.Segment;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.paths.UniversalPathBase;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

/**
 * Help with paths
 */
abstract public class SegmentPathBase extends UniversalPathBase {
    protected Segment segment;

    protected SegmentPathBase(Segment s) {
        assert(s != null);
        this.segment = s;
    }

    abstract public String getPathAsString();

    public boolean isRoot() {
        // conversion funnels can be evaluated at the root
        return true;
    }

    abstract public Path getEnclosingStructure();

    abstract public Path getParentStructure();

    public boolean sameOnPath(Path p) {
        if (p.isRoot())
            // the schema root is always on path for funnels
            return true;
        if (!(p instanceof SegmentPathBase))
            return false;
        else return this.segment.getName().equals(((SegmentPathBase) p).segment.getName());
    }

    abstract public boolean sameHigher(Path p);

    abstract public boolean sameLower(Path p);

    public Segment getSegment() {
        return segment;
    }

    @Override
    public String toString() {
       return getPathAsString();
    }

    // Static Helpers

    /**
     * Given a schema and a path string form a path object.  The path can have map and index operations which
     * will be ignored.
     *
     */
    static public SegmentPathBase formPath(Segment segment, String fullPath) {
        assert(segment != null);
        List<String> components = Arrays.asList(fullPath.split("\\."));

        // check the head syntax is correct
        if (components.size() < 2 || components.size() > 4)
            throw new ParseException(format("segment path '%s' is malformed for segment '%s'", fullPath, segment.getName()));
        if (components.size() < 3 && !components.get(1).equals("members"))
            throw new ParseException(format("segment path '%s' must refer to `members` component for segment '%s'", fullPath, segment.getName()));

        if (components.size() == 2)
            return new SegmentMembersPath(segment);
        else if (components.size() == 3)
            // paths field reference
            return SegmentMembersFieldPath.formPath(segment, components.get(2));

        throw new ParseException(format("invalid segment path '%s'", segment.getName()));
    }

}
