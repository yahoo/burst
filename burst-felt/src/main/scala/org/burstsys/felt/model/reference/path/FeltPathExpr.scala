/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.reference.path

import org.burstsys.brio.types.BrioPath.BrioPathName
import org.burstsys.brio.types.BrioTypes.BrioStringKey
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.expressions.bool.FeltBoolExpr
import org.burstsys.felt.model.reference.FeltReference
import org.burstsys.felt.model.reference.names.ROOT_NAME
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor}
import org.burstsys.felt.model.tree.source.S
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}
import org.burstsys.felt.model.types.FeltType
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.strings

import scala.reflect.{ClassTag, classTag}

/**
 * A Path is a 'name' that can be used to give a name to an artifact in the AST. It can be used
 * to declare an artifact or refer to the artifact in an expression
 */
trait FeltPathExpr extends FeltExpression with FeltBoolExpr {

  final override val nodeName = "felt-path-expr"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _reference: Option[FeltReference] = None

  private[this]
  var _isPassive: Boolean = false

  private[this]
  var _absolutePath: String = _

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Path manipulation
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def absolutePath: String = _absolutePath

  def absolutePath_=(path: String): Unit = _absolutePath = path

  def absolutePathSansRoot: String = absolutePath.stripPrefix(ROOT_NAME + ".")

  /**
   * the non-key '.' separated components of this path
   */
  def components: Array[String]

  final
  def shortName: String = strings.extractStringLiteral(components.last)

  /**
   * the optional key component in this path
   */
  def key: Option[FeltExpression]

  /**
   * a string normalized version of the components in the path
   */
  final
  def fullPath: BrioPathName = components.foldRight("")(_ + "." + _).stripSuffix(".").trim

  /**
   * the entire base path without quotes
   *
   * @return
   */
  final
  def fullPathNoQuotes: BrioPathName =
    components.foldRight("")(_ + "." + _).stripSuffix(".").
      replaceAll("'", "").replaceAll("\"", "").trim

  final
  def fullPathAndKey: BrioPathName = s"$fullPath${
    key match {
      case None => ""
      case Some(k) => s"[${k.normalizedSource}]"
    }
  }"

  final
  def fullPathAndKeyNoQuotes: BrioPathName = s"$fullPathNoQuotes${
    key match {
      case None => ""
      case Some(k) => s"[${k.normalizedSource}]"
    }
  }"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final
  def sync(node: FeltPathExpr): Unit = {
    super.sync(node)
    reference = node.reference
    feltType = node.feltType
    this._absolutePath = node.absolutePath
  }


  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] =
    rule(this) ++ _reference.treeApply(rule) ++ key.treeApply(rule)

  final override
  def children: Array[_ <: FeltNode] = _reference.asArray ++ key.asArray

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // equality
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def hashCode(): Int = fullPathAndKeyNoQuotes.hashCode()

  final override
  def equals(obj: Any): Boolean = {
    obj match {
      case that: FeltPathExpr => that.fullPathNoQuotes == this.fullPathNoQuotes
      case _ => false
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // References
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * all paths are turned into references during __scopeReferences__ phase
   *
   * @return
   */
  final
  def reference: Option[FeltReference] = _reference

  final
  def reference_=(r: Option[FeltReference]): Unit = _reference = r

  final
  def referenceGetOrThrow[R <: FeltReference : ClassTag]: R = {
    _reference match {
      case None =>
        throw VitalsException(s"no reference bound to path $fullPathNoQuotes")
      case Some(r) => r match {
        case ref: R => ref.asInstanceOf[R]
        case ref =>
          throw VitalsException(
            s"reference of wrong type ${
              classTag[R].runtimeClass.getSimpleName
            }, ${
              ref.getClass.getSimpleName
            } expected for path $fullPathNoQuotes"
          )
      }
    }
  }

  final
  def referenceType[R <: FeltReference : ClassTag]: Option[R] =
    _reference match {
      case None => None
      case Some(r) => r match {
        case ref: R => Some(ref)
        case _ => None
      }
    }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // attributes
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * True if this path is not to be resolved via a reference...
   *
   * @return
   */
  def isPassive: Boolean = _isPassive

  /**
   * True if this path is not to be resolved via a reference...
   *
   * @param state
   */
  final
  def isPassive_=(state: Boolean): Boolean = {
    _isPassive = state
    _isPassive
  }

  /**
   * Does this path represent an underlying artifact that is mutable?
   *
   * @return
   */
  final
  def isMutable: Boolean = reference.getOrElse(
    throw FeltException(location, s"reference '$fullPathNoQuotes' not installed yet")
  ).isMutable

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def canInferTypes: Boolean = _reference.exists(_.canInferTypes)

  final override
  def resolveTypes: this.type = {
    if (isPassive) {
      feltType = FeltType.valScal(BrioStringKey)
      this
    }
    else {
      _reference match {
        case None => this
        case Some(r) =>
          r.resolveTypes
          feltType = r.feltType
          this
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltPathExpr = new FeltPathExpr {
    sync(FeltPathExpr.this)
    final override val components: Array[String] = FeltPathExpr.this.components
    final override val key: Option[FeltExpression] = FeltPathExpr.this.key match {
      case None => None
      case Some(e) => Some(e.reduceStatics.resolveTypes)
    }
    final override val location: FeltLocation = FeltPathExpr.this.location
    final override val isPassive: Boolean = FeltPathExpr.this.isPassive
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    _reference.getOrElse(
      throw FeltException(location, s"no reference installed for path '$fullPathNoQuotes'")
    ).generateReferenceRead
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = s"$S$fullPath${
    key match {
      case None => ""
      case Some(k) =>
        val keyExpression = k.reduceStatics.resolveTypes
        s"[${keyExpression.normalizedSource}]"
    }
  }".trim

}
