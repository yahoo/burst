/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.paths;

/**
 * The universal base ties the different path implementations together so they can be compared without the
 * individual implementations having to know about each other.
 *
 * A specific path type implements its own base with `same` analogs that only worry about path comparisions between
 * the same path types.
 *
 * The universal comparison assume that specific paths like funnel and segment are `dynamically` created by the runtime
 * engine under the object root and so appear under the schema path root.
 *
 */
abstract public class UniversalPathBase implements Path {


    abstract public String getPathAsString();

    public boolean isRoot() {
       return false;
    }

    abstract public Path getEnclosingStructure();

    abstract public Path getParentStructure();

    final public boolean notOnPath(Path p) {
        return !this.isRoot() && !p.isRoot() && !sameOnPath(p);
    }
    abstract protected boolean sameOnPath(Path p);

    final public  boolean higher(Path p) {
        if (p instanceof ConstantPath)
            return false;
        else if (this.isRoot() && p.isRoot())
            return false;
        else if (this.isRoot() && !p.isRoot())
            return true;
        else if (!this.isRoot() && p.isRoot())
            return false;
        else
            return sameHigher(p);
    }
    abstract protected boolean sameHigher(Path p);

    final public boolean lower(Path p) {
        if (p.isRoot())
            return true;
        return sameLower(p);
    }
    abstract protected boolean sameLower(Path p);

}
