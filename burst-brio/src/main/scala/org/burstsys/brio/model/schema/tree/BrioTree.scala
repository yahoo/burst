/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.model.schema.tree

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.model.schema.types.{BrioRelation, BrioRelationForm}
import org.burstsys.brio.types.BrioPath.{BrioPathKey, BrioPathKeyNotFound, BrioPathName}
import org.burstsys.vitals.errors.VitalsException
import com.koloboke.collect.map.hash.{HashObjObjMaps, _}

import java.util.concurrent.atomic.AtomicInteger

/**
 * Semantics for Schema Tree/Nodes/Paths associated with the associated scan object tree structure.
 * This is meant to have runtime/scan-time performance, low GC churn, and be thread safe
 * TODO are koloboke maps thread safe if they are not being modified? We make the assumption they are.
 */
trait BrioTree extends Any {

  /**
   * the root relation for this tree
   *
   * @return
   */
  def rootRelation: BrioRelation

  /**
   * the root node for this tree
   *
   * @return
   */
  def rootNode: BrioNode

  /**
   * A map of parent paths for each path (used for fast lookups at runtime)
   *
   * @return
   */
  def parentPathKeys: Array[BrioPathKey]

  /**
   * the count of unique ''paths'' through this tree
   * this count INCLUDES the none existent '0' path
   *
   * @return
   */
  def pathCount: Int

  /**
   * is a given path key the root of the tree
   *
   * @param key
   * @return
   */
  def pathKeyIsRoot(key: BrioPathKey): Boolean

  /**
   * return a path key given a path name
   *
   * @param path
   * @return path key or [[BrioPathKeyNotFound]]
   */
  def keyForPath(path: BrioPathName): BrioPathKey

  /**
   * Return the tree node for a given path key
   *
   * @param key
   * @return node or null if not found
   */
  def nodeForPathKey(key: BrioPathKey): BrioNode

  /**
   *
   * @param forms
   * @return
   */
  def allNodesForForms(forms: BrioRelationForm*): Array[BrioNode]

  /**
   * return all nodes (paths) in the schema object tree
   * @return
   */
  def allNodes:Array[_ <: BrioNode]

  /**
   * Return the tree node for a given path name
   *
   * @param path
   * @return node or null if absent
   */
  def nodeForPathName(path: BrioPathName): BrioNode

  def nodeExistsForPathName(path: BrioPathName): Boolean

}

trait BrioTreeContext extends AnyRef with BrioTree {

  self: BrioSchema =>

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final private[model]
  val pathIndex: AtomicInteger = new AtomicInteger(1)

  final private[model]
  var _parentPathKeys: Array[BrioPathKey] = _

  final private[model]
  val parentPathKeyToKeyMap: HashIntIntMap = HashIntIntMaps.newMutableMap()

  final private[model]
  val pathNameToKeyMap: HashObjIntMap[BrioPathName] = HashObjIntMaps.newMutableMap()

  @inline final override
  def nodeExistsForPathName(path: BrioPathName): Boolean = pathNameToKeyMap.containsKey(path)

  @inline final override
  def allNodesForForms(forms: BrioRelationForm*): Array[BrioNode] = {
    (for (i <- 1 to pathCount) yield {
      val node = _nodeForPathKeyMap.get(i)
      if (node == null) throw VitalsException(s"node for path $i not found")
      if (forms.contains(node.relation.relationForm)) node else null
    }).filter(_ != null).toArray
  }

  @inline final override
  lazy val allNodes:Array[BrioNode] = (for (i <- 1 to pathCount) yield {
    _nodeForPathKeyMap.get(i)
  }).toArray


  private[this]
  lazy val _nodeForPathKeyMap: HashIntObjMap[BrioNode] = {
    val result = HashIntObjMaps.newMutableMap[BrioNode]()

    def recurse(node: BrioNode): Unit = {
      node.children foreach {
        child =>
          result.put(child.pathKey, child)
          recurse(child)
      }
    }

    result.put(rootNode.pathKey, rootNode)
    recurse(rootNode)
    result
  }

  private[this]
  lazy val _nodeForPathNameMap: HashObjObjMap[BrioPathName, BrioNode] = {
    val result = HashObjObjMaps.newMutableMap[BrioPathName, BrioNode]()

    def recurse(node: BrioNode): Unit = {
      node.children foreach {
        child =>
          result.put(child.pathName, child)
          recurse(child)
      }
    }

    result.put(rootNode.pathName, rootNode)
    recurse(rootNode)
    result
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def parentPathKeys: Array[BrioPathKey] = _parentPathKeys

  final override
  def pathKeyIsRoot(key: BrioPathKey): Boolean = {
    val index = key - 1
    if (index < 0)
      true
    if (index >= parentPathKeys.length)
      throw VitalsException(s"key=$key, outside of paths in schema '$name'")
    parentPathKeys(index) == BrioPathKeyNotFound
  }

  final override
  def pathCount: Int =
    pathNameToKeyMap.size()

  final lazy override
  val rootNode: BrioNode = BrioNode(this, rootRelation)

  final override
  def nodeForPathKey(key: BrioPathKey): BrioNode = {
    _nodeForPathKeyMap.get(key)
  }

  final override
  def keyForPath(path: BrioPathName): BrioPathKey = {
    pathNameToKeyMap.getOrDefault(path, BrioPathKeyNotFound)
  }

  final override
  def nodeForPathName(path: BrioPathName): BrioNode = {
    _nodeForPathNameMap.get(path)
  }

}

