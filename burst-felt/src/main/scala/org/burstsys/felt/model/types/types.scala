/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.vitals.strings._

/**
 * =types=
 */
package object types {

  def assertCanAssignOrUpdate(expression: FeltExpression, lhs: FeltPathExpr, rhs: FeltExpression): Unit = {
    // is it mutable?
    if (!lhs.isMutable)
      throw FeltException(expression.location, s"'${expression.normalizedSource.condensed}' assignment not possible to immutable lhs")
    else {
      // are they of the same type?
      if (lhs.resolveTypes.feltType == rhs.resolveTypes.feltType) return
      // anything can be assigned to static null?
      if (rhs.resolveTypes.reduceToNull.nonEmpty) return
      // are they of a compatible type?
      (lhs.feltType.valueType, rhs.feltType.valueType) match {
        case (BrioLongKey, BrioIntegerKey) => return
        case (BrioLongKey, BrioShortKey) => return
        case (BrioLongKey, BrioByteKey) => return
        case (BrioLongKey, BrioLongKey) => return

        case (BrioDoubleKey, BrioIntegerKey) => return
        case (BrioDoubleKey, BrioShortKey) => return
        case (BrioDoubleKey, BrioByteKey) => return
        case (BrioDoubleKey, BrioLongKey) => return

        case (BrioIntegerKey, BrioIntegerKey) => return
        case (BrioIntegerKey, BrioShortKey) => return
        case (BrioIntegerKey, BrioByteKey) => return
        case _ =>
      }
      throw FeltException(expression.location,
        s"'${expression.normalizedSource.condensed}' assignment types not compatible lhs=${lhs.feltType}, rhs=${rhs.feltType}"
      )
    }
  }


}
