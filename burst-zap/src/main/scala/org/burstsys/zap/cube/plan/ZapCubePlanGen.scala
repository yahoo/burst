/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.plan

import org.burstsys.brio.types.BrioTypes
import org.burstsys.felt.model.tree.code.{CC, FeltCode, FeltCodeCursor, I, I2, generateIntArrayCode, generateStringArrayCode}
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.FeltCubeAggSemRt
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.FeltCubeDimSemRt

import scala.language.postfixOps

trait ZapCubePlanGen {

  self: ZapCubePlanContext =>

  ////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////

  final
  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |$I${binding.builderClassName}(
        |$I2${builder.rowLimit},   // row limit
        |${CC(s"field names")}
        |${generateStringArrayCode(builder.fieldNames)(cursor indentRight 1)},
        |$I2${builder.dimensionCount}, // dimension count
        |${CC(s"dimension semantics")}
        |${generateDimensionsCode(builder.dimensionSemantics)(manifest[FeltCubeDimSemRt], cursor indentRight 1)},
        |${CC(s"dimension field types")}
        |${CC(s"(${builder.dimensionFieldTypes.map(BrioTypes.brioDataTypeNameFromKey).mkString(", ")})")}
        |${generateIntArrayCode(builder.dimensionFieldTypes)(cursor indentRight 1)},
        |${CC(s"dimension join mask")}${builder.dimensionCubeJoinMask.generateDeclaration(cursor indentRight)},
        |$I2${builder.aggregationCount},   // aggregation count
        |${CC(s"aggregation semantics")}
        |${generateAggregationsCode(builder.aggregationSemantics)(manifest[FeltCubeAggSemRt], cursor indentRight 1)},
        |${CC(s"aggregation field types")}
        |${CC(s"(${builder.aggregationFieldTypes.map(BrioTypes.brioDataTypeNameFromKey).mkString(", ")})")}
        |${generateIntArrayCode(builder.aggregationFieldTypes)(cursor indentRight 1)},
        |${CC(s"aggregation join mask")}${builder.aggregationCubeJoinMask.generateDeclaration(cursor indentRight)}
        |$I).init( ${builder.frameId}, "${builder.frameName}", feltBinding )""".stripMargin
  }

  ////////////////////////////////////////////////////////
  // INTERNAL
  ////////////////////////////////////////////////////////

  private
  def generateAggregationsCode[T <: FeltCubeAggSemRt](generators: Array[T])(implicit m: Manifest[T], cursor: FeltCodeCursor): String = {
    def generatorCode(implicit cursor: FeltCodeCursor): FeltCode = generators.map {
      f =>
        if (f == null)
          s"""|
              |${I}null""".stripMargin
        else
          s"""|
              |${f.generateDeclaration(cursor indentRight)}""".stripMargin

    }.mkString(", ")

    s"""|
        |${I}scala.Array[${m.runtimeClass.getName}](${generatorCode(cursor indentRight)}
        |$I)""".stripMargin
  }

  private
  def generateDimensionsCode[T <: FeltCubeDimSemRt](generators: Array[T])(implicit m: Manifest[T], cursor: FeltCodeCursor): String = {
    def generatorCode(implicit cursor: FeltCodeCursor): FeltCode = generators.map {
      f =>
        if (f == null)
          s"""
             |${I}null""".stripMargin
        else
          s"""|
              |${f.generateDeclaration(cursor indentRight)}""".stripMargin
    }.mkString(", ")

    s"""|
        |${I}scala.Array[${m.runtimeClass.getName}](${generatorCode(cursor indentRight)}
        |$I)""".stripMargin
  }


}
