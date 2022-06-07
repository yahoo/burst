/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.reference.names

import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree._
import org.burstsys.vitals.strings.extractStringLiteral

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * =Name Spaces=
 * ==visibility rules:==
 * <ol>
 * <li>starting at namespace root, traverse downwards into all
 * child namespace to create a set of absolute 'long' names</li>
 * <li>eliminate all names that traverse into 'anonymous' expression blocks
 * not on the path from here to the root</li>
 * </ol>
 */
trait FeltNameSpace {

  /**
   * lookup a possible match for a dot separated name to a [[FeltNameSpace]]
   *
   * @param name
   * @return
   */
  def lookup(refName: FeltPathExpr): Option[FeltNameSpace]

  /**
   *
   * @param name
   * @return
   */
  def lookupAbsolute(name: String): Option[FeltNameSpace]

  /**
   * the bound named node
   *
   * @return
   */
  def node: FeltNamedNode

  /**
   * the name of the local name space binding
   *
   * @return
   */
  def shortName: String

  /**
   * dot separated full name
   *
   * @return
   */
  def absoluteName: String

  /**
   * dot separated full name without the root part
   *
   * @return
   */
  def absoluteNameSansRoot: String

  /**
   * is this the root name space?
   *
   * @return
   */
  def isRoot: Boolean

  /**
   * is this the global name space?
   *
   * @return
   */
  def isGlobal: Boolean

  /**
   * is this an anonymous name space?
   *
   * @return
   */
  def isAnonymous: Boolean

  /**
   * the root of the hierarchical name space tree
   *
   * @return
   */
  def rootNameSpace: FeltNameSpace

  /**
   * long name divided into string components
   *
   * @return
   */
  def nameComponents: Array[String]

  /**
   * parent name space of None if this is the root
   *
   * @return
   */
  def parent: Option[FeltNameSpace]

  /**
   *
   * @return
   */
  def children: Array[FeltNameSpace]

  /**
   *
   * @param childNameSpace
   */
  def +=(childNameSpace: FeltNameSpace): Unit

  /**
   *
   * @param childNode
   */
  def +=(childNode: FeltNode): Unit

}

object FeltNameSpace {

  /**
   * non root felt node
   *
   * @param node
   * @param parent
   * @return
   */
  def apply(node: FeltNamedNode, parent: FeltNameSpace): FeltNameSpace = FeltNameSpaceContext(node, Some(parent))

  /**
   * root name space
   *
   * @return
   */
  def apply(): FeltNameSpace = FeltNameSpaceContext(null, None)

}

private final case
class FeltNameSpaceContext(node: FeltNamedNode, parent: Option[FeltNameSpace]) extends FeltNameSpace {

  override def toString: String = s"$shortName [ $absoluteName ] (childNodes=${_childNodes.size}, childSpaces=${_childNameSpaces.size})"

  override val shortName: String = if (parent.isEmpty) ROOT_NAME else node.nsName

  override
  lazy val nameComponents: Array[String] = {
    var builder = new mutable.ArrayBuffer[String]
    var current: FeltNameSpace = this
    var continue = true
    while (continue) {
      builder += current.shortName
      if (current.isRoot) continue = false else current = current.parent.get
    }
    builder.reverse.toArray
  }

  override lazy val absoluteName: String = nameComponents.map(extractStringLiteral).mkString(".")

  override lazy val absoluteNameSansRoot: String = absoluteName.stripPrefix(ROOT_NAME + ".")

  override val isRoot: Boolean = parent.isEmpty

  override lazy val isGlobal: Boolean = parent.isDefined && parent.get.isRoot

  override lazy val isAnonymous: Boolean = this.shortName.startsWith(anonymousScopeMarker)

  override def hashCode(): Int = absoluteName.hashCode()

  override def equals(obj: Any): Boolean = obj match {
    case that: FeltNameSpaceContext => that.absoluteName == this.absoluteName
    case _ => false
  }

  override def canEqual(that: Any): Boolean = that.isInstanceOf[FeltNameSpaceContext]

  private[this]
  val _childNameSpaces = new mutable.HashMap[String, FeltNameSpace]

  private[this]
  val _childNodes = new ArrayBuffer[FeltNode]

  override
  val rootNameSpace: FeltNameSpace = {
    var current: FeltNameSpace = this
    while (!current.isRoot) current = current.parent.get
    current
  }

  override
  lazy val children: Array[FeltNameSpace] = _childNameSpaces.values.toArray

  override
  def +=(childNameSpace: FeltNameSpace): Unit =
    _childNameSpaces += childNameSpace.shortName -> childNameSpace

  override
  def +=(childNode: FeltNode): Unit = _childNodes += childNode

  ////////////////////////////////////////////////////////////////////////////////////////////////
  // LOOKUP
  ////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def lookupAbsolute(name: String): Option[FeltNameSpace] = {
    val components = name.split('.')
    searchNode(rootNameSpace, components)
  }

  override
  def lookup(refName: FeltPathExpr): Option[FeltNameSpace] = {
    var refNameComponents = refName.components

    // search each space UP from current location to find first visible DOWN matching  name
    var current: FeltNameSpace = refName.nameSpace
    var continue = true
    while (continue) {
      //      log info s"LOOKUP( current=${current.absoluteName} refNameComponents=${refNameComponents.mkString(".")} )"
      continue = !current.isRoot
      if (!continue)
        true
      searchNode(current, refNameComponents) match {
        case None =>
          if (continue) {
            current = current.parent.get
          } else
            true
        case Some(s) =>
          return Some(s)
      }
    }

    None // oh well
  }

  /**
   * examine a single space, searching DOWN tree if we find a match at the start of the name
   *
   * @param current
   * @param components
   * @return
   */
  private
  def searchNode(current: FeltNameSpace, components: Array[String]): Option[FeltNameSpace] = {
    if (debugNames)
      log info
        s"""|searchNode(
            | current=${current.shortName}
            | children=${current.children.map(_.shortName).mkString(",")}"
            | components=${components.mkString(",")}"
            |)""".stripMargin
    current.children.foreach {
      child =>
        if (!child.isAnonymous && extractStringLiteral(child.shortName) == components.head) {
          // now explore downwards
          searchDownTree(child, components.drop(1)) match {
            case None =>
            case Some(s) =>
              return Some(s)
          }
        }
    }
    None
  }

  /**
   * lookdown the namespace subtree and stop when you hit a 1) anonymous scope 2) name list empty (found)
   *
   * @param current
   * @param components
   * @return
   */
  private
  def searchDownTree(current: FeltNameSpace, components: Array[String]): Option[FeltNameSpace] = {
    if (debugNames)
      log info
        s"""|searchDownTree(
            | current=${current.shortName}
            | children=${current.children.map(_.shortName).mkString(",")}"
            | components=${components.mkString(",")}"
            |)""".stripMargin
    if (components.isEmpty)
      return Some(current)
    current.children foreach {
      child =>
        if (!child.isAnonymous && extractStringLiteral(components.head) == extractStringLiteral(child.shortName))
          return searchDownTree(child, components.drop(1))
    }
    None
  }

}
