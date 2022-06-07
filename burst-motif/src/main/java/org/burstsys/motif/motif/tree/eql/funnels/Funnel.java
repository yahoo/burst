/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.funnels;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.motif.tree.eql.common.SourcedStatement;
import org.burstsys.motif.motif.tree.eql.common.Statement;
import org.burstsys.motif.motif.tree.expression.ParameterDefinition;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.symbols.Definition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 */
public interface Funnel extends SourcedStatement, Definition {
    enum Type { CONVERSION, TRANSACTION }

    enum Tags { LOOSE_MATCH }

    String getName();

    Type getType();

    List<ParameterDefinition> getParameters();

    Collection<StepDefinition> getSteps();

    StepDefinition getStep(Long id);

    FunnelMatchDefinition getDefinition();

    ValueExpression getWithin();

    long getWithinValue();

    Integer getLimit();

    ArrayList<String> getTags();

    @Override
    default Context getContext() {
        return Context.FUNNEL;
    }

    @Override
    default DataType getDtype(UsageContext context) {
        return DataType.NULL;
    }
}
