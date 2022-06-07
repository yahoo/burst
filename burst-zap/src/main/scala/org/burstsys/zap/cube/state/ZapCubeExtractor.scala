/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.state

import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.brio.types.BrioPrimitives._
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.fabric.execution.model.result.row._
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.vitals.text.VitalsTextCodec
import org.burstsys.zap.cube.{ZapCube, ZapCubeBuilder, ZapCubeContext, ZapCubeRow}

import scala.collection.mutable

/**
 * extract data from a cube and export as ''results''
 */
trait ZapCubeExtractor extends Any with ZapCube {

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def extractRows(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary)(implicit text: VitalsTextCodec): FeltCubeResultData = {
    val zb = builder.asInstanceOf[ZapCubeBuilder]
    val tc = thisCube.asInstanceOf[ZapCubeContext]
    val result = new mutable.ArrayBuffer[FeltCubeRowData]

    tc.foreachRow(zb, tc, {
      row: ZapCubeRow =>
        val rowData = new mutable.ArrayBuffer[FabricResultColumn]
        var d = 0
        while (d < builder.dimensionCount) {
          val isNull = row.readRowDimensionIsNull(zb, tc, d)
          val value = row.readRowDimensionPrimitive(zb, tc, d)
          builder.dimensionFieldTypes(d) match {
            case BrioBooleanKey => rowData +=
              FabricResultColumn(cellType = FabricDimensionCell, BrioBooleanKey, isNull = isNull, isNan = false, brioPrimitiveToBoolean(value))
            case BrioByteKey => rowData +=
              FabricResultColumn(cellType = FabricDimensionCell, BrioByteKey, isNull = isNull, isNan = false, brioPrimitiveToByte(value))
            case BrioShortKey => rowData +=
              FabricResultColumn(cellType = FabricDimensionCell, BrioShortKey, isNull = isNull, isNan = false, brioPrimitiveToShort(value))
            case BrioIntegerKey => rowData +=
              FabricResultColumn(cellType = FabricDimensionCell, BrioIntegerKey, isNull = isNull, isNan = false, brioPrimitiveToInteger(value))
            case BrioLongKey => rowData +=
              FabricResultColumn(cellType = FabricDimensionCell, BrioLongKey, isNull = isNull, isNan = false, brioPrimitiveToLong(value))
            case BrioDoubleKey =>
              val dValue = brioPrimitiveToDouble(value)
              val isNan = dValue.asInstanceOf[Double].isNaN
              rowData +=
                FabricResultColumn(cellType = FabricDimensionCell, BrioDoubleKey, isNull = isNull, isNan = isNan, if (isNan) 0D else dValue)
            case BrioStringKey =>
              val txt = if (isNull) null else brioPrimitiveToString(thisDictionary, value)
              rowData +=
                FabricResultColumn(cellType = FabricDimensionCell, BrioStringKey, isNull = isNull, isNan = false, txt)
            case BrioCourse32Key =>
              rowData +=
                FabricResultColumn(cellType = FabricDimensionCell, BrioCourse32Key, isNull = isNull, isNan = false, brioPrimitiveToCourse32(value).toString)
          }
          d += 1
        }
        var a = 0
        while (a < builder.aggregationCount) {
          val isNull = row.readRowAggregationIsNull(zb, tc, a)
          val value = row.readRowAggregationPrimitive(zb, tc, a)
          val v = builder.aggregationFieldTypes(a) match {
            case BrioBooleanKey => rowData +=
              FabricResultColumn(cellType = FabricAggregationCell, BrioBooleanKey, isNull = isNull, isNan = false, brioPrimitiveToBoolean(value))
            case BrioByteKey => rowData +=
              FabricResultColumn(cellType = FabricAggregationCell, BrioByteKey, isNull = isNull, isNan = false, brioPrimitiveToByte(value))
            case BrioShortKey => rowData +=
              FabricResultColumn(cellType = FabricAggregationCell, BrioShortKey, isNull = isNull, isNan = false, brioPrimitiveToShort(value))
            case BrioIntegerKey => rowData +=
              FabricResultColumn(cellType = FabricAggregationCell, BrioIntegerKey, isNull = isNull, isNan = false, brioPrimitiveToInteger(value))
            case BrioLongKey => rowData +=
              FabricResultColumn(cellType = FabricAggregationCell, BrioLongKey, isNull = isNull, isNan = false, brioPrimitiveToLong(value))
            case BrioDoubleKey =>
              val dValue = brioPrimitiveToDouble(value)
              val isNan = dValue.asInstanceOf[Double].isNaN
              rowData +=
                FabricResultColumn(cellType = FabricAggregationCell, BrioDoubleKey, isNull = isNull, isNan = isNan, if (isNan) 0D else dValue)
            case BrioStringKey =>
              val txt = if (isNull) null else brioPrimitiveToString(thisDictionary, value)
              rowData +=
                FabricResultColumn(cellType = FabricAggregationCell, BrioStringKey, isNull = isNull, isNan = false, txt)
          }
          a += 1
        }
        result += rowData.toArray
    })
    result.toArray
  }

}
