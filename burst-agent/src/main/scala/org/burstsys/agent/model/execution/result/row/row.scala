/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.model.execution.result

import org.burstsys.agent.api.BurstQueryApiDatum._
import org.burstsys.agent.api.BurstQueryApiResultCell
import org.burstsys.agent.model.execution.result.cell.tipe._
import org.burstsys.brio.types.BrioTypes
import org.burstsys.fabric.wave.execution.model.result.row.{FabricResultCell, FabricResultRow}
import org.burstsys.vitals.errors.VitalsException

import scala.language.implicitConversions

package object row {

  implicit def thriftToFabricResultRow(cells: Seq[BurstQueryApiResultCell]): FabricResultRow = {
    val cellValues = cells.map {
      c =>
        c.bData match {
          case v: BooleanData => FabricResultCell(
            bData = v.booleanData,
            bType = BrioTypes.BrioBooleanKey,
            bName = BrioTypes.BrioBooleanName,
            isNull = c.isNull,
            isNan = c.isNan,
            cellType = c.cellType
          )
          case v: ByteData => FabricResultCell(
            bData = v.byteData,
            bType = BrioTypes.BrioByteKey,
            bName = BrioTypes.BrioByteName,
            isNull = c.isNull,
            isNan = c.isNan,
            cellType = c.cellType
          )
          case v: ShortData => FabricResultCell(
            bData = v.shortData,
            bType = BrioTypes.BrioShortKey,
            bName = BrioTypes.BrioShortName,
            isNull = c.isNull,
            isNan = c.isNan,
            cellType = c.cellType
          )
          case v: IntegerData => FabricResultCell(
            bData = v.integerData,
            bType = BrioTypes.BrioIntegerKey,
            bName = BrioTypes.BrioIntegerName,
            isNull = c.isNull,
            isNan = c.isNan,
            cellType = c.cellType
          )
          case v: LongData => FabricResultCell(
            bData = v.longData,
            bType = BrioTypes.BrioLongKey,
            bName = BrioTypes.BrioLongName,
            isNull = c.isNull,
            isNan = c.isNan,
            cellType = c.cellType
          )
          case v: DoubleData => FabricResultCell(
            bData = v.doubleData,
            bType = BrioTypes.BrioDoubleKey,
            bName = BrioTypes.BrioDoubleName,
            isNull = c.isNull,
            isNan = c.isNan,
            cellType = c.cellType
          )
          case v: StringData => FabricResultCell(
            bData = v.stringData,
            bType = BrioTypes.BrioStringKey,
            bName = BrioTypes.BrioStringName,
            isNull = c.isNull,
            isNan = c.isNan,
            cellType = c.cellType
          )

          case _ => throw VitalsException(s"unknown datatype ${c.bData}")
        }
    }
    FabricResultRow(cellValues.toArray)
  }
}
