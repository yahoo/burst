/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.types.BrioTypes
import org.burstsys.brio.types.BrioTypes.{BrioRelationName, BrioTypeKey}
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.FeltCubeAggSemRt
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.FeltCubeDimSemRt
import org.burstsys.felt.model.collectors.cube.runtime.{FeltCubeOrdinalMap, FeltCubeTreeMask}
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeBuilderContext}
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemorySize}
import org.burstsys.tesla.part.TeslaPartBuilder

trait ZapCube2Builder extends FeltCubeBuilder with TeslaPartBuilder {

}

object ZapCube2Builder {

  /**
   * constructor for the original  unit test world
   *
   */
  def apply(
             defaultStartSize:TeslaMemorySize = flex.defaultStartSize,
             dimensionCount: Int = 0,
             aggregationCount: Int = 0,
             dimensionFieldNames: Array[BrioRelationName] = Array.empty,
             dimensionFieldTypes: Array[BrioTypeKey] = Array.empty,
             aggregationFieldNames: Array[BrioRelationName] = Array.empty,
             aggregationFieldTypes: Array[BrioTypeKey] = Array.empty,
             aggregationSemantics: Array[FeltCubeAggSemRt] = Array.empty
           ): ZapCube2Builder =
    ZapCube2BuilderContext(
      defaultStartSize = defaultStartSize,

      fieldNames = dimensionFieldNames ++ aggregationFieldNames,

      dimensionCount = dimensionCount: Int,
      dimensionFieldTypes = dimensionFieldTypes: Array[BrioTypeKey],
      dimensionSemantics = null, // TODO

      aggregationCount = aggregationCount: Int,
      aggregationFieldTypes = aggregationFieldTypes: Array[BrioTypeKey],
      aggregationSemantics = aggregationSemantics: Array[FeltCubeAggSemRt]
    )

  /**
   * constructor for the FELT code generation world
   *
   */
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

           ): ZapCube2Builder = ZapCube2BuilderContext(

    rowLimit = rowLimit: Int,

    fieldNames = fieldNames: Array[BrioRelationName],

    dimensionCount = dimensionCount: Int,
    dimensionSemantics = dimensionSemantics: Array[FeltCubeDimSemRt],
    dimensionFieldTypes = dimensionFieldTypes: Array[BrioTypeKey],
    dimensionCubeJoinMask = dimensionCubeJoinMask: FeltCubeTreeMask,

    aggregationCount = aggregationCount: Int,
    aggregationSemantics = aggregationSemantics: Array[FeltCubeAggSemRt],
    aggregationFieldTypes = aggregationFieldTypes: Array[BrioTypeKey],
    aggregationCubeJoinMask = aggregationCubeJoinMask: FeltCubeTreeMask

  )

}

/**
 * parameters for building zap cubes. The cube does '''not''' maintains these in cube state currently.
 * they must be passed in during instantiation and various algorithms. These '''must be''' immutable and thread
 * safe. Though its not space efficient to store these in off heap cube state, it may be time efficient enough
 * to try later.
 */
private final case
class ZapCube2BuilderContext(
                              var defaultStartSize: TeslaMemorySize = flex.defaultStartSize,
                              var rowLimit: Int = 10000,

                              var fieldNames: Array[BrioRelationName] = Array.empty,

                              var dimensionCount: Int = 0,
                              var dimensionSemantics: Array[FeltCubeDimSemRt],
                              var dimensionFieldTypes: Array[BrioTypeKey] = Array.empty,
                              var dimensionOrdinalMap: FeltCubeOrdinalMap = FeltCubeOrdinalMap(), // TODO DEPRECATED GIST ONLY
                              var dimensionCubeJoinMask: FeltCubeTreeMask = FeltCubeTreeMask(),

                              var aggregationCount: Int = 0,
                              var aggregationSemantics: Array[FeltCubeAggSemRt] = Array.empty,
                              var aggregationFieldTypes: Array[BrioTypeKey] = Array.empty,
                              var aggregationOrdinalMap: FeltCubeOrdinalMap = FeltCubeOrdinalMap(), // TODO DEPRECATED GIST ONLY
                              var aggregationCubeJoinMask: FeltCubeTreeMask = FeltCubeTreeMask()

                            ) extends FeltCubeBuilderContext with ZapCube2Builder {

  override val bucketCount: Int = 16

  override def totalMemorySize: TeslaMemoryOffset =
    ???

  override def requiredMemorySize: TeslaMemorySize =
    ???

  /**
   * convert a field name to a column key
   */
  override
  lazy val fieldKeyMap: Map[BrioRelationName, Int] = {
    for (i <- fieldNames.indices) yield fieldNames(i) -> i
  }.toMap

  val hasStringDimensions: Boolean = dimensionFieldTypes.contains(BrioTypes.BrioStringKey)

  val hasStringAggregations: Boolean = aggregationFieldTypes.contains(BrioTypes.BrioStringKey)

  //  val relationTypes: Array[TeslaMemoryOffset] = dimensionFieldTypes ++ aggregationFieldTypes

  override
  def dimensionName(d: Int): String = {
    fieldNames(d)
  }

  override
  def aggregationName(a: Int): String = {
    fieldNames(dimensionCount + a)
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    output.writeInt(defaultStartSize)
    output.writeInt(dimensionCount)
    output.writeInt(aggregationCount)
    output.writeInt(rowLimit)
    kryo.writeClassAndObject(output, dimensionFieldTypes)
    kryo.writeClassAndObject(output, aggregationFieldTypes)
    kryo.writeClassAndObject(output, aggregationSemantics)
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    defaultStartSize = input.readInt
    dimensionCount = input.readInt
    aggregationCount = input.readInt
    rowLimit = input.readInt
    dimensionFieldTypes = kryo.readClassAndObject(input).asInstanceOf[Array[BrioTypeKey]]
    aggregationFieldTypes = kryo.readClassAndObject(input).asInstanceOf[Array[BrioTypeKey]]
    aggregationSemantics = kryo.readClassAndObject(input).asInstanceOf[Array[FeltCubeAggSemRt]]
  }


}
