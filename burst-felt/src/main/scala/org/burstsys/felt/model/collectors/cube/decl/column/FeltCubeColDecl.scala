/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.cube.decl.{FeltCubeColSem, FeltCubeDecl, FeltSemType}
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.literals.primitive.{FeltFixPrimitive, FeltFltPrimitive, FeltStrPrimitive}
import org.burstsys.felt.model.reference.FeltRefDecl
import org.burstsys.felt.model.reference.names.FeltNamedNode
import org.burstsys.felt.model.tree.FeltNode
import org.burstsys.felt.model.types.FeltType

import scala.reflect.ClassTag

/**
 * generic column node used in [[FeltCubeDecl]] metadata
 */
trait FeltCubeColDecl[S <: FeltCubeColSem] extends FeltRefDecl with FeltNamedNode {

  /**
   * TODO
   *
   * @return
   */
  def cubeDecl: FeltCubeDecl = _cubeDecl

  final
  def cubeDecl_=(c: FeltCubeDecl): Unit = _cubeDecl = c

  final lazy
  val cubeName: String = _cubeDecl.cubeName

  final lazy
  val columnName: String = refName.shortName

  /**
   *
   * @return
   */
  def semanticType: FeltSemType

  /**
   *
   * @return
   */
  def semantic: S

  final override lazy val nsName: String = refName.shortName

  /**
   * column ordinal
   * TODO: is this an ordinal within dimensions or aggregations or across the combined set?
   *
   * @return
   */
  def columnOrdinal: Int = _columnOrdinal

  def columnOrdinal_=(o: Int): Unit = _columnOrdinal = o

  /**
   * column value type
   *
   * @return
   */
  def valueType: BrioTypeKey

  final
  def sync(node: FeltCubeColDecl[S]): Unit = {
    super.sync(node)
    _cubeDecl = node.cubeDecl
    _columnOrdinal = node.columnOrdinal
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _cubeDecl: FeltCubeDecl = _

  private[this]
  var _columnOrdinal: Int = _

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] = rule(this)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def reduceStatics: FeltCubeColDecl[S] = this

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def canInferTypes: Boolean = true

  override
  def resolveTypes: this.type = {
    feltType = FeltType.valScal(valueType)
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // internal
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def extractValues[T <: BrioDataType : ClassTag](inputs: Array[FeltExpression]): Array[T] = {
    inputs.foreach {
      v => v.reduceToLiteral.getOrElse(throw FeltException(location, s"$v cannot reduce to constant"))
    }
    inputs.map {
      v =>
        v.reduceToLiteral match {
          case Some(literal) => literal match {
            case l: FeltFixPrimitive => valueType match {
              case BrioByteKey => l.value.toByte.asInstanceOf[T]
              case BrioShortKey => l.value.toShort.asInstanceOf[T]
              case BrioIntegerKey => l.value.toInt.asInstanceOf[T]
              case BrioLongKey => l.value.asInstanceOf[T]
              case _ => throw FeltException(location, s"")
            }
            case l: FeltFltPrimitive => l.value.asInstanceOf[T]
            case l: FeltStrPrimitive => l.value.asInstanceOf[T]
            case e => throw FeltException(location, s"unknown primitive literal $e for enum $this")
          }
          case None => throw FeltException(location, s"irreducible expression value $v in enum $this")
        }
    }
  }

}
