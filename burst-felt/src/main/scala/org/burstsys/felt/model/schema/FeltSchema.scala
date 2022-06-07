/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.schema

import org.burstsys.brio.model.schema.encoding.BrioSchematic
import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types.{BrioRelation, BrioRelationForm, BrioStructure}
import org.burstsys.brio.model.schema.{BrioSchema, rootPathKey}
import org.burstsys.brio.types.BrioPath.{BrioPathKey, BrioPathName}
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.schema.decl.FeltSchemaExtension
import org.burstsys.vitals.errors.VitalsException

import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable

/**
 * this is a facade on top of the [[BrioSchema]] that is used to
 * provide a per analysis 'brio schema with extensions' so we
 * can provide dynamic visits and other future extensions.
 */
trait FeltSchema extends BrioSchema {

  def brioSchema: BrioSchema

}

object FeltSchema {
  def apply(brioSchema: BrioSchema, schemaExtensions: Array[FeltSchemaExtension]): FeltSchema =
    FeltSchemaContext(brioSchema, schemaExtensions)
}

private final case
class FeltSchemaContext(brioSchema: BrioSchema, schemaExtensions: Array[FeltSchemaExtension]) extends FeltSchema {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _nodeKeyMap = new mutable.HashMap[BrioPathKey, BrioNode]

  private[this]
  val _nodeNameMap = new mutable.HashMap[BrioPathName, BrioNode]

  private[this]
  val schemaPathKeyGenerator = new AtomicInteger(brioSchema.pathCount)

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  INIT
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  schemaExtensions.foreach(_.extendedKey = schemaPathKeyGenerator.incrementAndGet())

  for (i <- rootPathKey to brioSchema.pathCount) {
    val node = FeltSchemaNode(this, brioSchema.nodeForPathKey(i))
    _nodeKeyMap += node.pathKey -> node
    _nodeNameMap += node.pathName -> node
  }

  schemaExtensions foreach {
    ext =>
      val node = FeltSchemaNode(
        feltSchema = this, relation = FeltRelation(ext.relationName), pathKey = ext.extendedKey, pathName = ext.extendedPath,
        parentPathKey = ext.parentKey, parentPathName = ext.parentPath
      )
      _nodeKeyMap += node.pathKey -> node
      _nodeNameMap += node.pathName -> node
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // FELT SCHEMA FACADE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def rootNode: BrioNode = _nodeKeyMap(rootPathKey)

  override
  lazy val allNodes: Array[BrioNode] = _nodeKeyMap.values.toArray

  /**
   * static schema paths as well as extension paths
   *
   * @return
   */
  override
  def pathCount: Int = brioSchema.pathCount + schemaExtensions.length

  /**
   * static schema paths as well as extension paths
   *
   * @param path
   * @return
   */
  override
  def keyForPath(path: BrioPathName): BrioPathKey = _nodeNameMap(path).pathKey

  override
  def nodeForPathName(path: BrioPathName): BrioNode = _nodeNameMap(path)

  override
  def nodeForPathKey(pathKey: BrioPathKey): BrioNode = _nodeKeyMap(pathKey)

  override
  def nodeExistsForPathName(path: BrioPathName): Boolean = _nodeNameMap.contains(path)

  override
  def allNodesForForms(forms: BrioRelationForm*): Array[BrioNode] = {
    (for (i <- 1 to pathCount) yield {
      _nodeKeyMap.get(i) match {
        case None => throw VitalsException(s"node for path $i not found")
        case Some(node) =>
          if (forms.contains(node.relation.relationForm)) node else null

      }
    }).filter(_ != null).toArray
  }


  /////////////////////////////////////////////////////////////////////////////////////////////////////////
  // DELEGATION TO BRIO SCHEMA
  /////////////////////////////////////////////////////////////////////////////////////////////////////////

  override val name: BrioSchemaName = brioSchema.name

  override def aliasedTo(alias: BrioSchemaName): Boolean = brioSchema.aliasedTo(alias)

  override val versionCount: BrioPathKey = brioSchema.versionCount

  override val rootRelationName: BrioRelationName = brioSchema.rootRelationName

  override def pathKeyIsRoot(key: BrioPathKey): Boolean = nodeForPathKey(key).isRoot

  /////////////////////////////////////////////////////////////////////////////////////////////////////////
  // UNUSED IN FELT WORLD
  /////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def relationList(version: BrioVersionKey, structureType: BrioTypeKey): Array[BrioRelation] = ???

  override def rootStructureType: BrioTypeName = ???

  override def structureKey(structure: BrioTypeName): BrioTypeKey = ???

  override def structureName(structure: BrioTypeKey): BrioTypeName = ???

  override def structures: Array[BrioStructure] = ???

  override def schematic(path: BrioPathName, version: BrioVersionKey): BrioSchematic = ???

  override def schematic(structureType: BrioTypeKey, version: BrioVersionKey): BrioSchematic = ???

  override def rootRelation: BrioRelation = ???

  override def parentPathKeys: Array[BrioPathKey] = ???

}
