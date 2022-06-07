/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.model.schema.tree

import org.burstsys.brio.model.schema._
import org.burstsys.brio.model.schema.types._
import org.burstsys.brio.types.BrioPath.{BrioPathKey, BrioPathName}
import org.burstsys.brio.types.BrioTypes.BrioVersionKey
import org.burstsys.vitals.errors.VitalsException

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * A single object tree traversal '''node'''. These nodes form a schema defined object tree topology and are
 * used throughout Burst to understand and implement schema specific object tree traversals.
 */
trait BrioNode extends Any with Equals {

  /**
   * the associated brio schema
   *
   * @return
   */
  def brioSchema: BrioSchema

  /**
   * the path ''name'' as a string. These are mostly used at language level for human friendly path
   * definitions but are generally translated as quickly as possible into '''pathKey''' [[BrioPathKey]] equivalents.
   *
   * @return
   */
  def pathName: BrioPathName

  /**
   * the runtime friendly path as a schema unique number.
   *
   * @return
   */
  def pathKey: BrioPathKey

  /**
   * the associated brio relation
   *
   * @return
   */
  def relation: BrioRelation

  /**
   * is this node the root of the object tree?
   *
   * @return
   */
  def isRoot: Boolean

  /**
   * does this node have a parent and what is it. This is [[None]] for the root of the tree.
   *
   * @return parent node or null if it does not exist (the parent)
   */
  def parent: BrioNode

  /**
   * the container for this relation (self if this is a container)
   *
   * @return
   */
  def container: BrioNode

  /**
   * the child tree nodes
   *
   * @return
   */
  def children: Array[_ <:BrioNode]

  /**
   * given a specific schema version, what are the child tree nodes
   *
   * @return
   */
  def childrenForVersion(version: BrioVersionKey): Array[BrioNode]

  /**
   * what are the child tree nodes for a given set of [[BrioRelationForm]]
   *
   * @return
   */
  def childrenForForms(form: BrioRelationForm*): Array[BrioNode]

  /**
   * children without a single form
   *
   * @param form
   * @return
   */
  def childrenWithoutForm(form: BrioRelationForm): Array[BrioNode]

  /**
   * what are the child tree nodes for a given set of [[BrioRelationForm]] and a specific version
   *
   * @return
   */
  def childrenForVersionAndForms(version: BrioVersionKey, forms: BrioRelationForm*): Array[BrioNode]

  /**
   * what are the recursive searched child tree nodes for a given set of [[BrioRelationForm]]
   *
   * @param form
   * @return
   */
  def descendantsForForms(form: BrioRelationForm*): Array[BrioNode]

  /**
   * what are the recursive searched child tree nodes
   *
   * @return
   */
  def descendants: Array[BrioNode]

  /**
   * all the node between parent and root of tree
   * TODO THIS INCLUDES THE ROOT NODE AND SO SHOULD BE RENAMED - think through better
   *
   * @return
   */
  def ancestors: Array[BrioNode]

  /**
   * this node AND all its ancestors...
   * @return
   */
  def transitToRoot: Array[BrioNode]

  /**
   * return true if a given target relation node can be ''read'' (accessed) from the point in the
   * traversal represented by this node. This is used to test that you can read a part of an object tree
   * from another part of the object tree during a traversal.
   * The rules are:
   * <ol>
   * <li>If `targetNode` node is on the same '''axis''' and '''at''' or '''above''' the current node</li>
   * <li>If `targetNode` node can be accessed via a scalar reference '''tunnel''' anywhere along the
   * way up the '''axis''' (a tunnel is one or more navigations through a scalar-reference relation since
   * they can be accessed off axis)</li>
   * </ol>
   *
   * @param targetNode
   * @param forms the set of valid forms for the target, if none are specified then all are allowed
   * @return
   */
  def canReachRelation(targetNode: BrioNode, forms: BrioRelationForm*): Boolean

}

object BrioNode {

  def apply(schema: BrioSchema, relation: BrioRelation): BrioNode = {
    val schemaContext = schema.asInstanceOf[BrioSchemaContext]
    buildTree(
      schemaContext,
      BrioNodeContext(
        brioSchema = schema, relation,
        pathKey = schemaContext.pathNameToKeyMap.getInt(schema.rootRelationName),
        pathName = schema.rootRelationName,
        parent = null
      )
    )
  }

  private
  def buildTree(schema: BrioSchema, parentNode: BrioNodeContext): BrioNode = {
    parentNode ++ parentNode.relation.referenceStructure.specificStructureRelationNameMap.values.map {
      childRelation =>
        val pathName = s"${parentNode.pathName}.${childRelation.relationName}"
        val childNode = BrioNodeContext(
          brioSchema = schema,
          childRelation,
          pathKey = schema.keyForPath(pathName),
          pathName = pathName,
          parent = parentNode
        )
        childRelation.relationForm match {
          case BrioReferenceScalarRelation | BrioReferenceVectorRelation => buildTree(schema, childNode)
          case BrioValueScalarRelation | BrioValueVectorRelation | BrioValueMapRelation => childNode
          case _ => ???
        }
    }.toArray
    parentNode
  }


}

