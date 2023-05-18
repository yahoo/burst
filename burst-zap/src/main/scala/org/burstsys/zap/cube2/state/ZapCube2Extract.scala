/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2.state

import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.brio.types.BrioPrimitives._
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.fabric.wave.execution.model.result.row._
import org.burstsys.felt.model.collectors.cube.FeltCubeBuilder
import org.burstsys.felt.model.collectors.cube.FeltCubeCollector
import org.burstsys.vitals.text.VitalsTextCodec
import org.burstsys.zap.cube2.ZapCube2
import org.burstsys.zap.cube2.row.ZapCube2Row

import scala.collection.mutable

/**
 * extract data from a cube and export as ''results''
 */
trait ZapCube2Extract extends Any with ZapCube2 with ZapCube2Iterator {

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def extractRows(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary)(implicit text: VitalsTextCodec): FeltCubeResultData = {
    val result = new mutable.ArrayBuffer[FeltCubeRowData]

    foreachRow({ row: ZapCube2Row =>
      val rowData = new mutable.ArrayBuffer[FabricResultColumn]
      var d = 0
      while (d < builder.dimensionCount) {
        val isNull = row.dimIsNull(d)
        val value = row.dimRead(d)
        val cell = builder.dimensionFieldTypes(d) match {
          case BrioBooleanKey => FabricResultColumn(FabricDimensionCell, BrioBooleanKey, isNull, isNan = false, brioPrimitiveToBoolean(value))
          case BrioByteKey => FabricResultColumn(FabricDimensionCell, BrioByteKey, isNull, isNan = false, brioPrimitiveToByte(value))
          case BrioShortKey => FabricResultColumn(FabricDimensionCell, BrioShortKey, isNull, isNan = false, brioPrimitiveToShort(value))
          case BrioIntegerKey => FabricResultColumn(FabricDimensionCell, BrioIntegerKey, isNull, isNan = false, brioPrimitiveToInteger(value))
          case BrioLongKey => FabricResultColumn(FabricDimensionCell, BrioLongKey, isNull, isNan = false, brioPrimitiveToLong(value))
          case BrioDoubleKey =>
            val dValue = brioPrimitiveToDouble(value)
            val isNan = dValue.isNaN
            FabricResultColumn(FabricDimensionCell, BrioDoubleKey, isNull, isNan, if (isNan) 0D else dValue)
          case BrioStringKey =>
            val txt = if (isNull) null else brioPrimitiveToString(thisDictionary, value)
            FabricResultColumn(FabricDimensionCell, BrioStringKey, isNull, isNan = false, txt)
          case BrioCourse32Key => FabricResultColumn(cellType = FabricDimensionCell, BrioCourse32Key, isNull, isNan = false, brioPrimitiveToCourse32(value).toString)
        }
        rowData += cell
        d += 1
      }
      var a = 0
      while (a < builder.aggregationCount) {
        val isNull = row.aggIsNull(a)
        val value = row.aggRead(a)
        val cell = builder.aggregationFieldTypes(a) match {
          case BrioBooleanKey => FabricResultColumn(FabricAggregationCell, BrioBooleanKey, isNull, isNan = false, brioPrimitiveToBoolean(value))
          case BrioByteKey => FabricResultColumn(FabricAggregationCell, BrioByteKey, isNull, isNan = false, brioPrimitiveToByte(value))
          case BrioShortKey => FabricResultColumn(FabricAggregationCell, BrioShortKey, isNull, isNan = false, brioPrimitiveToShort(value))
          case BrioIntegerKey => FabricResultColumn(FabricAggregationCell, BrioIntegerKey, isNull, isNan = false, brioPrimitiveToInteger(value))
          case BrioLongKey => FabricResultColumn(FabricAggregationCell, BrioLongKey, isNull, isNan = false, brioPrimitiveToLong(value))
          case BrioDoubleKey =>
            val dValue = brioPrimitiveToDouble(value)
            val isNan = dValue.isNaN
            FabricResultColumn(FabricAggregationCell, BrioDoubleKey, isNull, isNan, if (isNan) 0D else dValue)
          case BrioStringKey =>
            val txt = if (isNull) null else brioPrimitiveToString(thisDictionary, value)
            FabricResultColumn(FabricAggregationCell, BrioStringKey, isNull, isNan = false, txt)
        }
        rowData += cell
        a += 1
      }
      result += rowData.toArray
    })
    result.toArray
  }

}
