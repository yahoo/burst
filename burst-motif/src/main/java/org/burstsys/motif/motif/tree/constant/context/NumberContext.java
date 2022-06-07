/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.constant.context;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeLocation;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.constant.NumberConstant;

public abstract class NumberContext extends ConstantContext implements NumberConstant {

    protected NumberContext(NodeGlobal global, NodeLocation location, NodeType ntype, DataType dataType, String valueText) {
        super(global, location, ntype, dataType, valueText);
    }

    protected NumberContext(NodeGlobal global, NodeLocation location, NodeType ntype, DataType dataType, Object value) {
        super(global, location, ntype, dataType, value);
    }

    protected NumberContext(NodeGlobal global, NodeType ntype) {
        super(global, ntype);
    }

}
