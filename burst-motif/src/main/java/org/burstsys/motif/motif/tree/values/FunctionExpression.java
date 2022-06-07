/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.values;

import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.symbols.Definition;

import java.util.List;

/**
 */
public interface FunctionExpression extends ValueExpression, Definition.UsageContext {
    // context data specific to a particular functions
    interface FunctionContext extends Definition.UsageContext {
        String getFunctionName();
    }

    String getFunctionName();

    List<ValueExpression> getParms();

    NodeLocation getLocation();

    NodeGlobal getGlobal();

    FunctionContext getContext();

    void setContext(FunctionContext ctx);
}
