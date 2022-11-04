/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.result.row

import org.burstsys.brio.types.BrioCourse.{BrioCourse16, BrioCourse32, BrioCourse4, BrioCourse8}
import org.burstsys.brio.types.BrioTypes._

/**
 * This is a single column/cell in a Zap Cube row.
 */
final case
class FabricResultColumn(cellType: FabricResultCellType, bType: BrioTypeKey, isNull: Boolean, isNan: Boolean, var value: BrioDataType)
  extends Ordered[FabricResultColumn] {

  override
  def toString: String =
    if (isNull) "(null)"
    else if (isNan) "(NaN)"
    else {
      bType match {
        case BrioBooleanKey => f"${value.asInstanceOf[Boolean]}%B"
        case BrioByteKey => f"${value.asInstanceOf[Byte]}%,d"
        case BrioShortKey => f"${value.asInstanceOf[Short]}%,d"
        case BrioIntegerKey => f"${value.asInstanceOf[Int]}%,d"
        case BrioLongKey => f"${value.asInstanceOf[Long]}%,d"
        case BrioDoubleKey => f"${value.asInstanceOf[Double]}%f"
        case BrioStringKey => f"'${value.asInstanceOf[String]}'"
        case BrioCourse32Key | BrioCourse16Key | BrioCourse8Key | BrioCourse4Key => f"'${value.toString}'"
        // TODO EXTENDED TYPES
      }
    }

  override
  def compare(that: FabricResultColumn): Int = {
    if (this.isNull && that.isNull) return 0
    bType match {
      case BrioBooleanKey => value.asInstanceOf[Boolean].compareTo(that.value.asInstanceOf[Boolean])
      case BrioByteKey => value.asInstanceOf[Byte].compareTo(that.value.asInstanceOf[Byte])
      case BrioShortKey => value.asInstanceOf[Short].compareTo(that.value.asInstanceOf[Short])
      case BrioIntegerKey => value.asInstanceOf[Int].compareTo(that.value.asInstanceOf[Int])
      case BrioLongKey => value.asInstanceOf[Long].compareTo(that.value.asInstanceOf[Long])
      case BrioDoubleKey => value.asInstanceOf[Double].compareTo(that.value.asInstanceOf[Double])
      case BrioStringKey => value.asInstanceOf[String].compareTo(that.value.asInstanceOf[String])
      case BrioCourse32Key => value.asInstanceOf[BrioCourse32].data.compareTo(that.value.asInstanceOf[BrioCourse32].data)
      case BrioCourse16Key => value.asInstanceOf[BrioCourse16].data.compareTo(that.value.asInstanceOf[BrioCourse16].data)
      case BrioCourse8Key => value.asInstanceOf[BrioCourse8].data.compareTo(that.value.asInstanceOf[BrioCourse8].data)
      case BrioCourse4Key => value.asInstanceOf[BrioCourse4].data.compareTo(that.value.asInstanceOf[BrioCourse4].data)
      // TODO EXTENDED TYPES
    }
  }
}
