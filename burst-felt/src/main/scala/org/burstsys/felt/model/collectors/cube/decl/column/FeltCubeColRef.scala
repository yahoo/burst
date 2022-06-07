/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.FeltCollectorRef
import org.burstsys.felt.model.expressions.assign.FeltUpdateOp
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.types.FeltType
import org.burstsys.felt.model.types.FeltType.valScal

import scala.language.postfixOps


trait FeltCubeColRef extends AnyRef with FeltCollectorRef {

  override def refDecl: FeltCubeColDecl[_]

  final def cubeName: String = refDecl.cubeName

  final override val isMutable: Boolean = true

  final lazy val relationType: BrioTypeKey = refDecl.valueType

  final lazy val relationName: BrioRelationName = refDecl.nameSpace.absoluteName

  final lazy val relationOrdinal: BrioRelationOrdinal = refDecl.columnOrdinal

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = refDecl.canInferTypes

  final override
  def resolveTypes: this.type = {
    if (!canInferTypes) return this
    relationName // for debugging
    relationOrdinal // for debugging
    feltType = relationType match {
      case BrioBooleanKey => valScal[Boolean]
      case BrioByteKey => valScal[Byte]
      case BrioShortKey => valScal[Short]
      case BrioIntegerKey => valScal[Int]
      case BrioLongKey => valScal[Long]
      case BrioDoubleKey => valScal[Double]
      case BrioStringKey => valScal[String]
      case BrioUnitTypeKey => FeltType.unit
      case _ => throw FeltException(location, s"")
    }
    this
  }

  final override
  def generateReferenceUpdate(op: FeltUpdateOp)(implicit cursor: FeltCodeCursor): FeltCode = ???

  final override
  def generateReferenceRead(implicit cursor: FeltCodeCursor): FeltCode =
    throw FeltException(refName.location, s"does not makes sense to read from a cube '${refName.fullPathAndKeyNoQuotes}'")

}
