/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.brio.reference

import org.burstsys.brio.model.schema.types._
import org.burstsys.brio.types.BrioPath.{BrioPathKey, BrioPathName}
import org.burstsys.brio.types.BrioTypes.{BrioRelationOrdinal, BrioTypeKey, BrioTypeName}
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.brio.decl.FeltBrioStdDecl
import org.burstsys.felt.model.brio.reference.relation._
import org.burstsys.felt.model.expressions.assign.FeltUpdateOp
import org.burstsys.felt.model.expressions.function.FeltFuncExpr
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.reference.{FeltReference, FeltStdRefResolver}
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.types.FeltType

import scala.language.postfixOps

object FeltBrioStdRef {

  final case
  class FeltBrioStdRefResolver(global: FeltGlobal) extends FeltStdRefResolver[FeltBrioStdDecl] {
    override val resolverName: String = "brio"

    override protected def addResolution(refName: FeltPathExpr, d: FeltBrioStdDecl): FeltReference =
      FeltBrioStdRef(refName, d)

    override protected def addNomination(d: FeltBrioStdDecl): Option[FeltReference] =
      Some(FeltBrioStdRef(d.refName, d))

  }

}

/**
 * A path reference to a brio lattice field (read access to the current [[org.burstsys.brio.blob.BrioBlob]] being scanned)
 */
final case
class FeltBrioStdRef(refName: FeltPathExpr, refDecl: FeltBrioStdDecl) extends FeltBrioRef
  with FeltBrioValScalRef with FeltBrioValMapRef with FeltBrioValVecRef
  with FeltBrioRefScalRef with FeltBrioRefVecRef {

  sync(refName)

  override val nodeName: String = "felt-brio-std-ref"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // local state
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected
  lazy val keyType: BrioTypeKey = refDecl.brioNode.relation.keyEncoding.typeKey

  protected
  lazy val keyTypeName: BrioTypeName = refDecl.brioNode.relation.keyEncoding.typeName

  protected
  lazy val valueType: BrioTypeKey = refDecl.brioNode.relation.valueEncoding.typeKey

  protected
  lazy val valueTypeName: BrioTypeName = refDecl.brioNode.relation.valueEncoding.typeName

  protected
  lazy val relationOrdinal: BrioRelationOrdinal = refDecl.brioNode.relation.relationOrdinal

  lazy val parentPathName: BrioPathName = refDecl.brioNode.parent.pathName

  lazy val parentPathKey: BrioPathKey = refDecl.brioNode.parent.pathKey

  lazy val pathName: BrioPathName = refDecl.brioNode.pathName

  lazy val pathKey: BrioPathKey = refDecl.brioNode.pathKey

  protected
  lazy val parentInstance: String = latticeRelationViaSweepRuntime(parentPathName)

  protected
  lazy val parentInstanceIsNull: String = latticeRelationIsNullViaSweepRuntime(parentPathName)

  protected
  lazy val instanceIsNull: String = latticeRelationIsNullViaSweepRuntime(pathName)

  protected
  lazy val parentStructure: BrioStructure = refDecl.brioNode.parent.relation.referenceStructure

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def canInferTypes: Boolean = {
    true
  }

  override
  def resolveTypes: this.type = {
    feltType = {
      feltType = refDecl.brioNode.relation.relationForm match {
        case BrioValueScalarRelation => FeltType.valScal(valueType)
        case BrioValueMapRelation =>
          if (refName.key.nonEmpty) {
            FeltType.valScal(keyType)
          } else {
            FeltType.valMap(keyType, valueType)
          }
        case BrioValueVectorRelation => FeltType.valVec(valueType)
        case BrioReferenceScalarRelation => FeltType.refScal()
        case BrioReferenceVectorRelation => FeltType.refVec()
        case _ => FeltType.any
      }
    }
    this
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  CODE GENERATION
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def generateReferenceUpdate(op: FeltUpdateOp)(implicit cursor: FeltCodeCursor): FeltCode =
    throw FeltException(location,
      s"brio data is read only can't generate code for ref update '${refName.normalizedSource}'"
    )

  override
  def generateReferenceAssign(implicit cursor: FeltCodeCursor): FeltCode =
    throw FeltException(location,
      s"brio data is read only can't generate code for ref assign '${refName.normalizedSource}'"
    )

  override
  def generateReferenceRead(implicit cursor: FeltCodeCursor): FeltCode = {
    val traversalNode = global.feltSchema.nodeForPathKey(cursor.pathKey)
    refDecl.brioNode.relation.relationForm match {
      case BrioValueScalarRelation => generateValScalRefRead
      case BrioValueMapRelation => generateValMapRefRead
      case BrioValueVectorRelation => generateValVecRefRead
      case BrioReferenceVectorRelation => generateRefVecRefRead
      case BrioReferenceScalarRelation => generateRefScalRefRead
      case _ =>
        throw FeltException(
          refName.location,
          s" read reference for ${
            refDecl.brioNode.relation.relationForm
          } at traversalNode=${
            traversalNode.pathName
          } not implemented")
    }
  }

  override def generateContainsPolyFunc(func: FeltFuncExpr)(implicit cursor: FeltCodeCursor): FeltCode = {
    func.parameterCountAtLeast(2)
    val brioRef = func.parameterAsReferenceOrThrow[FeltBrioStdRef](0, "brio-path")
    val node = brioRef.refDecl
    val form = node.relation.relationForm
    form match {
      case BrioValueScalarRelation =>
        throw FeltException(location, s"value scalar $printSource not supported for contains()")
      case BrioValueVectorRelation =>
        s"""|
            |$I{ // ${M(this)}
            |$I2??? // value vector contains() not implemented
            |$I}""".stripMargin
      case BrioValueMapRelation =>
        s"""|
            |$I{ // ${M(this)}
            |$I2//value map contains() not implemented
            |$I}""".stripMargin
      case _ =>
        throw FeltException(location, s"$form form $printSource not supported for contains()")
    }
  }

  override def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = ???

  override def generatePrepare(implicit cursor: FeltCodeCursor): FeltCode = ???

  override def generateRelease(implicit cursor: FeltCodeCursor): FeltCode = ???
}
