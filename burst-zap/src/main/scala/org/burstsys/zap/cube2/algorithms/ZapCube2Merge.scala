/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2.algorithms

import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.FeltCubeAggSemRt
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.vitals.bitmap.VitalsBitMapAnyVal
import org.burstsys.vitals.git.log
import org.burstsys.vitals.text.VitalsTextCodec
import org.burstsys.zap.cube2.ZapCube2
import org.burstsys.zap.cube2.row.ZapCube2Row
import org.burstsys.zap.cube2.state._

import scala.annotation.unused

/**
 *
 */
trait ZapCube2Merge extends Any with ZapCube2State with ZapCube2Nav {

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def interMerge(builder: FeltCubeBuilder, thatCube: FeltCubeCollector)
                (implicit codec: VitalsTextCodec): Unit = {
    try {
      rowMerge(builder, thatCube.asInstanceOf[ZapCube2], VitalsBitMapAnyVal(~0L), intra = false)
    } finally if (!rowsLimited)
      resizeCount = 0 // made it all the way through without a resize
  }

  @inline final override
  def intraMerge(builder: FeltCubeBuilder, theDictionary: BrioMutableDictionary, thatCube: FeltCubeCollector,
                 dimensionMask: VitalsBitMapAnyVal, aggregationMask: VitalsBitMapAnyVal): Unit = {
    try {
      this.dictionary = theDictionary
      rowMerge(builder, thatCube.asInstanceOf[ZapCube2], aggregationMask, intra = true)
    } finally if (!rowsLimited)
      resizeCount = 0 // made it all the way through without a resize

  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // IMPLEMENTATION
  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * merge that cube into this cube.  This differs from the buckerMerger in that it tolerates cubes with different
   * bucket sizes
   */
  @inline private
  def rowMerge(builder: FeltCubeBuilder, thatCube: ZapCube2, aggregationMask: VitalsBitMapAnyVal, intra: Boolean)
              (implicit codec: VitalsTextCodec): Unit = {
    if (thatCube.isEmpty)
      return // if the incoming cube is empty - nothing to do

    var rc = 0
    while (rc < thatCube.rowsCount) {
      val currentThatRow = thatCube.row(rc)
      val startRowsCount = rowsCount
      setCursorFrom(currentThatRow)

      if (!intra) {
        var d = 0
        while (d < dimCount) {
          if (!currentThatRow.dimIsNull(d) && builder.dimensionFieldTypes(d) == BrioStringKey) {
            val stringIndex = this.dimRead(d)
            val oldString = thatCube.dictionary.stringLookup(stringIndex.toShort)
            val newIdx = this.dictionary.keyLookupWithAdd(oldString)
            this.dimWrite(d, newIdx)
            this.dictionary
          }
          d += 1
        }
      }

      val currentThisRow = navigate(cursor) // this might create a new row
      // check for overflow
      if (rowsLimited)
        return

      // process row unless it is dirty from a previous try
      if (!currentThisRow.dirty) {
        if (startRowsCount == rowsCount) {
          // we found an existing row to merge into
          mergeRows(builder, aggregationMask, currentThisRow, currentThatRow, intra)
        } else {
          // we created a new row so just assign aggregations
          importAggs(builder, aggregationMask, currentThisRow, currentThatRow)
        }
        currentThisRow.dirty = true
      }
      rc += 1
    }
  }


  /**
   * Import the masked aggregations from the source row to the target row
   */
  @inline
  private
  def importAggs(@unused builder: FeltCubeBuilder, aggregationMask: VitalsBitMapAnyVal,
                 targetRow: ZapCube2Row, sourceRow: ZapCube2Row): Unit = {
    var aggregation = 0
    while (aggregation < aggCount) {
      // make sure that we do not aggregate for fields in other gathers
      if (aggregationMask.testBit(aggregation)) {
        if (!sourceRow.aggIsNull(aggregation)) {
          targetRow.aggWrite(aggregation, sourceRow.aggRead(aggregation))
        } else {
          targetRow.aggSetNull(aggregation)
        }
      }
      aggregation += 1
    }
  }

  @inline
  private
  def mergeRows(builder: FeltCubeBuilder, aggregationMask: VitalsBitMapAnyVal,
                existingRow: ZapCube2Row, incomingRow: ZapCube2Row, intra: Boolean) : Unit = {
    // merge the rows for this we require aggregation semantics
    var aggregation = 0
    while (aggregation < aggCount) {
      // make sure that we do not aggregate for fields in other gathers
      if (aggregationMask.testBit(aggregation)) {
        val semantic: FeltCubeAggSemRt = builder.aggregationSemantics(aggregation)
        if (!incomingRow.aggIsNull(aggregation)) {
          builder.aggregationFieldTypes(aggregation) match {
            case BrioBooleanKey =>
              existingRow.aggWrite(aggregation, semantic.doBoolean(existingRow.aggRead(aggregation), incomingRow.aggRead(aggregation), intra))
            case BrioByteKey =>
              existingRow.aggWrite(aggregation, semantic.doByte(existingRow.aggRead(aggregation), incomingRow.aggRead(aggregation), intra))
            case BrioShortKey =>
              existingRow.aggWrite(aggregation, semantic.doShort(existingRow.aggRead(aggregation), incomingRow.aggRead(aggregation), intra))
            case BrioIntegerKey =>
              existingRow.aggWrite(aggregation, semantic.doInteger(existingRow.aggRead(aggregation), incomingRow.aggRead(aggregation), intra))
            case BrioLongKey =>
              existingRow.aggWrite(aggregation, semantic.doLong(existingRow.aggRead(aggregation), incomingRow.aggRead(aggregation), intra))
            case BrioDoubleKey =>
              existingRow.aggWrite(aggregation, semantic.doDouble(existingRow.aggRead(aggregation), incomingRow.aggRead(aggregation), intra))
            case BrioStringKey =>
              existingRow.aggWrite(aggregation, semantic.doString(existingRow.aggRead(aggregation), incomingRow.aggRead(aggregation), intra))
          }
        }
      }
      aggregation += 1
    }
  }
}
