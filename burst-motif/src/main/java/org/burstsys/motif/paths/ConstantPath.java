/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.paths;

/**
 *  This path is the constant path that can be evaluated anywhere.
 *
 *  It provides uniform evaluation comparision operations with all other
 *  paths.
 */
public class ConstantPath extends UniversalPathBase {
    public final static String CONSTANT_PATH = "_constant_";

    private String path;

    public ConstantPath(String path) {
        if (path == null)
            this.path = CONSTANT_PATH;
        this.path = path;
    }

    @Override
    public String getPathAsString() {
        return this.path;
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
    public boolean sameOnPath(Path p) {
        return p instanceof ConstantPath;
    }

    @Override
    public boolean sameHigher(Path p) {
        // only higher if the arg isn't a root
        return !p.isRoot();
    }

    @Override
    public boolean sameLower(Path p) {
        // constants are never lower than anything else
        return false;
    }
}
