/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.data;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.Node;
import org.burstsys.motif.motif.tree.logical.BooleanExpression;
import org.burstsys.motif.motif.tree.values.ValueExpression;

public interface ParameterAccessor extends Node, ValueExpression {
    String getName();

    DataType getDataType();
}
