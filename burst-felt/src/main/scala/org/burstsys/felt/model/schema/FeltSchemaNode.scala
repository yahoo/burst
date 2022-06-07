/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.schema

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types.{BrioReferenceScalarRelation, BrioRelation, BrioRelationForm}
import org.burstsys.brio.types.BrioPath.{BrioMissingPathKey, BrioPathKey, BrioPathName}
import org.burstsys.brio.types.BrioTypes.BrioVersionKey
import org.burstsys.vitals.errors.VitalsException

import scala.collection.mutable

/**
 *
 */
trait FeltSchemaNode extends BrioNode

object FeltSchemaNode {

  val rootParentPath = "ROOT"

  def apply(brioSchema: FeltSchema, brioNode: BrioNode): FeltSchemaNode = {
    val parentPathKey = if (brioNode.isRoot) BrioMissingPathKey else brioNode.parent.pathKey
    val parentPathName = if (brioNode.isRoot) rootParentPath else brioNode.parent.pathName
    FeltSchemaNodeContext(brioSchema, brioNode.relation, brioNode.pathName, brioNode.pathKey,
      parentPathKey, parentPathName)
  }

  def apply(feltSchema: FeltSchema, relation: BrioRelation, pathKey: BrioPathKey, pathName: BrioPathName,
            parentPathKey: BrioPathKey, parentPathName: BrioPathName): FeltSchemaNode =
    FeltSchemaNodeContext(feltSchema, relation, pathName, pathKey, parentPathKey, parentPathName)

  def apply(feltSchema: FeltSchema, relation: BrioRelation, pathName: BrioPathName, pathKey: BrioPathKey): FeltSchemaNode =
    FeltSchemaNodeContext(brioSchema = feltSchema, relation = relation, pathName = pathName, pathKey = pathKey,
      parentPathKey = BrioMissingPathKey, parentPathName = rootParentPath)

}

private final case
class FeltSchemaNodeContext(brioSchema: FeltSchema, relation: BrioRelation, pathName: BrioPathName, pathKey: BrioPathKey,
                            parentPathKey: BrioPathKey, parentPathName: BrioPathName) extends FeltSchemaNode {

  override val toString: BrioPathName = s"FeltSchemaNode(pathName=$pathName, pathKey=$pathKey)"

  override val isRoot: Boolean = parentPathKey == BrioMissingPathKey

  override def children: Array[_ <: BrioNode] = brioSchema.allNodes.filter(n => !n.isRoot && n.parent.pathKey == pathKey)

  override def parent: BrioNode = brioSchema.allNodes.find(_.pathKey == parentPathKey).orNull

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

  override def childrenForVersion(version: BrioVersionKey): Array[BrioNode] = ???

  override def childrenForForms(form: BrioRelationForm*): Array[BrioNode] = ???

  override def childrenWithoutForm(form: BrioRelationForm): Array[BrioNode] = ???

  override def childrenForVersionAndForms(version: BrioVersionKey, forms: BrioRelationForm*): Array[BrioNode] = ???

  override def descendantsForForms(form: BrioRelationForm*): Array[BrioNode] = ???

  override def descendants: Array[BrioNode] = ???

}
