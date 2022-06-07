/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups.enum

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.{FeltCubeDimDecl, FeltDimSemType}
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.source.S
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}

import scala.reflect.ClassTag

/**
 * A Felt compatible ''enum'' dimension AST node
 */
trait FeltCubeDimEnumDecl extends FeltCubeDimDecl {

  final override val nodeName = "felt-cube-enum-dim"

  /**
   *
   * @return
   */
  def enumValues: Array[FeltExpression]

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] = rule(this) ++ enumValues.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = enumValues

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // artifact planning
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override val semanticType: FeltDimSemType = ENUM_DIMENSION_SEMANTIC

  final override lazy val semantic: FeltCubeDimEnumSem = new FeltCubeDimEnumSem {
    override val bType: BrioTypeKey = valueType
    override val columnName: BrioRelationName = FeltCubeDimEnumDecl.this.columnName
    override val semanticRt: FeltCubeDimEnumSemRt =
      valueType match {
        case BrioBooleanKey =>
          FeltCubeDimBoolEnumSemRt(extractValues[Boolean](enumValues))
        case BrioByteKey =>
          FeltCubeDimByteEnumSemRt(extractValues[Byte](enumValues))
        case BrioShortKey =>
          FeltCubeDimShortEnumSemRt(extractValues[Short](enumValues))
        case BrioIntegerKey =>
          FeltCubeDimIntEnumSemRt(extractValues[Int](enumValues))
        case BrioLongKey =>
          FeltCubeDimLongEnumSemRt(extractValues[Long](enumValues))
        case BrioDoubleKey =>
          FeltCubeDimDoubleEnumSemRt(extractValues[Double](enumValues))
        case BrioStringKey =>
          FeltCubeDimStrEnumSemRt(extractValues[String](enumValues))
        case t => throw FeltException(location, s" unknown typekey=$valueType")
      }

  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def reduceStatics: FeltCubeDimEnumDecl = new FeltCubeDimEnumDecl {
    sync(FeltCubeDimEnumDecl.this)
    final override val refName: FeltPathExpr = FeltCubeDimEnumDecl.this.refName
    final override val valueType: BrioTypeKey = FeltCubeDimEnumDecl.this.valueType
    final override val location: FeltLocation = FeltCubeDimEnumDecl.this.location
    final override val enumValues: Array[FeltExpression] = FeltCubeDimEnumDecl.this.enumValues.map(_.resolveTypes.reduceStatics)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"$S'$columnName':${semanticType.name}[${brioDataTypeNameFromKey(valueType).toLowerCase}](${
      enumValues.map(_.normalizedSource).mkString(", ")
    })"

}
