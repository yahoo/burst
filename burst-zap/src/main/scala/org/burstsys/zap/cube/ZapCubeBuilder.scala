/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeBuilderContext}
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.FeltCubeAggSemRt
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.FeltCubeDimSemRt
import org.burstsys.felt.model.collectors.cube.plane.{FeltCubePlane, FeltCubePlaneContext}
import org.burstsys.felt.model.collectors.cube.runtime.{FeltCubeOrdinalMap, FeltCubeTreeMask}
import org.burstsys.felt.model.collectors.runtime.FeltCollectorPlane
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemorySize}
import org.burstsys.tesla.part.TeslaPartBuilder
import org.burstsys.vitals.errors.{VitalsException, safely}

/**
 *
 */
trait ZapCubeBuilder extends FeltCubeBuilder with TeslaPartBuilder {

  /**
   *
   * @return
   */
  def linkOffset: TeslaMemoryOffset

  /**
   *
   * @return
   */
  def bucketBlockSize: TeslaMemoryOffset

  /**
   *
   * @return
   */
  def firstAggregationPointerOffset: TeslaMemoryOffset

  /**
   * The byte size per row
   */
  def rowSize: TeslaMemoryOffset

}

object ZapCubeBuilder {

  def apply(): ZapCubeBuilder = ZapCubeBuilderContext(
    // global...
    0, null,
    // dimensions...
    0, null, null, FeltCubeOrdinalMap(), FeltCubeTreeMask(),
    // aggregations...
    0, null, null, FeltCubeOrdinalMap(), FeltCubeTreeMask()
  )

  def apply(
             rowLimit: Int,
             fieldNames: Array[BrioRelationName],

             // dimensions...
             dimensionCount: Int,
             dimensionSemantics: Array[FeltCubeDimSemRt],
             dimensionFieldTypes: Array[BrioTypeKey],
             dimensionOrdinalMap: FeltCubeOrdinalMap,
             dimensionCubeJoinMask: FeltCubeTreeMask,

             // aggregations...
             aggregationCount: Int,
             aggregationSemantics: Array[FeltCubeAggSemRt],
             aggregationFieldTypes: Array[BrioTypeKey],
             aggregationOrdinalMap: FeltCubeOrdinalMap,
             aggregationCubeJoinMask: FeltCubeTreeMask

           ): ZapCubeBuilder = ZapCubeBuilderContext(
    rowLimit: Int,
    fieldNames: Array[BrioRelationName],
    dimensionCount: Int,
    dimensionSemantics: Array[FeltCubeDimSemRt],
    dimensionFieldTypes: Array[BrioTypeKey],
    dimensionOrdinalMap: FeltCubeOrdinalMap,
    dimensionCubeJoinMask: FeltCubeTreeMask,
    aggregationCount: Int,
    aggregationSemantics: Array[FeltCubeAggSemRt],
    aggregationFieldTypes: Array[BrioTypeKey],
    aggregationOrdinalMap: FeltCubeOrdinalMap,
    aggregationCubeJoinMask: FeltCubeTreeMask
  )
  def apply(
             rowLimit: Int,
             fieldNames: Array[BrioRelationName],

             // dimensions...
             dimensionCount: Int,
             dimensionSemantics: Array[FeltCubeDimSemRt],
             dimensionFieldTypes: Array[BrioTypeKey],
             dimensionCubeJoinMask: FeltCubeTreeMask,

             // aggregations...
             aggregationCount: Int,
             aggregationSemantics: Array[FeltCubeAggSemRt],
             aggregationFieldTypes: Array[BrioTypeKey],
             aggregationCubeJoinMask: FeltCubeTreeMask

           ): ZapCubeBuilder = ZapCubeBuilderContext(
    rowLimit: Int,
    fieldNames: Array[BrioRelationName],
    dimensionCount: Int,
    dimensionSemantics: Array[FeltCubeDimSemRt],
    dimensionFieldTypes: Array[BrioTypeKey],
    FeltCubeOrdinalMap(),
    dimensionCubeJoinMask: FeltCubeTreeMask,
    aggregationCount: Int,
    aggregationSemantics: Array[FeltCubeAggSemRt],
    aggregationFieldTypes: Array[BrioTypeKey],
    FeltCubeOrdinalMap(),
    aggregationCubeJoinMask: FeltCubeTreeMask
  )
}

/**
 * The definition of all characteristics and typing of a cube. This can be shared across all instances of cubes
 * since they all share the same super schema
 */
