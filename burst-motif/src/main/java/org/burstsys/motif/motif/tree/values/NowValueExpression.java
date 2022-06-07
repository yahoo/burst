/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.values;

import org.burstsys.motif.motif.tree.constant.LongConstant;

/**
 * A placehold value expression to be replaced by the 'current datetime'
 * as a Long epoch ms. This cannot be constant reduced cause it needs to find
 * 'now' at runtime. Its more like a Path accessor.
 */
public interface NowValueExpression extends ValueExpression {

    /**
     *
     */
    LongConstant getNow();

}
