/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2.flex

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.dictionary.flex.BrioFlexDictionary
import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.fabric.execution.model.result.row.FeltCubeResultData
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.flex.{TeslaFlexCoupler, TeslaFlexProxy, TeslaFlexProxyContext, TeslaFlexSlotIndex}
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.bitmap.VitalsBitMapAnyVal
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.text.VitalsTextCodec
import org.burstsys.zap.cube2
import org.burstsys.zap.cube2.key.ZapCube2Key
import org.burstsys.zap.cube2.row.ZapCube2Row
import org.burstsys.zap.cube2.{ZapCube2, ZapCube2Builder}

/**
 * The Tesla Flex Proxy for the Cube2
 * ===a word on resizing and dirty bits===
 * The way we do resizing is that any operation that can '''add rows''' is monitored for when that addition
 * overflows existing space in the off heap storage chunk. At that pt the '''rowsLimited''' flag is set, and
 * we back out of the current operation as fast as we can. Once we are at the top level, we copy the entire off
 * heap chunk into a new bigger chunk and ''restart'' the operation. This is relatively straightforward since
 * all the data along with tmp data is stored in that off heap chunk and copied over verbatim. In simple operations
 * such as a aggregation write, this means a single row is ''not'' allocated at first, but then ''is'' allocated the
 * second time. Thats easy. However when we do more complex operations such as merges and joins, each operation both
 * potentially allocates rows ''and'' updates existing rows. To handle this we use '''dirty bits'''. This means that
 * when we start the operation we mark any rows added or updated as ''dirty'' and when/if we need to do a restart we do
 * ''not'' touch those rows, skipping over these as we go. At the end of this complex operation we scan through all
 * the final resulting rows '''resetting''' the dirty bit and making ready for the next operation. This takes a bit
 * of careful thinking and carefully following through the various scenarios to be sure it all hangs together under
 * all sorts of edge cases. We try to have a fairly exhaustive set of unit tests that exhibit resizing during
 * joins and merges in order to make sure there are no holes. All of this in the service of allocating no heap
 * objects, and keeping our inner loop memory references localized to one region of memory.
 */
trait ZapFlexCube2 extends Any with ZapCube2 with TeslaFlexProxy[ZapCube2Builder, ZapCube2]

