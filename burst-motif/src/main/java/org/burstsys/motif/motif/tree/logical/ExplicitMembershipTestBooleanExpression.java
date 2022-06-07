/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.logical;

import org.burstsys.motif.motif.tree.values.ValueExpression;

import java.util.List;

public interface ExplicitMembershipTestBooleanExpression extends BooleanExpression {
    ValueExpression getLeft();

    List<ValueExpression> getMembers();

    MembershipTestOperatorType getOp();
}
