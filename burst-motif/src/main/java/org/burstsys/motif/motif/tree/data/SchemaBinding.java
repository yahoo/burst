/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.data;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.paths.schemas.RelationType;

/**
 * Bind a path to its appropriate Schema meta-data
 */
public interface SchemaBinding {
    Path getPath();

    RelationType getRelationType();

    DataType getDatatype();
}
