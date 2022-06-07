/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.common;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Global information needed by every Node in a tree.
 */
public class NodeGlobal {

    /** The name of the time zone for resolving date/time values for which the author did not specify a zone. */
    @Nonnull private String defaultTimeZoneName;

    /**
     * Creates a "global information" instance to provide shared context to every Node in a tree. For the default
     * time zone, values from the JDK's list are accepted.
     *
     * @param defaultTimeZoneName the name of the time zone to be used to resolve expressions requiring one, but whose
     *                            source does not specify one
     * @see TimeZone#getAvailableIDs()
     */
    public NodeGlobal(@Nonnull final String defaultTimeZoneName) {
        this.defaultTimeZoneName = Objects.requireNonNull(defaultTimeZoneName);
    }

    /** The name of the time zone for resolving date/time values for which the author did not specify a zone. */
    @Nonnull
    public String defaultTimeZoneName() { return defaultTimeZoneName; }

    /** Returns a statically defined global-information object with default values, for serde operations only. */
    public static NodeGlobal defaultNodeGlobal() {
        return DEFAULT;
    }
    private static final NodeGlobal DEFAULT = new NodeGlobal();
    private NodeGlobal() {
        defaultTimeZoneName = "America/Los_Angeles";
    }

}
