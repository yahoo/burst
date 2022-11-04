/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2.flex

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.dictionary.flex.BrioFlexDictionary
import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.fabric.wave.execution.model.result.row.FeltCubeResultData
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.flex
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

  // not sure these should be overridden
  @inline override
  def currentMemorySize: TeslaMemorySize = internalCollector.currentMemorySize

  @inline override
  def poolId: TeslaPoolId = internalCollector.poolId

  @inline override
  def blockPtr: TeslaMemoryPtr = internalCollector.blockPtr

  // proxy

  @inline override
  def size(): TeslaMemorySize = internalCollector.size()

  @inline override
  def coupler: TeslaFlexCoupler[ZapCube2Builder, ZapCube2, ZapFlexCube2] = cube2.flex.coupler

  /////////////////////////////////////////////////////////////////////////////////////////////
  // simple delegation for internal dictionary read only methods
  /////////////////////////////////////////////////////////////////////////////////////////////

  @inline override
  def clear(): Unit = internalCollector.clear()

  @inline override
  def bucketsCount: Int = internalCollector.bucketsCount

  @inline override
  def rowsCount: Int = internalCollector.rowsCount

  @inline override
  def itemCount: Int = internalCollector.itemCount

  @inline override
  def rowsLimited: Boolean = internalCollector.rowsLimited

  @inline override
  def itemLimited: Boolean = internalCollector.itemLimited

  @inline override
  def isEmpty: Boolean = internalCollector.isEmpty

  @inline override
  def aggCount: Int = internalCollector.aggCount

  @inline override
  def dimCount: Int = internalCollector.dimCount

  @inline override
  def cursor: ZapCube2Key = internalCollector.cursor

  @inline override
  def pivot: ZapCube2Key = internalCollector.pivot

  @inline override
  def initialize(pId: TeslaPoolId, builder: ZapCube2Builder): Unit = internalCollector.initialize(pId, builder)

  @inline override
  def reset(builder: ZapCube2Builder): Unit = internalCollector.reset(builder)

  @inline override
  def defaultBuilder: ZapCube2Builder = internalCollector.defaultBuilder

  @inline override
  def builder: ZapCube2Builder = internalCollector.builder

  @inline override
  def aggIsNull(aggregation: Int): Boolean = internalCollector.aggIsNull(aggregation)

  @inline override
  def dimIsNull(dimension: Int): Boolean = internalCollector.dimIsNull(dimension)

  @inline override
  def row(index: TeslaPoolId): ZapCube2Row = internalCollector.row(index)

  @inline override
  def write(k: Kryo, out: Output): Unit = {
    internalCollector.write(k, out)
  }

  @inline override
  def read(k: Kryo, in: Input): Unit = {
    internalCollector.read(k, in)
  }

  @inline override
  def toString: String = {
    if (this.index != flex.emptySlotIndex ) {
      val c = this.internalCollector
      s"FlexCube[$index] ${c.toString}"
    } else
      "EmptySlotIndex"
  }

  @inline override
  def truncateToTopKBasedOnAggregation(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, k: TeslaPoolId, aggregation: TeslaPoolId): Unit =
    internalCollector.truncateToTopKBasedOnAggregation(builder, thisCube, k, aggregation)

  @inline override
  def truncateToBottomKBasedOnAggregation(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, k: TeslaPoolId, aggregation: TeslaPoolId): Unit =
    internalCollector.truncateToBottomKBasedOnAggregation(builder, thisCube, k, aggregation)

  @inline override
  def truncateRows(limit: TeslaPoolId): Unit = {
    internalCollector.truncateRows(limit)
  }

  @inline override
  def bucketRead(index: TeslaPoolId): TeslaMemoryOffset = internalCollector.bucketRead(index)

  @inline override
  def resetCursor(): Unit = internalCollector.resetCursor()

  @inline override
  def resetPivot(): Unit = internalCollector.resetPivot()

  @inline override
  def setCursorFrom(row: ZapCube2Row): Unit = internalCollector.setCursorFrom(row)

  @inline override
  def initCursor(builder: FeltCubeBuilder, thisCube: FeltCubeCollector): Unit = internalCollector.initCursor(builder, thisCube)

  @inline override
  def inheritCursor(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, parentCube: FeltCubeCollector): FeltCubeCollector =
    internalCollector.inheritCursor(builder, thisCube, parentCube)

  @inline override
  def dictionary: BrioFlexDictionary = internalCollector.dictionary

  @inline override
  def dictionary_=(d: BrioFlexDictionary): Unit = internalCollector.dictionary_=(d)

  @inline override
  def cursorRow: TeslaMemoryOffset = internalCollector.cursorRow

  @inline override
  def extractRows(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary)(implicit text: VitalsTextCodec): FeltCubeResultData = {
    internalCollector.extractRows(builder, thisCube, thisDictionary)
  }

  @inline override
  def bucketStdDeviation: Double = internalCollector.bucketStdDeviation

  @inline override
  def bucketListLengthMax: Int = internalCollector.bucketListLengthMax

  @inline override
  def resizeCount: Int = internalCollector.resizeCount

  @inline override
  def resetDirtyRows(): Unit = internalCollector.resetDirtyRows()

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
    runWithRetry {
      internalCollector.joinWithChildCubeIntoResultCube(
        builder, internalCollector, thisDictionary,
        childCube,
        resultCube,
        parentDimensionMask, parentAggregationMask, childDimensionMask, childAggregationMask
      )
    }
  }

  override
  def normalizeThatCubeToThis(thatCube: ZapCube2, builder: ZapCube2Builder, text: VitalsTextCodec): ZapCube2 = {
    runWithRetry{
      internalCollector.normalizeThatCubeToThis(thatCube, builder, text)

    }
  }


  @inline override
  def normalize(builder: FeltCubeBuilder,
                         thisCube: FeltCubeCollector,
                         thisDictionary: BrioMutableDictionary,
                         thatCube: FeltCubeCollector,
                         thatDictionary: BrioMutableDictionary)(implicit text: VitalsTextCodec): FeltCubeCollector = {
    internalCollector.normalize(builder, thisCube, thisDictionary, thatCube, thatDictionary)
  }


  override
  def navigate(key: ZapCube2Key): ZapCube2Row = {
    runWithRetry{
      internalCollector.navigate(key)
    }
  }

  @inline override
  def createJoinRow(
                              parentRow: ZapCube2Row,
                              childRow: ZapCube2Row,
                              resultCube: ZapCube2,
                              parentDimensionMask: VitalsBitMapAnyVal,
                              parentAggregationMask: VitalsBitMapAnyVal,
                              childDimensionMask: VitalsBitMapAnyVal,
                              childAggregationMask: VitalsBitMapAnyVal
                            ): ZapCube2Row = {
    runWithRetry{
      internalCollector.createJoinRow(
        parentRow: ZapCube2Row,
        childRow: ZapCube2Row,
        resultCube: ZapCube2,
        parentDimensionMask: VitalsBitMapAnyVal, parentAggregationMask: VitalsBitMapAnyVal,
        childDimensionMask: VitalsBitMapAnyVal, childAggregationMask: VitalsBitMapAnyVal
      )
    }
  }

  override
  def interMerge(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary,
                 thatCube: FeltCubeCollector, thatDictionary: BrioMutableDictionary): Unit = {
    assert(this == thisCube)
    internalCollector.resetDirtyRows()
    runWithRetry{
      internalCollector.interMerge(builder, internalCollector, thisDictionary, thatCube, thatDictionary)
    }
  }

  override
  def intraMerge(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary,
                 thatCube: FeltCubeCollector, thatDictionary: BrioMutableDictionary,
                 dimensionMask: VitalsBitMapAnyVal, aggregationMask: VitalsBitMapAnyVal): Unit = {
    assert(this == thisCube)
    internalCollector.resetDirtyRows()
    runWithRetry {
      internalCollector.intraMerge(builder, internalCollector, thisDictionary, thatCube, thatDictionary, dimensionMask, aggregationMask)
    }
  }

  override
  def dimRead(dimension: Int): BrioPrimitive = {
    internalCollector.dimRead(dimension)
  }

  @inline override
  def dimWrite(): Unit = {
    runWithRetry{
      internalCollector.dimWrite()
    }
  }

  @inline override
  def dimSetNotNull(dimension: Int): Unit = {
    internalCollector.dimSetNotNull(dimension)
  }

  @inline override
  def dimSetNull(dimension: Int): Unit = {
    internalCollector.dimSetNull(dimension)
  }

  @inline override
  def dimWrite(dimension: Int, value: BrioPrimitive): Unit = {
    internalCollector.dimWrite(dimension, value)
  }

  @inline override
  def aggRead(aggregation: Int): BrioPrimitive = {
    runWithRetry{
      internalCollector.aggRead(aggregation)
    }
  }

  @inline override
  def aggSetNull(aggregation: Int): Unit = {
    runWithRetry{
      internalCollector.aggSetNull(aggregation)
    }
  }

  @inline override
  def aggSetNotNull(aggregation: Int): Unit = {
    runWithRetry{
      internalCollector.aggSetNotNull(aggregation)
    }
  }

  @inline override
  def aggWrite(aggregation: Int, value: BrioPrimitive): Unit = {
    runWithRetry{
      internalCollector.aggWrite(aggregation, value)
    }
  }

  @inline override
  def navigate(): Unit = {
    runWithRetry{
      internalCollector.navigate()
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////
  // Older ZapCube interface...still generated by hydra compiler
  /////////////////////////////////////////////////////////////////////////////////////////////
  @inline override
  def writeAggregationNull(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, column: Int): Unit = {
    aggSetNull(column)
  }

  @inline override
  def readAggregationPrimitive(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, aggregation: Int): BrioPrimitive = {
    aggRead(aggregation)
  }

  @inline override
  def writeAggregationPrimitive(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, aggregation: Int, value: BrioPrimitive): Unit = {
    aggWrite(aggregation, value)
  }

  @inline override
  def readAggregationNull(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, aggregation: Int): Boolean = {
    aggIsNull(aggregation)
  }

  @inline override
  def writeDimension(builder: FeltCubeBuilder, thisCube: FeltCubeCollector): Unit = {
    dimWrite()
  }

  @inline override
  def writeDimensionNull(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, dimension: Int): Unit = {
    dimSetNull(dimension)
  }

  @inline override
  def writeDimensionPrimitive(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, dimension: Int, value: BrioPrimitive): Unit = {
    dimWrite(dimension, value)
  }

  /////////////////////////////////////////////////////////////////////////////////////////////
  // methods that should not be called on a proxy
  /////////////////////////////////////////////////////////////////////////////////////////////

  @inline override
  def importCollector(sourceCollector: ZapCube2, sourceItems: TeslaPoolId, builder: ZapCube2Builder): Unit =
    throw VitalsException(s"importCollector should not be called in flex cube!")

  @inline override
  def itemCount_=(count: TeslaPoolId): Unit = ???

  /////////////////////////////////////////////////////////////////////////////////////////////
  // show some state
  /////////////////////////////////////////////////////////////////////////////////////////////
  @inline override
  def printCube(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary): String =
    internalCollector.printCube(builder, thisCube, thisDictionary)

  @inline override
  def printCubeState(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary, msg: String): Unit =
    internalCollector.printCubeState(builder, thisCube, thisDictionary, msg)

  @inline override
  def distribution(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, thisDictionary: BrioMutableDictionary): Double =
    internalCollector.distribution(builder, thisCube, thisDictionary)

  @inline override
  def itemLimited_=(s: Boolean): Unit = internalCollector.itemLimited=s

  @inline override
  def validateRow(row: ZapCube2Row): Boolean = internalCollector.validateRow(row)

  @inline override private[cube2]
  def rowNormalize(thatBuilder: ZapCube2Builder, thatDictionary: BrioFlexDictionary, text: VitalsTextCodec): Unit =
    internalCollector.rowNormalize(thatBuilder, thatDictionary, text)

  //
  @inline
  private
  def runWithRetry[R](body:  => R): R = {
    assert(!internalCollector.rowsLimited)
    var r: R = body
    var runCount = 1
    while (internalCollector.rowsLimited && runCount < 11) {
      coupler.upsize(this.index, rowsCount, internalCollector.builder)
      r = body
      runCount += 1
    }
    if (runCount > 10) {
      log warn s"Too many upsize attempts"
    }
    r
  }
}