private final case
class ZapCubeBuilderContext(
                             // global...
                             var rowLimit: Int,
                             var fieldNames: Array[BrioRelationName],

                             // dimensions...
                             var dimensionCount: Int,
                             var dimensionSemantics: Array[FeltCubeDimSemRt],
                             var dimensionFieldTypes: Array[BrioTypeKey],
                             var dimensionOrdinalMap: FeltCubeOrdinalMap,
                             var dimensionCubeJoinMask: FeltCubeTreeMask,

                             // aggregations...
                             var aggregationCount: Int,
                             var aggregationSemantics: Array[FeltCubeAggSemRt],
                             var aggregationFieldTypes: Array[BrioTypeKey],
                             var aggregationOrdinalMap: FeltCubeOrdinalMap,
                             var aggregationCubeJoinMask: FeltCubeTreeMask

                           ) extends FeltCubeBuilderContext with  ZapCubeBuilder {

  override def defaultStartSize: TeslaMemorySize = ???
  override def requiredMemorySize: TeslaMemorySize = ???

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  @inline override
  def hasStringAggregations: Boolean = _hasStringAggregations

  @inline override
  def hasStringDimensions: Boolean = _hasStringDimensions

  @inline override
  def totalMemorySize: TeslaMemoryOffset = bucketBlockSize + rowBlockSize

  @inline override
  def linkOffset: TeslaMemoryOffset = rowSize - LinkColumnSize

  @inline override
  lazy val bucketBlockSize: TeslaMemoryOffset = bucketCount * BucketFieldSize

  @inline override
  lazy val firstAggregationPointerOffset: TeslaMemoryOffset =
    DimensionNullMapSize + AggregationNullMapSize + (DimensionColumnSize * dimensionCount)

  /**
   * convert a field name to a column key
   */
  @inline override
  def fieldKeyMap: Map[BrioRelationName, Int] = {
    for (i <- fieldNames.indices) yield fieldNames(i) -> i
  }.toMap

  @inline override
  def bucketCount: Int = {
    rowLimit match {
      case n if n == 1 => 1;
      case n if n < 10 => 3;
      case n if n < 50 => 7;
      case n if n < 100 => 11;
      case n if n < 500 => 23;
      case n if n < 1000 => 43;
      case n if n < 5000 => 83;
      case n if n < 10000 => 103;
      case _ => 103
    }
  }

  /**
   * The byte size per row
   */
  @inline override
  lazy val rowSize: TeslaMemoryOffset = (
    DimensionNullMapSize // aggregation nulls...
      + AggregationNullMapSize // dimension nulls...
      + (DimensionColumnSize * dimensionCount)
      + (AggregationColumnSize * aggregationCount)
      + LinkColumnSize
    )

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _hasStringDimensions: Boolean = false

  private[this]
  var _hasStringAggregations: Boolean = false

  /**
   * The byte size for all rows
   */
  private[this]
  lazy val rowBlockSize: TeslaMemoryOffset = rowLimit * rowSize

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  //  private implementation
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  if (dimensionFieldTypes != null)
    initFlags

  @inline private[zap]
  def initFlags: ZapCubeBuilder = {
    var i = 0
    while (i < dimensionFieldTypes.length) {
      if (dimensionFieldTypes(i) == BrioStringKey) {
        _hasStringDimensions = true
      }
      i += 1
    }
    var j = 0
    while (j < aggregationFieldTypes.length) {
      if (aggregationFieldTypes(j) == BrioStringKey) {
        _hasStringAggregations = true
      }
      j += 1
    }
    this
  }

  override
  def dimensionName(d: Int): String = {
    fieldNames(d)
  }

  override
  def aggregationName(a: Int): String = {
    fieldNames(dimensionCount + a)
  }

  override
  def toString: String =
    f"""
       |ZapCubeBuilder(frameId=$frameId, frameName=$frameName, rowLimit=$rowLimit,
       |   row:
       |        limit=$rowLimit%,d, size=$rowSize%,d, blockSize=$rowBlockSize%,d
       |   dimensions:
       |       dimensionCount=$dimensionCount%,d, semantics=${dimensionSemantics.mkString("{'", "', '", "'}")}
       |       types=${dimensionFieldTypes.mkString("{'", "', '", "'}")}, mask=$dimensionCubeJoinMask
       |       ordinalMap=$dimensionOrdinalMap
       |   aggregations:
       |       aggregationCount=$aggregationCount%,d, semantics=${aggregationSemantics.mkString("{'", "', '", "'}")}
       |       types=${aggregationFieldTypes.mkString("{'", "', '", "'}")}, mask=$aggregationCubeJoinMask
       |       ordinalMap=$aggregationOrdinalMap
       |)
       """.stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    try {
      super.write(kryo, output)
      // global
      output writeInt rowLimit
      kryo.writeClassAndObject(output, fieldNames)

      // dimensions
      output writeInt dimensionCount
      kryo.writeClassAndObject(output, dimensionSemantics)
      kryo.writeClassAndObject(output, dimensionFieldTypes)
      dimensionCubeJoinMask.write(kryo, output)
      dimensionOrdinalMap.write(kryo, output)

      // aggregations
      output writeInt aggregationCount
      kryo.writeClassAndObject(output, aggregationSemantics)
      kryo.writeClassAndObject(output, aggregationFieldTypes)
      aggregationCubeJoinMask.write(kryo, output)
      aggregationOrdinalMap.write(kryo, output)

    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    try {
      super.read(kryo, input)
      rowLimit = input.readInt
      fieldNames = kryo.readClassAndObject(input).asInstanceOf[Array[BrioRelationName]]

      // dimensions
      dimensionCount = input.readInt
      dimensionSemantics = kryo.readClassAndObject(input).asInstanceOf[Array[FeltCubeDimSemRt]]
      dimensionFieldTypes = kryo.readClassAndObject(input).asInstanceOf[Array[BrioTypeKey]]
      dimensionCubeJoinMask = FeltCubeTreeMask(kryo, input)
      dimensionOrdinalMap = FeltCubeOrdinalMap(kryo, input)

      // aggregation
      aggregationCount = input.readInt
      aggregationSemantics = kryo.readClassAndObject(input).asInstanceOf[Array[FeltCubeAggSemRt]]
      aggregationFieldTypes = kryo.readClassAndObject(input).asInstanceOf[Array[BrioTypeKey]]
      aggregationCubeJoinMask = FeltCubeTreeMask(kryo, input)
      aggregationOrdinalMap = FeltCubeOrdinalMap(kryo, input)

      initFlags
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

}
