/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.tree.code

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.types.BrioPath.{BrioPathKey, BrioPathName}
import org.burstsys.felt.model.schema.FeltSchema
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.vitals.errors.VitalsException

/**
 * A cursor used to contain Felt genetic code generation context
 */
trait FeltCodeCursor {

  /**
   * the global metadata for code generation
   *
   * @return
   */
  def global: FeltGlobal;

  /**
   * current call scope for constructed lexical name spaces
   *
   * @return
   */
  def callScope: FeltCodeScope

  /**
   * active felt schema for code generation
   *
   * @return
   */
  def schema: FeltSchema

  /**
   * the current active node in a brio object traversal tree
   *
   * @return
   */
  def treeNode: BrioNode

  /**
   * current pretty print indent
   *
   * @return
   */
  def indent: Int

  /**
   * current active path name for brio object traversal tree
   *
   * @return
   */
  def pathName: BrioPathName

  /**
   * current active path key for brio object traversal tree
   *
   * @return
   */
  def pathKey: BrioPathKey

  /**
   * are we at the root of the brio object traversal tree
   *
   * @return
   */
  def isRoot: Boolean

  /**
   * current active parent path name for brio object traversal tree
   *
   * @return
   */
  def parentPathName: BrioPathName

  /**
   * current active parent path key for brio object traversal tree
   *
   * @return
   */
  def parentPathKey: BrioPathKey

  /**
   * add a quantum of indent moving pretty print context to right
   *
   * @param increment the indent quantums
   * @return
   */
  def indentRight(increment: Int): FeltCodeCursor

  /**
   * add a single indent moving pretty print context to right
   *
   * @return
   */
  def indentRight: FeltCodeCursor = indentRight(1)

  /**
   * set current indent to a quantum
   *
   * @param i
   * @return
   */
  def newIndent(i: Int): FeltCodeCursor

  /**
   * push name space on level deeper
   *
   * @return
   */
  def scopeDown: FeltCodeCursor

  /**
   * modify some aspect of this cursor and return a modified copy
   *
   * @param indent
   * @param callScope
   * @param treeNode
   * @return
   */
  def modify(
              indent: Int = -1,
              callScope: FeltCodeScope = null,
              treeNode: BrioNode = null
            ): FeltCodeCursor

  def toString: String

}

object FeltCodeCursor {

  def apply(schema: BrioSchema): FeltCodeCursor = {
    val global = FeltGlobal(brioSchema = schema)
    FeltCodeCursorContext(
      global,
      indent = 0,
      callScope = FeltCodeScope(),
      treeNode = global.feltSchema.rootNode
    )
  }

  def apply(global: FeltGlobal): FeltCodeCursor = {
    FeltCodeCursorContext(
      global: FeltGlobal,
      indent = 0,
      callScope = FeltCodeScope(),
      treeNode = global.feltSchema.rootNode
    )
  }

  def apply(
             global: FeltGlobal,
             indent: Int = 0,
             callScope: FeltCodeScope = FeltCodeScope(),
             treeNode: BrioNode = null,
             rangeId: Long = 0
           ): FeltCodeCursor =
    FeltCodeCursorContext(
      global: FeltGlobal,
      indent = indent,
      callScope = callScope,
      treeNode = treeNode
    )

}

private final case
class FeltCodeCursorContext(
                             global: FeltGlobal,
                             indent: Int,
                             callScope: FeltCodeScope,
                             treeNode: BrioNode
                           ) extends FeltCodeCursor {

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  lazy val _scopeInfo: String =
    if (callScope == null) "" else s" callScope='$callScope'"

  private[this]
  lazy val _parentScopeInfo: String =
    if (callScope.parentScope.isEmpty) "" else if (isRoot) " (ROOT)" else s" parentCallScope='${callScope.parentScope}'"

  private[this]
  lazy val _pathInfo: String =
    if (treeNode == null) "" else s" path=$pathKey:'$pathName'"

  private[this]
  lazy val _parentPathInfo: String =
    if (treeNode == null) "" else if (isRoot) " (ROOT)" else s" parent=$parentPathKey:'$parentPathName'"

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def toString: String = s"indent[$indent]${_pathInfo}${_parentPathInfo}${_scopeInfo}${_parentScopeInfo}"

  override def schema: FeltSchema = global.feltSchema

  override
  def isRoot: Boolean = schema.pathKeyIsRoot(pathKey)

  private val noSchemaTreeNode = s"no schema tree node specified!"
  private val noParentSchemaTreeNode = s"no parent schema tree node specified!"

  override
  def pathName: BrioPathName = {
    if (treeNode == null)
      throw VitalsException(noSchemaTreeNode)
    treeNode.pathName
  }

  override
  def pathKey: BrioPathKey = {
    if (treeNode == null)
      throw VitalsException(noSchemaTreeNode)
    treeNode.pathKey
  }

  override
  def parentPathName: BrioPathName = {
    if (treeNode == null)
      throw VitalsException(noSchemaTreeNode)
    if (treeNode.parent == null)
      throw VitalsException(noParentSchemaTreeNode)
    treeNode.parent.pathName
  }

  override
  def parentPathKey: BrioPathKey = {
    if (treeNode == null)
      throw VitalsException(noSchemaTreeNode)
    if (treeNode.parent == null)
      throw VitalsException(noParentSchemaTreeNode)
    treeNode.parent.pathKey
  }

  override
  def indentRight(increment: Int): FeltCodeCursor = {
    this.copy(indent = indent + increment)
  }

  override
  def scopeDown: FeltCodeCursor = {
    this.copy(callScope = FeltCodeScope(callScope))
  }

  override
  def modify(
              indent: Int = -1,
              callScope: FeltCodeScope = null,
              treeNode: BrioNode = null
            ): FeltCodeCursor = {
    this.copy(
      indent = if (indent == -1) this.indent else indent,
      treeNode = if (treeNode == null) this.treeNode else treeNode,
      callScope = if (callScope == null) this.callScope else callScope
    )
  }

  override def newIndent(i: BrioPathKey): FeltCodeCursor = this.modify(indent = i)

}


