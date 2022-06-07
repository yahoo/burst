/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.tree

import org.burstsys.felt.model.frame.FeltFrameDecl
import org.burstsys.felt.model.reference.names.FeltNameSpace
import org.burstsys.felt.model.tree.source.FeltSource
import org.burstsys.felt.model.types.FeltType
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.strings._

/**
 * A single node in the Felt semantic model semantic-tree
 */
trait FeltNode extends AnyRef with FeltSource with FeltTree {

  /**
   * every node has a 'name' that is used for troubleshooting
   *
   * @return
   */
  def nodeName: String

  /**
   * the location in the source translation unit. This data is carried from the parser and
   * is available for syntax positioned error messages
   *
   * @return
   */
  def location: FeltLocation = FeltLocation()

  /**
   *
   * @return
   */
  final
  implicit def global: FeltGlobal = _global.getOrElse(throw VitalsException(s"no global binding for node $this"))

  def sync(that: FeltNode): Unit = {
    this.global = that.global
    this.nameSpace = that.nameSpace
    _frame = that.frame
  }

  /**
   * set the FeltGlobal for this node
   *
   * @param s
   */
  final
  def global_=(s: FeltGlobal): Unit = _global = Some(s)

  /**
   *
   * @return
   */
  final
  def frame: FeltFrameDecl = _frame

  /**
   * set the query for this node
   *
   * @param f
   */
  def frame_=(f: FeltFrameDecl): Unit = _frame = f

  /**
   *
   * @return
   */
  final
  def nameSpace: FeltNameSpace = _nameSpace

  final
  def nameSpace_=(ns: FeltNameSpace): Unit = _nameSpace = ns

  /**
   * The initial or resolved type of this node. Default is no type (Unit)
   *
   * @return
   */
  final
  def feltType: FeltType = _feltType

  final
  def feltType_=(t: FeltType): FeltType = {
    _feltType = t
    _feltType
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _global: Option[FeltGlobal] = None

  private[this]
  var _frame: FeltFrameDecl = _

  private[this]
  var _nameSpace: FeltNameSpace = _

  private[this]
  var _feltType: FeltType = FeltType.any

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * reduce/simplify this tree node and its descendants using statically available information
   * <p>'''NOTE:''' YOU MAY MODIFY THE TOPOLOGY OF THE TREE IN THIS CALL! (you will need to then redo some bindings as
   * new subtrees result)
   * <p>'''NOTE:''' THIS CALL MUST BE IDEMPOTENT
   *
   * @return
   */
  def reduceStatics: FeltNode

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * resolve the type of this node - default is the `feltType`. Note this is called multiple times - that
   * should be performant and semantically neutral
   * <p>'''NOTE:''' DO NOT MODIFY THE TOPOLOGY OF THE TREE IN THIS CALL!
   * <p>'''NOTE:''' THIS CALL MUST BE IDEMPOTENT (you can call as often as you want)
   *
   * @return
   * @deprecated this should use the apply rule
   */
  def resolveTypes: this.type

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNAL
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def toString: String = {
    assert(nodeName != null)
    assert(feltType != null)
    assert(normalizedSource != null)
    s"$nodeName:$feltType { ${normalizedSource.condensed} }"
  }

}
