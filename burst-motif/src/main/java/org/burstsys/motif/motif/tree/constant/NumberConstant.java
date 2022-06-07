/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.constant;

import org.burstsys.motif.motif.tree.values.BinaryValueOperatorType;

public interface NumberConstant extends Constant {

    NumberConstant binaryOperate(BinaryValueOperatorType operator, NumberConstant rightLiteral);

    NumberConstant negate();

}
