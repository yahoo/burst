/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.coerce

import org.burstsys.brio.types.BrioTypes.{BrioRelationName, BrioTypeKey, brioDataTypeNameFromKey}
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.{FeltCubeDimDecl, FeltDimSemType}
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltLocation
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.tree.source.S

/**
 * A Felt compatible ''coerce'' dimension AST node
 */
trait FeltCubeDimCoerceDecl extends FeltCubeDimDecl {

  final override val nodeName = "felt-cube-coerce-dim"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // artifact planning
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override val semanticType: FeltDimSemType = COERCE_DIMENSION_SEMANTIC

  final override lazy val semantic: FeltCubeDimCoerceSem = new FeltCubeDimCoerceSem {
    override val bType: BrioTypeKey = FeltCubeDimCoerceDecl.this.valueType
    override val columnName: BrioRelationName = FeltCubeDimCoerceDecl.this.columnName
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def reduceStatics: FeltCubeDimCoerceDecl = new FeltCubeDimCoerceDecl {
    sync(FeltCubeDimCoerceDecl.this)
    final override val refName: FeltPathExpr = FeltCubeDimCoerceDecl.this.refName
    final override val valueType: BrioTypeKey = FeltCubeDimCoerceDecl.this.valueType
    final override val location: FeltLocation = FeltCubeDimCoerceDecl.this.location
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def generateCode(implicit cursor: FeltCodeCursor): FeltCode = {
    s""
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"$S'$columnName':${semanticType.name}[${brioDataTypeNameFromKey(valueType).toLowerCase}]"

}
