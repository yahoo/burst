/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.segments;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.motif.tree.eql.common.SourcedStatement;
import org.burstsys.motif.motif.tree.eql.common.Statement;
import org.burstsys.motif.motif.tree.expression.ParameterDefinition;
import org.burstsys.motif.symbols.Definition;

import java.util.List;

import static org.burstsys.motif.common.DataType.NULL;

/**
 *   Segment Definition
 */
public interface Segment extends SourcedStatement, Definition {
    String getName();

    List<ParameterDefinition> getParameters();

    List<SegmentDefinition> getDefinitions();

    @Override
    default Context getContext() {
        return Context.SOURCE;
    }

    @Override
    default DataType getDtype(UsageContext context) {
        return NULL;
    }
}
