/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube

import com.esotericsoftware.kryo.KryoSerializable
import org.burstsys.fabric.execution.model.result.row.FabricDataKeyAnyVal
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.block.TeslaBlockPart
import org.burstsys.tesla.pool.{TeslaPoolId, TeslaPooledResource}
import org.burstsys.zap.cube.algorithms._
import org.burstsys.zap.cube.state.{ZapCubeExtractor, _}

/**
 * As we move zap cubes to value class, we should separate out the public from the private stuff
 * and put the public API here. This is a universal ''pure'' trait.
 * Currently this is not a real interface as we have yet to rejigger external usage.
 * We need to slowly move all external references to be to  [[ZapCube]] and not [[ZapCubeContext]]
 */
/** == ZAP the 'Zero Allocation Protocol'  Cube Multidimensional result set ==
 * This is a 2 dimension memory array of dimensional/aggregating rows. The memory array is allocated once as a single
 * block/plane of memory and all other operations on it require no object allocations. All operations on the plane
 * consist purely of unsafe read/write intrinsics. A plane of memory with the same schema can be easily cached and
 * reused without any additional object allocations. This means that a plane can be allocated for each thread in
 * a worker pool and even though there may be billions of traversals, no additional object allocations are made. This
 * single plane of memory design also is ideal for off-heap 'unsafe' access models. There is a very small amount of
 * on heap allocation at creation time, but all other memory is outside the GC world and cause no churn.
 * <ul>
 * <li>This is a single threaded structure.</li>
 * <li>Currently there is a numeric limit of MaxInt / 2 rows (2 Billion or 2,147,483,648). Clearly that is impractical for other reasons</li>
 * <li>We allow nulls for dimensions and aggregations. The latter semantic is not yet completely nailed down since we start aggregations at zero</li>
 * <li>This structure is meant to be cached and re-used via the reset method.</li>
 * <li>This is a fixed row size structure. We use this to '''limit''' the size of the result set</li>
 * <li>This structure must be ''released'' when no longer needed since it has non GC memory (off heap)</li>
 * <li>We support a maximum of __64__ total dimensions and aggregations though we could eventually support more.</li>
 * <li>There is a finite limit to the number of rows but currently it seems absurdly high so not to worry.</li>
 * <li>For simplicity, everything is a long including dimensions, aggregations, pointers, null maps.</li>
 * </ul>
 * ==Bucket Structure==
 * The bucket structure is a fixed array of longs each of which is a pointer to the first row in the bucket list. This
 * is placed at the start of our off heap storage block and accessed via ''unsafe'' operators.
 * {{{
 *      | B0 | B1 | ... | Bn |
 * }}}
 * ==Row Structure==
 * This is the zap block layout for Rows '''R0''' through '''Rk''', Dimensions '''D0''' through '''Di''',
 * Aggregations '''A1''' through '''Aj'''. There is a '''Null''' column to allow for nulls in dimension
 * columns and a '''Nxt''' column that points to the next row in the bucket list. Nulls are __not__ allowed in
 * aggregation columns. They default to zero (or in the case of strings to an empty string)
 * {{{
 *          | NULL | D0 | D1 | ... | Di | A0 | A1 | ... | Aj | Nxt |
 *      R0  |______|____|____| ... |____|____|____| ... |____|_____|
 *      R1  |______|____|____| ... |____|____|____| ... |____|_____|
 *          .......................................................
 *      Rk  |______|____|____| ... |____|____|____| ... |____|_____|
 * }}}
 * This is allocated off-heap and accessed via ''unsafe'' operators.
 *
 * ==Row Lookup==
 * Like any hash map type strategy, we take the incoming key and produce a hash code from it,  do a modulo bucket-size
 * on that value and get a bucket for that hash code. That bucket contains an unsafe pointer to the location in off heap
 * memory of the beginning of the first row in the bucket list or 0 if the bucket is empty. If there is at least one
 * row in the bucket list, then we traverse that bucket list matching dimensions as we go to find a matching row
 * (if it exists). If it does not exist, we create a new row and link it into the bucket list.
 * == Row Creation==
 * We create new rows at the end of the zap block.
 * == Reset ==
 * We zero out the bucket structure and set the first row to -1
 * == Serialization ==
 * The final zap contains all the resulting rows with all their dimensions and aggregations. It can be serialized as
 * a compressed array of longs. We do not transfer the buckets, the null maps, and the row pointers since we are returning
 * only the row contents, not the wiring for the hash map functions.
 * == Joins ==
 * We are able to join zaps together by scanning their dimensions and creating missing rows and aggregating matching
 * rows. We also do a cross join where any dimensions are null (unknown/unset)
 * == NOTE ==
 * This is staged to move to an implementation that is a single Value class with a single off heap chunk per map.
 * All interfaces are _universal traits_ i.e. they extends Any and have no state. We just have to take this class
 * and move all state into fields in the off-heap block.
 */

