/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.queries;

import org.burstsys.motif.motif.tree.eql.common.Source;
import org.burstsys.motif.motif.tree.eql.common.SourcedStatement;
import org.burstsys.motif.motif.tree.eql.common.Statement;
import org.burstsys.motif.motif.tree.expression.ParameterDefinition;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;

import java.util.List;

public interface Query extends SourcedStatement {

    List<ParameterDefinition> getParameters();

    List<Select> getSelects();

    BooleanExpression getWhere();

    Integer getLimit();
}