private final case
class ZapFlexCube2AnyVal(index: TeslaFlexSlotIndex) extends AnyVal with ZapFlexCube2
  with TeslaFlexProxyContext[ZapCube2Builder, ZapCube2, ZapFlexCube2] {

  override def coupler: TeslaFlexCoupler[ZapCube2Builder, ZapCube2, ZapFlexCube2] = cube2.flex.coupler

  /////////////////////////////////////////////////////////////////////////////////////////////
  // simple delegation for internal dictionary read only methods
  /////////////////////////////////////////////////////////////////////////////////////////////

  override def clear(): Unit = internalCollector.clear()

  override def bucketsCount: Int = internalCollector.bucketsCount

  override def rowsCount: Int = internalCollector.rowsCount

  override def rowsLimited: Boolean = internalCollector.rowsLimited

  override def isEmpty: Boolean = internalCollector.isEmpty

  override def aggCount: Int = internalCollector.aggCount

  override def dimCount: Int = internalCollector.dimCount

  //  override def inheritCursorFrom(thatCube: ZapCube2): Unit = internalCollector.inheritCursorFrom(thatCube)

  override def cursor: ZapCube2Key = internalCollector.cursor

  override def pivot: ZapCube2Key = internalCollector.pivot

  override def initialize(pId: TeslaPoolId, builder: ZapCube2Builder): Unit = internalCollector.initialize(pId, builder)

  override def reset(builder: ZapCube2Builder): Unit = internalCollector.reset(builder)

  override def defaultBuilder: ZapCube2Builder = internalCollector.defaultBuilder

  override def currentMemorySize: TeslaMemorySize = internalCollector.currentMemorySize

  override def poolId: TeslaPoolId = internalCollector.poolId

  override def blockPtr: TeslaMemoryPtr = internalCollector.blockPtr

  override def builder: ZapCube2Builder = internalCollector.builder

  override def aggIsNull(aggregation: Int): Boolean = internalCollector.aggIsNull(aggregation)

  override def dimIsNull(dimension: Int): Boolean = internalCollector.dimIsNull(dimension)

  override def row(index: TeslaPoolId): ZapCube2Row = internalCollector.row(index)

  override def write(k: Kryo, out: Output): Unit = internalCollector.write(k, out)

  override def read(k: Kryo, in: Input): Unit = internalCollector.read(k, in)

  override def toString: String = internalCollector.toString

  override def truncateToTopKBasedOnAggregation(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, k: TeslaPoolId, aggregation: TeslaPoolId): Unit =
    internalCollector.truncateToTopKBasedOnAggregation(builder, thisCube, k, aggregation)

  override def truncateToBottomKBasedOnAggregation(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, k: TeslaPoolId, aggregation: TeslaPoolId): Unit =
    internalCollector.truncateToBottomKBasedOnAggregation(builder, thisCube, k, aggregation)

  override def bucketRead(index: TeslaPoolId): TeslaMemoryOffset = internalCollector.bucketRead(index)

  override def resetCursor(): Unit = internalCollector.resetCursor()

  override def resetPivot(): Unit = internalCollector.resetPivot()

  override def initCursor(builder: FeltCubeBuilder, thisCube: FeltCubeCollector): Unit = internalCollector.initCursor(builder, thisCube)

  override def inheritCursor(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, parentCube: FeltCubeCollector): FeltCubeCollector =
    internalCollector.inheritCursor(builder, thisCube, parentCube)

  override def dictionary: BrioFlexDictionary = internalCollector.dictionary

  override def dictionary_=(d: BrioFlexDictionary): Unit = internalCollector.dictionary_=(d)

  override def cursorRow: TeslaMemoryOffset = internalCollector.cursorRow

  override def extractRows(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary)(implicit text: VitalsTextCodec): FeltCubeResultData =
    internalCollector.extractRows(builder, thisCube, thisDictionary)

  override def bucketStdDeviation: Double = internalCollector.bucketStdDeviation

  override def bucketListLengthMax: Int = internalCollector.bucketListLengthMax

  override def resizeCount: Int = internalCollector.resizeCount

  override def resetDirtyRows(): Unit = internalCollector.resetDirtyRows()

  /////////////////////////////////////////////////////////////////////////////////////////////
  // write method(s) that require overflow interception and resizing - just one so far
  /////////////////////////////////////////////////////////////////////////////////////////////

  override
  def joinWithChildCubeIntoResultCube(builder: FeltCubeBuilder,
                                      thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary,
                                      childCube: FeltCubeCollector,
                                      resultCube: FeltCubeCollector,
                                      parentDimensionMask: VitalsBitMapAnyVal, parentAggregationMask: VitalsBitMapAnyVal,
                                      childDimensionMask: VitalsBitMapAnyVal, childAggregationMask: VitalsBitMapAnyVal
                                     ): Unit = {
    internalCollector.joinWithChildCubeIntoResultCube(
      builder, thisCube, thisDictionary,
      childCube,
      resultCube,
      parentDimensionMask, parentAggregationMask, childDimensionMask, childAggregationMask
    )
    if (internalCollector.rowsLimited) {
      coupler.upsize(this.index, rowsCount, internalCollector.builder)
      // we have an opportunity to redo this
      internalCollector.joinWithChildCubeIntoResultCube(
        builder, thisCube, thisDictionary, childCube, resultCube,
        parentDimensionMask, parentAggregationMask, childDimensionMask, childAggregationMask
      )
    }
    resetDirtyRows()
  }

  override
  def normalizeThatCubeToThis(thatCube: ZapCube2, builder: ZapCube2Builder, text: VitalsTextCodec): ZapCube2 = {
    var result = internalCollector.normalizeThatCubeToThis(thatCube, builder, text)
    if (internalCollector.rowsLimited) {
      coupler.upsize(this.index, rowsCount, internalCollector.builder)
      // we have an opportunity to redo this
      result = internalCollector.normalizeThatCubeToThis(thatCube, builder, text)
    }
    resetDirtyRows()
    result
  }


  override def normalize(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary, thatCube: FeltCubeCollector, thatDictionary: BrioMutableDictionary)(implicit text: VitalsTextCodec): FeltCubeCollector = ???


  override
  def navigate(key: ZapCube2Key): ZapCube2Row = {
    var result = internalCollector.navigate(key)
    if (internalCollector.rowsLimited) {
      coupler.upsize(this.index, rowsCount, internalCollector.builder)
      // we have an opportunity to redo this
      result = internalCollector.navigate(key)
    }
    //    resetDirtyRows()
    result
  }

  override def createCopyRow(parentCube: ZapCube2, childCube: ZapCube2, childRow: ZapCube2Row,
                             resultCube: ZapCube2, parentDimensionMask: VitalsBitMapAnyVal,
                             parentAggregationMask: VitalsBitMapAnyVal, childDimensionMask: VitalsBitMapAnyVal,
                             childAggregationMask: VitalsBitMapAnyVal): ZapCube2Row = {
    var result = internalCollector.createCopyRow(parentCube: ZapCube2, childCube: ZapCube2, childRow: ZapCube2Row,
      resultCube: ZapCube2, parentDimensionMask: VitalsBitMapAnyVal,
      parentAggregationMask: VitalsBitMapAnyVal, childDimensionMask: VitalsBitMapAnyVal,
      childAggregationMask: VitalsBitMapAnyVal)
    if (internalCollector.rowsLimited) {
      coupler.upsize(this.index, rowsCount, internalCollector.builder)
      // we have an opportunity to redo this
      result = internalCollector.createCopyRow(parentCube: ZapCube2, childCube: ZapCube2, childRow: ZapCube2Row,
        resultCube: ZapCube2, parentDimensionMask: VitalsBitMapAnyVal,
        parentAggregationMask: VitalsBitMapAnyVal, childDimensionMask: VitalsBitMapAnyVal,
        childAggregationMask: VitalsBitMapAnyVal)
    }
    resetDirtyRows()
    result
  }

  override def createJoinRow(
                              parentRow: ZapCube2Row,
                              childCube: ZapCube2, childRow: ZapCube2Row,
                              resultCube: ZapCube2,
                              parentDimensionMask: VitalsBitMapAnyVal,
                              parentAggregationMask: VitalsBitMapAnyVal,
                              childDimensionMask: VitalsBitMapAnyVal,
                              childAggregationMask: VitalsBitMapAnyVal
                            ): ZapCube2Row = {
    var result = internalCollector.createJoinRow(
      parentRow: ZapCube2Row,
      childCube: ZapCube2, childRow: ZapCube2Row,
      resultCube: ZapCube2,
      parentDimensionMask: VitalsBitMapAnyVal, parentAggregationMask: VitalsBitMapAnyVal,
      childDimensionMask: VitalsBitMapAnyVal, childAggregationMask: VitalsBitMapAnyVal
    )
    if (internalCollector.rowsLimited) {
      coupler.upsize(this.index, rowsCount, internalCollector.builder)
      // we have an opportunity to redo this
      result = internalCollector.createJoinRow(
        parentRow: ZapCube2Row,
        childCube: ZapCube2, childRow: ZapCube2Row,
        resultCube: ZapCube2,
        parentDimensionMask: VitalsBitMapAnyVal, parentAggregationMask: VitalsBitMapAnyVal,
        childDimensionMask: VitalsBitMapAnyVal, childAggregationMask: VitalsBitMapAnyVal
      )
    }
    resetDirtyRows()
    result
  }

  override
  def interMerge(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary,
                 thatCube: FeltCubeCollector, thatDictionary: BrioMutableDictionary): Unit = {
    internalCollector.interMerge(builder, thisCube, thisDictionary, thatCube, thatDictionary)
    if (internalCollector.rowsLimited) {
      coupler.upsize(this.index, rowsCount, internalCollector.builder)
      // we have an opportunity to redo this
      internalCollector.interMerge(builder, thisCube, thisDictionary, thatCube, thatDictionary)
    }
    resetDirtyRows()
  }

  override
  def intraMerge(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary,
                 thatCube: FeltCubeCollector, thatDictionary: BrioMutableDictionary,
                 dimensionMask: VitalsBitMapAnyVal, aggregationMask: VitalsBitMapAnyVal): Unit = {
    internalCollector.intraMerge(builder, thisCube, thisDictionary, thatCube, thatDictionary, dimensionMask, aggregationMask)
    if (internalCollector.rowsLimited) {
      coupler.upsize(this.index, rowsCount, internalCollector.builder)
      // we have an opportunity to redo this
      internalCollector.intraMerge(builder, thisCube, thisDictionary, thatCube, thatDictionary, dimensionMask, aggregationMask)
    }
    resetDirtyRows()
  }

  override
  def dimRead(dimension: Int): BrioPrimitive = {
    var result = internalCollector.dimRead(dimension)
    if (internalCollector.rowsLimited) {
      coupler.upsize(this.index, rowsCount, internalCollector.builder)
      // we have an opportunity to redo this
      result = internalCollector.dimRead(dimension)
    }
    result
  }

  override def dimWrite(): Unit = {
    internalCollector.dimWrite()
    if (internalCollector.rowsLimited) {
      coupler.upsize(this.index, rowsCount, internalCollector.builder)
      // we have an opportunity to redo this
      internalCollector.dimWrite()
    }
  }

  override def dimSetNotNull(dimension: Int): Unit = {
    internalCollector.dimSetNotNull(dimension)
    if (internalCollector.rowsLimited) {
      coupler.upsize(this.index, rowsCount, internalCollector.builder)
      // we have an opportunity to redo this
      internalCollector.dimSetNotNull(dimension)
    }
  }

  override def dimSetNull(dimension: Int): Unit = {
    internalCollector.dimSetNull(dimension)
    if (internalCollector.rowsLimited) {
      coupler.upsize(this.index, rowsCount, internalCollector.builder)
      // we have an opportunity to redo this
      internalCollector.dimSetNull(dimension)
    }
  }

  override def dimWrite(dimension: Int, value: BrioPrimitive): Unit = {
    internalCollector.dimWrite(dimension, value)
    if (internalCollector.rowsLimited) {
      coupler.upsize(this.index, rowsCount, internalCollector.builder)
      // we have an opportunity to redo this
      internalCollector.dimWrite(dimension, value)
    }
  }

  override def aggRead(aggregation: Int): BrioPrimitive = {
    val result = internalCollector.aggRead(aggregation)
    if (internalCollector.rowsLimited) {
      coupler.upsize(this.index, rowsCount, internalCollector.builder)
      // we have an opportunity to redo this
      internalCollector.aggRead(aggregation)
    } else result
  }

  override def aggSetNull(aggregation: Int): Unit = {
    internalCollector.aggSetNull(aggregation)
    if (internalCollector.rowsLimited) {
      coupler.upsize(this.index, rowsCount, internalCollector.builder)
      // we have an opportunity to redo this
      internalCollector.aggSetNull(aggregation)
    }
  }

  override def aggSetNotNull(aggregation: Int): Unit = {
    internalCollector.aggSetNotNull(aggregation)
    if (internalCollector.rowsLimited) {
      coupler.upsize(this.index, rowsCount, internalCollector.builder)
      // we have an opportunity to redo this
      internalCollector.aggSetNotNull(aggregation)
    }
  }

  override def aggWrite(aggregation: Int, value: BrioPrimitive): Unit = {
    internalCollector.aggWrite(aggregation, value)
    if (internalCollector.rowsLimited) {
      coupler.upsize(this.index, rowsCount, internalCollector.builder)
      // we have an opportunity to redo this
      internalCollector.aggWrite(aggregation, value)
    }
  }

  override def navigate(): Unit = {
    internalCollector.navigate()
    if (internalCollector.rowsLimited) {
      coupler.upsize(this.index, rowsCount, internalCollector.builder)
      // we have an opportunity to redo this
      internalCollector.navigate()
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////
  // methods that should not be called on a proxy
  /////////////////////////////////////////////////////////////////////////////////////////////

  override def importCollector(sourceCollector: ZapCube2, sourceItems: TeslaPoolId, builder: ZapCube2Builder): Unit =
    throw VitalsException(s"importCollector should not be called in flex cube!")

  override def rowCount: TeslaPoolId = ???

  override def rowCount_=(count: TeslaPoolId): Unit = ???

  override def rowLimited: Boolean = ???

  override def writeAggregationNull(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, column: TeslaPoolId): Unit = ???

  override def readAggregationPrimitive(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, aggregation: TeslaPoolId): BrioPrimitive = ???

  override def writeAggregationPrimitive(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, aggregation: TeslaPoolId, value: BrioPrimitive): Unit = ???

  override def readAggregationNull(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, aggregation: TeslaPoolId): Boolean = ???

  override def writeDimension(builder: FeltCubeBuilder, thisCube: FeltCubeCollector): Unit = ???

  override def writeDimensionNull(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, dimension: TeslaPoolId): Unit = ???

  override def writeDimensionPrimitive(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, dimension: TeslaPoolId, value: BrioPrimitive): Unit = ???

  override def printCube(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary): String = ???

  override def printCubeState(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary, msg: String): Unit = ???

  override def distribution(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary): Double = ???

  override def rowLimited_=(s: Boolean): Unit = ???
}
