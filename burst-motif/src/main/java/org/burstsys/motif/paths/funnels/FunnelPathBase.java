/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.paths.funnels;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.eql.funnels.Funnel;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.paths.UniversalPathBase;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

/**
 * Help with paths
 */
abstract public class FunnelPathBase extends UniversalPathBase {
    protected Funnel funnel;

    protected FunnelPathBase(Funnel f) {
        assert(f != null);
        this.funnel = f;
    }

    abstract public String getPathAsString();

    public boolean isRoot() {
        return false;
    }

    abstract public org.burstsys.motif.paths.Path getEnclosingStructure();

    abstract public org.burstsys.motif.paths.Path getParentStructure();

    public boolean sameOnPath(Path p) {
        if (p.isRoot())
            // the schema root is always on path for funnels
            return true;
        if (!(p instanceof FunnelPathBase))
            return false;
        else return this.funnel.getName().equals(((FunnelPathBase) p).funnel.getName());
    }

    abstract public boolean sameHigher(Path p);

    abstract public boolean sameLower(Path p);

    public Funnel getFunnel() {
        return funnel;
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
    static public FunnelPathBase formPath(Funnel funnel, String fullPath) {
        assert(funnel != null);
        List<String> components = Arrays.asList(fullPath.split("\\."));

        // check the head syntax is correct
        if (components.size() < 1 )
            throw new ParseException(format("funnel path '%s' is malformed for funnel '%s'", fullPath, funnel.getName()));
        if (components.size() < 2)
            return new FunnelPath(funnel);
        if (!components.get(1).equals("paths"))
            throw new ParseException(format("funnel path '%s' must refer to a `paths` component", funnel.getName()));
        if (components.size() > 3 && !components.get(2).equals("steps"))
            throw new ParseException(format("funnel path '%s' must refer to a `steps` component", funnel.getName()));
        if (components.size() > 4)
            throw new ParseException(format("invalid funnel path '%s'", funnel.getName()));

        if (components.size() == 2) {
            // paths collection reference
            return new FunnelPathsPath(funnel);
        } else if (components.size() == 3 && components.get(2).equals("steps")) {
            // steps collection reference
            return new FunnelStepsPath(funnel);
        } else if (components.size() == 3) {
            // paths field reference
            return FunnelPathFieldPath.formPath(funnel, components.get(2));
        } else {
           // steps field reference
            return FunnelStepFieldPath.formPath(funnel, components.get(3));
        }

        // paths
        // paths.startTime, paths.endTime, paths.isComplete, paths.isNotComplete, paths.firstStep, paths.lastStep
        // paths.steps.id, paths.steps.time
        // length(paths), length(paths.steps)

    }

}
