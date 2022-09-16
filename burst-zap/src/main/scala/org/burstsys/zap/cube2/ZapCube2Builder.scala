/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.types.BrioTypes
import org.burstsys.brio.types.BrioTypes.{BrioRelationName, BrioTypeKey}
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.FeltCubeAggSemRt
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.FeltCubeDimSemRt
import org.burstsys.felt.model.collectors.cube.runtime.FeltCubeTreeMask
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeBuilderContext}
import org.burstsys.tesla.TeslaTypes.{SizeOfLong, TeslaMemoryOffset, TeslaMemorySize}
import org.burstsys.tesla.part.TeslaPartBuilder
import org.burstsys.zap.cube2.row.ZapCube2Row

trait ZapCube2Builder extends FeltCubeBuilder with TeslaPartBuilder {
  def neededSize(itemCount: Int): TeslaMemorySize
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
      declaredDefaultStartSize = defaultStartSize,

      fieldNames = dimensionFieldNames ++ aggregationFieldNames,

      dimensionCount = dimensionCount: Int,
      dimensionFieldTypes = dimensionFieldTypes: Array[BrioTypeKey],
      dimensionSemantics = Array.empty, // TODO

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
                              var declaredDefaultStartSize: TeslaMemorySize = flex.defaultStartSize,
                              var rowLimit: Int = 100,

                              var fieldNames: Array[BrioRelationName] = Array.empty,

                              var dimensionCount: Int = 0,
                              var dimensionSemantics: Array[FeltCubeDimSemRt],
                              var dimensionFieldTypes: Array[BrioTypeKey] = Array.empty,
                              var dimensionCubeJoinMask: FeltCubeTreeMask = FeltCubeTreeMask(),

                              var aggregationCount: Int = 0,
                              var aggregationSemantics: Array[FeltCubeAggSemRt] = Array.empty,
                              var aggregationFieldTypes: Array[BrioTypeKey] = Array.empty,
                              var aggregationCubeJoinMask: FeltCubeTreeMask = FeltCubeTreeMask()

                            ) extends FeltCubeBuilderContext with ZapCube2Builder {

  def defaultStartSize: TeslaMemorySize = neededSize(rowLimit)

  //override val bucketCount: Int = 16
  @inline override
  def bucketCount(maxRowCount: Int): Int = {
    maxRowCount match {
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

  @inline
  lazy val rowSize: TeslaMemoryOffset = ZapCube2Row.byteSize(dimensionCount, aggregationCount)


  override def totalMemorySize: TeslaMemoryOffset =
    ???

  override def requiredMemorySize: TeslaMemorySize =
    ???

  def neededSize(itemCount: Int): TeslaMemorySize = {
    if (itemCount <= 0)
      defaultStartSize
    else
      state.SizeofFixedSizeHeader + bucketCount(itemCount)*SizeOfLong + itemCount*rowSize
  }
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

  override
  def toString: String =
    f"""
       |ZapCubeBuilder(frameId=$frameId, frameName=$frameName, rowLimit=$rowLimit,
       |   fieldNames: ${fieldNames.mkString("[",",","]")}
       |     row:
       |          limit=$rowLimit%,d, size=$rowSize%,d, blockSize=${rowSize * rowLimit}%,d
       |   dimensions:
       |       dimensionCount=$dimensionCount%,d, semantics=${if (dimensionSemantics != null) dimensionSemantics.mkString("{'", "', '", "'}") else "NONE"}
       |       types=${dimensionFieldTypes.mkString("{'", "', '", "'}")}, mask=$dimensionCubeJoinMask
       |   aggregations:
       |       aggregationCount=$aggregationCount%,d, semantics=${if (aggregationSemantics != null) aggregationSemantics.mkString("{'", "', '", "'}") else "NONE"}
       |       types=${aggregationFieldTypes.mkString("{'", "', '", "'}")}, mask=$aggregationCubeJoinMask
       |)
       """.stripMargin

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    output.writeInt(dimensionCount)
    output.writeInt(aggregationCount)
    output.writeInt(rowLimit)
    kryo.writeClassAndObject(output, fieldNames)
    kryo.writeClassAndObject(output, dimensionFieldTypes)
    kryo.writeClassAndObject(output, dimensionSemantics)
    dimensionCubeJoinMask.write(kryo, output)
    kryo.writeClassAndObject(output, aggregationFieldTypes)
    kryo.writeClassAndObject(output, aggregationSemantics)
    aggregationCubeJoinMask.write(kryo, output)
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    dimensionCount = input.readInt
    aggregationCount = input.readInt
    rowLimit = input.readInt
    fieldNames = kryo.readClassAndObject(input).asInstanceOf[Array[BrioRelationName]]
    dimensionFieldTypes = kryo.readClassAndObject(input).asInstanceOf[Array[BrioTypeKey]]
    dimensionSemantics = kryo.readClassAndObject(input).asInstanceOf[Array[FeltCubeDimSemRt]]
    dimensionCubeJoinMask = FeltCubeTreeMask(kryo, input)
    aggregationFieldTypes = kryo.readClassAndObject(input).asInstanceOf[Array[BrioTypeKey]]
    aggregationSemantics = kryo.readClassAndObject(input).asInstanceOf[Array[FeltCubeAggSemRt]]
    aggregationCubeJoinMask = FeltCubeTreeMask(kryo, input)
  }


}
