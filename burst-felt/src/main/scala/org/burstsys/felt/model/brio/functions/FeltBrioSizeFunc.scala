/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.brio.functions

import org.burstsys.felt.model.brio.reference.FeltBrioStdRef
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.sweep.symbols
import org.burstsys.felt.model.sweep.symbols.{blobReaderSym, brioSchemaSym, latticeRelationViaSweepRuntime}
import org.burstsys.felt.model.tree.code.{FeltCodeCursor, I, I2, I3, _}
import org.burstsys.felt.model.types.FeltType

object FeltBrioSizeFunc {
  final val functionName: String = "size"
}

/**
 * return the fixed ''size'' of a datatype provided in the single parameter
 */
trait FeltBrioSizeFunc extends FeltBrioFunction {

  final override val nodeName = "felt-brio-size-func"

  final override val usage: String =
    s"""
       |usage: $functionName(<path_to_relation>) -> fixed_literal
     """.stripMargin

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    feltType = FeltType.valScal[Long]
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    parameterCountIs(1)
    val brioRef = parameterAsReferenceOrThrow[FeltBrioStdRef](0, "brio-path")
    val decl = brioRef.refDecl
    val parentRelation = decl.brioNode.parent.relation

    val parentStructureKey = parentRelation.referenceStructure.structureTypeKey
    val parentPath = decl.brioNode.parent.pathName
    val parentInstanceIsNull: String = symbols.latticeRelationIsNullViaSweepRuntime(parentPath)

    s"""|
        |${T(this)}
        |${I}if($parentInstanceIsNull) { ${cursor.callScope.scopeNull} = true; } else {
        |${I2}val instance = ${latticeRelationViaSweepRuntime(parentPath)};
        |${I2}val schematic = $brioSchemaSym.schematic($parentStructureKey, instance.versionKey($blobReaderSym));
        |${I2}if( instance.relationIsNull($blobReaderSym, schematic, ${decl.relation.relationOrdinal}) ) { ${cursor.callScope.scopeNull} = true; } else {
        |$I3${cursor.callScope.scopeVal} = instance.relationSize($blobReaderSym, schematic, ${decl.relation.relationOrdinal});
        |$I2}
        |$I}""".stripMargin

  }

}