private final case
class BrioNodeContext(
                       brioSchema: BrioSchema,
                       relation: BrioRelation,
                       pathKey: BrioPathKey,
                       pathName: BrioPathName,
                       parent: BrioNode
                     ) extends BrioNode {

  override
  def toString: BrioPathName = {
    relation.relationForm match {
      case BrioReferenceScalarRelation =>
        s"ReferenceScalar(pathkey=$pathKey, ordinal=${relation.relationOrdinal}, path=$pathName, structure=${relation.referenceStructure.structureTypeName})"
      case BrioReferenceVectorRelation =>
        s"ReferenceVector(pathkey=$pathKey, ordinal=${relation.relationOrdinal}, path=$pathName, structure=${relation.referenceStructure.structureTypeName})"
      case BrioValueScalarRelation =>
        s"ValueScalar(pathkey=$pathKey, ordinal=${relation.relationOrdinal}, path=$pathName, structure=${relation.valueEncoding.typeName})"
      case BrioValueVectorRelation =>
        s"ValueVector(pathkey=$pathKey, ordinal=${relation.relationOrdinal}, path=$pathName, structure=Array[${relation.valueEncoding.typeName})"
      case BrioValueMapRelation =>
        s"ValueMap(pathkey=$pathKey, ordinal=${relation.relationOrdinal}, path=$pathName, structure=Map[${relation.keyEncoding.typeName}, ${relation.valueEncoding.typeName})"
      case _ => ???

    }
  }

  private[this]
  var _children: Array[BrioNode] = Array.empty

  override
  def isRoot: Boolean = parent == null

  override
  def children: Array[BrioNode] = _children

  def ++(children: Array[BrioNode]): BrioNode = {
    _children = children
    this
  }

  override
  def descendants: Array[BrioNode] = {
    val descendants = new ArrayBuffer[BrioNode]

    def recurse(node: BrioNode): BrioNode = {
      node.children foreach {
        child => descendants += recurse(child)
      }
      node
    }

    recurse(this)
    descendants.toArray
  }

  override
  def ancestors: Array[BrioNode] = {
    if (this.isRoot)
      return Array.empty
    val ancestors = new mutable.HashSet[BrioNode]
    var p: BrioNode = this
    do {
      ancestors += p
      p = p.parent
    } while (!p.isRoot)
    ancestors += p // add root
    ancestors.toArray
  }

  override
  def transitToRoot: Array[BrioNode] = {
    this +: ancestors
  }

  override
  def childrenForVersion(version: BrioVersionKey): Array[BrioNode] = _children.filter(_.relation.validVersionSet.contains(version))

  override
  def childrenForForms(forms: BrioRelationForm*): Array[BrioNode] = _children.filter(c => forms.contains(c.relation.relationForm))

  override
  def childrenWithoutForm(form: BrioRelationForm): Array[BrioNode] = _children.filter(_.relation.relationForm != form)

  override
  def childrenForVersionAndForms(version: BrioVersionKey, forms: BrioRelationForm*): Array[BrioNode] = _children.filter {
    c => forms.contains(c.relation.relationForm) && c.relation.validVersionSet.contains(version)
  }

  override
  def descendantsForForms(forms: BrioRelationForm*): Array[BrioNode] = {
    val descendants = new ArrayBuffer[BrioNode]

    def recurse(node: BrioNode): BrioNode = {
      node.childrenForForms(forms: _*) foreach {
        child => descendants += recurse(child)
      }
      node
    }

    recurse(this)
    descendants.toArray
  }

  override
  def canReachRelation(targetNode: BrioNode, forms: BrioRelationForm*): Boolean = {
    // validate we have the right types of nodes...
    if (forms.nonEmpty && !forms.contains(targetNode.relation.relationForm))
      throw VitalsException(s"provided node $targetNode is not of form(s) ${forms.mkString("{", ", ", "}")} $this")

    // if we are at the node that actually contains this field we are good to read
    val container = targetNode.container

    /**
     * check to see if we are looking at a field inside ourselves, or a field in one of
     * our ancestors up to the traversal (object-tree) root. At each stop, we look to see
     * if we can '''tunnel''' via scalar references downwards...
     */
    var currentNode: BrioNode = this
    var continue = true
    while (continue) {
      // if we found our container on the closure to the root
      if (currentNode == container)
        return true
      // if we found our container by tunneling through reference scalar relations
      if (canTunnelTo(currentNode, container))
        return true
      continue = !currentNode.isRoot
      currentNode = currentNode.parent
    }
    false
  }

  /**
   * see if we can follow  reference scalar relations to get to the desired container
   *
   * @param node
   * @param container
   * @return
   */
  private
  def canTunnelTo(node: BrioNode, container: BrioNode): Boolean = {
    // find all reference scalar relations and check for joy
    node.children.foreach {
      childNode =>
        if (childNode.relation.relationForm == BrioReferenceScalarRelation) {
          if (childNode == container)
            return true
          if (canTunnelTo(childNode, container))
            return true
        }
    }
    // epic fail
    false
  }

  override
  def container: BrioNode = {
    if (this.parent == null) throw VitalsException(s"$this has no parent")
    if (relation.relationForm.isReference) this else this.parent
  }

  override def hashCode(): BrioVersionKey = pathKey.hashCode()

  override def equals(obj: Any): Boolean = obj match {
    case that: BrioNodeContext => this.pathKey == that.pathKey
    case _ => ???
  }

  override def canEqual(obj: Any): Boolean = obj match {
    case that: BrioNodeContext => true
    case _ => false
  }

}