trait ZapCube extends Any with FeltCubeCollector with TeslaBlockPart with TeslaPooledResource {

  /**
   * internal count of 'buckets'
   * @return
   */
  def bucketCount: Int

  def lastRow: Int

  def rowBlockOffset: ZapMemoryOffset

  def row(builder: FeltCubeBuilder, thisCube: FeltCubeCollector, rowIndex: Int): ZapCubeRow

  def rowSize: TeslaMemorySize

  def bucketBlockSize: TeslaMemorySize

  def cubeDataStart: TeslaMemoryPtr

  /**
   * we re-use a single array in order to limit allocations
   * Each key is a null map long and a long for each dimension. There is just one
   * of these per cube (clearly this should go off-heap embedded into the memory chunk)
   */
  def keyData: Array[Long]

  /**
   * make keyData an initialized version of itself (no object creation)
   * @return
   */
  def freshKeyData: Array[Long]

  /**
   * the cursor that tracks the current state of dimension updates. There is just one
   * of these per cube
   */
  def cursorKey: FabricDataKeyAnyVal

  /**
   *
   * @return
   */
  def cursorKeyLength: Int

  /**
   * track updates to the cursor. We start out assuming the cursor is updated
   */
  def cursorUpdated: Boolean

  /**
   * current row based on last navigation
   */
  def cursorRow: ZapCubeRow

}

final
class ZapCubeContext(val blockPtr: TeslaMemoryPtr, val poolId: TeslaPoolId) extends AnyRef with ZapCube
  with KryoSerializable
  with ZapCubeNavigator with ZapCubeJoiner with ZapCubeDimensioner with ZapCubeAggregator
  with ZapCubePrinter with ZapCubeIterator with ZapCubeCodec with ZapCubeMerger with ZapCubeState
  with ZapCubeBuckets with ZapCubeRower with ZapCubeExtractor with ZapCubeNormalizer
  with ZapCubeTopper with Comparable[ZapCubeContext] {


  override def toString: String = s"CUBE(blockPtr=$blockPtr, rowCount=$rowCount)"

  override
  def compareTo(o: ZapCubeContext): Int = blockPtr.compareTo(o.blockPtr)

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline override
  def clear(): Unit = {
    lastRow = ZapCubeZeroRows
    initBuckets(bucketBlockAddress)
  }

  /**
   * Called when cube is first allocated
   *
   * @return
   */
  @inline
  def initialize(builder: FeltCubeBuilder, thisCube: FeltCubeCollector): ZapCubeContext = {
    configure(builder)
    initBuckets(cubeDataStart)
    initCursor(builder, thisCube)
    this
  }

  /**
   * @param builder
   * @return
   */
  @inline private
  def configure(builder: FeltCubeBuilder): ZapCubeContext = {
    cursorKeyLength = builder.dimensionCount + 1
    bucketCount = builder.bucketCount
    rowLimited = false

    // with rows all we need to do is set the row creation pointer to initial state
    lastRow = ZapCubeZeroRows

    // these happen to be the same - should keep them separate though
    rowBlockOffset = builder.asInstanceOf[ZapCubeBuilder].bucketBlockSize
    bucketBlockSize = builder.asInstanceOf[ZapCubeBuilder].bucketBlockSize

    rowSize = builder.asInstanceOf[ZapCubeBuilder].rowSize

    this
  }

}
