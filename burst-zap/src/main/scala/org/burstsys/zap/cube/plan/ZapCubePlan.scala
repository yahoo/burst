/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.plan

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types.BrioValueScalarRelation
import org.burstsys.brio.types.BrioPath.BrioPathKey
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.{FeltCubeAggDecl, FeltCubeAggSemRt}
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.{FeltCubeDimDecl, FeltCubeDimSemRt}
import org.burstsys.felt.model.collectors.cube.runtime.{FeltCubeOrdinalMap, FeltCubeTreeMask}
import org.burstsys.felt.model.collectors.cube.{FeltCubeId, FeltCubePlan, FeltCubeProvider}
import org.burstsys.zap.cube._

import java.util
import java.util.concurrent.atomic.AtomicInteger
import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

/**
 *
 */
trait ZapCubePlan extends FeltCubePlan {

  /**
   *
   * @return
   */
  def brioSchema: BrioSchema

  /**
   *
   * @param key
   * @param aggregation
   */
  def addCubeAggregation(key: ZapCubeAggregationKey, aggregation: FeltCubeAggDecl): Unit

  /**
   *
   * @return
   */
  def newCubeAggregationKey: ZapCubeAggregationKey

  /**
   *
   * @param dimensionKey
   * @param d
   */
  def addCubeDimension(dimensionKey: ZapCubeDimensionKey, d: FeltCubeDimDecl): Unit

  /**
   *
   * @return
   */
  def newCubeDimensionKey: ZapCubeDimensionKey

  /**
   *
   * @return
   */
  def pathKeyToCubeIdMap: mutable.HashMap[BrioPathKey, FeltCubeId]

  /**
   *
   * @return
   */
  def newCubeId: FeltCubeId

}

object ZapCubePlan {

  def apply(decl: FeltCubeDecl): ZapCubePlan = ZapCubePlanContext(decl: FeltCubeDecl)

}

/**
 * Root cube (true for exactly one root cube) plan context (temp info to help create a plan) true
 * at all places in the traversal
 *
 * @param decl
 */
private[plan] final case
class ZapCubePlanContext(decl: FeltCubeDecl) extends ZapCubePlan with ZapCubePlanGen {

//  assert(brioSchema != null)

  assert(decl != null)

  override def brioSchema: BrioSchema = decl.global.brioSchema

  val binding: FeltCubeProvider = decl.global.binding.collectors.cubes

  ////////////////////////////////////////////////////////
  // Private State
  ////////////////////////////////////////////////////////

  private[this]
  var _pathKeyToCubeIdMap = new mutable.HashMap[BrioPathKey, FeltCubeId]

  private[this]
  val _pathToParentCubeIdMap = new mutable.HashMap[BrioPathKey, FeltCubeId]

  /**
   *
   */
  private[this]
  val _cubeIdGenerator = new AtomicInteger(0)

  /**
   * A special Zap scala Value Class - one for each cube that has a bit map where a '1' means the dimension
   * is active for that cube, '0' if it is invisible. This is for run-time checking of
   * which dimensions are active for a given cube scope.
   */
  private[this]
  var _dimensionCubeViewMask: FeltCubeTreeMask = FeltCubeTreeMask()

  private[this]
  var _dimensionCubeJoinMask: FeltCubeTreeMask = FeltCubeTreeMask()

  /**
   * Same as above but for aggregations
   */
  private[this]
  var _aggregationCubeViewMask: FeltCubeTreeMask = FeltCubeTreeMask()

  private[this]
  var _aggregationCubeJoinMask: FeltCubeTreeMask = FeltCubeTreeMask()

  /**
   * We build a hierarchical tree for the cube - this is used to establish runtime view structures.
   */
  private[this]
  var _cubeNodeTree: ZapCubePlanNode = _

  /**
   *
   */
  private[this]
  val _dimensionIndex = new AtomicInteger(0)

  /**
   *
   */
  private[this]
  val _aggregationIndex = new AtomicInteger(0)

  /**
   *
   */
  private[this]
  val _dimensionNames = new util.HashMap[ZapCubeDimensionKey, String]

  /**
   *
   */
  private[this]
  val _aggregationNames = new util.HashMap[ZapCubeAggregationKey, String]

  /**
   * Super Cube Dimensions indexed by key
   */
  private[this]
  val _dimensionKeyMap = new util.HashMap[ZapCubeDimensionKey, FeltCubeDimDecl]

  /**
   * Super Cube Aggregations indexed by key
   */
  private[this]
  val _aggregationKeyMap = new util.HashMap[ZapCubeAggregationKey, FeltCubeAggDecl]

  /**
   * we store all the keys for a cube's child cubes (nested cubes)
   */
  private[this]
  var _cubeChildIds: ZapCubeIdArray = _

  /**
   * keep track of our cube nodes
   */
  private[this]
  val _childCubeNodeList = new ZapCubeNodeList

  /**
   * keep track of our cube nodes
   */
  private[this]
  var _planned = false

  ////////////////////////////////////////////////////////
  // Api
  ////////////////////////////////////////////////////////

  override
  lazy val initialize: ZapCubePlan = {
    constructCubeTopology.bindCubeSemantics
    this
  }


  /**
   * cube ids are unique within a given cube
   *
   * @return
   */
  override
  def newCubeId: FeltCubeId = _cubeIdGenerator.getAndIncrement()

  /**
   * add dimension to an overall hyper cube
   *
   * @param key
   * @param dimension
   */
  override
  def addCubeDimension(key: ZapCubeDimensionKey, dimension: FeltCubeDimDecl): Unit = {
    _dimensionNames.put(key, dimension.columnName)
    _dimensionKeyMap.put(key, dimension)
  }

  /**
   * add an aggregation to an overall hyper cube
   *
   * @param key
   * @param aggregation
   */
  override
  def addCubeAggregation(key: ZapCubeAggregationKey, aggregation: FeltCubeAggDecl): Unit = {
    _aggregationNames.put(key, aggregation.columnName)
    _aggregationKeyMap.put(key, aggregation)
  }

  override
  def pathKeyToCubeIdMap: mutable.HashMap[BrioPathKey, FeltCubeId] = _pathKeyToCubeIdMap

  /**
   * A new dimension key within this hyper cube
   *
   * @return
   */
  override
  def newCubeDimensionKey: ZapCubeDimensionKey = _dimensionIndex.getAndIncrement()

  /**
   * A new aggregation key within this hyper cube
   *
   * @return
   */
  override
  def newCubeAggregationKey: ZapCubeAggregationKey = _aggregationIndex.getAndIncrement()

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNAL
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  @tailrec
  private
  def parentCubeId(node: BrioNode): FeltCubeId = {
    val pathKey = node.parent.pathKey
    _pathKeyToCubeIdMap.get(pathKey) match {
      case None =>
        parentCubeId(node.parent)
      case Some(cId) =>
        cId
    }
  }


  private
  def bindCubeSemantics: this.type = {
    val fieldNames = {
      val list = new ArrayBuffer[BrioRelationName]
      list ++= _dimensionNames.asScala.toList.sortBy(_._1).map(_._2)
      list ++= _aggregationNames.asScala.toList.sortBy(_._1).map(_._2)
      list.toArray
    }

    /**
     * install just the runtime artifacts for dimensions
     */
    val dimensionCount = _dimensionKeyMap.size()
    val dimensionSemantics = new Array[FeltCubeDimSemRt](dimensionCount)
    val dimensionFieldTypes = new Array[BrioTypeKey](dimensionCount)
    val dimensionOrdinalMap = FeltCubeOrdinalMap()
    dimensionOrdinalMap.put("", ZapCubeInvalidDimensionKey) // a key of zero is an error
    _dimensionKeyMap forEach {
      (key, dimDecl) =>
        dimensionSemantics(key) = dimDecl.semantic.semanticRt
        dimensionFieldTypes(key) = dimDecl.semantic.bType
        dimensionOrdinalMap.put(dimDecl.columnName, key + 1)
    }

    /**
     * install just the runtime artifacts for aggregations
     */
    val aggregationCount = _aggregationKeyMap.size
    val aggregationSemantics = new Array[FeltCubeAggSemRt](aggregationCount)
    val aggregationFieldTypes = new Array[BrioTypeKey](aggregationCount)
    val aggregationOrdinalMap = FeltCubeOrdinalMap()
    aggregationOrdinalMap.put("", ZapCubeInvalidAggregationKey) // a key of zero is an error
    _aggregationKeyMap forEach {
      (key, aggDecl) =>
        aggregationSemantics(key) = aggDecl.semantic.semanticRt
        aggregationFieldTypes(key) = aggDecl.semantic.bType
        aggregationOrdinalMap.put(aggDecl.columnName, key + 1)
    }

    val schemaDecl = decl.global.feltSchema

    for (pathKey <- 1 to schemaDecl.pathCount) {
      val node = schemaDecl.nodeForPathKey(pathKey)
      if (!node.isRoot)
        node.relation.relationForm match {
          case BrioValueScalarRelation => // no need to keep track of cube transitions
          case _ =>
            _pathToParentCubeIdMap += pathKey -> parentCubeId(node)
        }
    }

    // add scheme extensions

    this.builder = ZapCubeBuilder(
      rowLimit = decl.rowLimit,
      fieldNames = fieldNames,
      dimensionCount = dimensionCount, dimensionSemantics = dimensionSemantics,
      dimensionFieldTypes = dimensionFieldTypes, dimensionOrdinalMap = dimensionOrdinalMap,
      dimensionCubeJoinMask = _dimensionCubeJoinMask,
      aggregationCount = aggregationCount, aggregationSemantics = aggregationSemantics,
      aggregationFieldTypes = aggregationFieldTypes,
      aggregationOrdinalMap = aggregationOrdinalMap, aggregationCubeJoinMask = _aggregationCubeJoinMask
    ).init(decl.frame.frameId, decl.frame.frameName, decl.global.binding)
    this
  }

  private
  def constructCubeTopology: this.type = {

    _cubeNodeTree = buildNodeTreeStructure(0, decl, null)

    val cubeIdCount = pathKeyToCubeIdMap.size

    /**
     * through the magic of implicit defs and Value classes - ladies and gentleman
     * behold the future of virtual objects!
     */
    _dimensionCubeViewMask = new Array[Long](cubeIdCount)
    _dimensionCubeJoinMask = new Array[Long](cubeIdCount)
    _aggregationCubeViewMask = new Array[Long](cubeIdCount)
    _aggregationCubeJoinMask = new Array[Long](cubeIdCount)

    _cubeChildIds = new Array[Array[FeltCubeId]](cubeIdCount)

    /**
     * now we create dimension and aggregation column view masks.
     */
    _childCubeNodeList foreach {
      cube =>
        _dimensionCubeViewMask.addCube(cube.cubeId, cube.dimensionView)
        _aggregationCubeViewMask.addCube(cube.cubeId, cube.aggregationView)
    }

    annotateNodeTreeStructure(_cubeNodeTree)

    /**
     * now we create dimension and aggregation column join masks
     */
    _childCubeNodeList foreach {
      cube =>
        _dimensionCubeJoinMask.addCube(cube.cubeId, cube.dimensionJoinKeys)
        cube.dimensionJoin = _dimensionCubeJoinMask(cube.cubeId)
        _aggregationCubeJoinMask.addCube(cube.cubeId, cube.aggregationJoinKeys)
        cube.aggregationJoin = _aggregationCubeJoinMask(cube.cubeId)
    }
    this
  }

  /**
   * recursive routine to bind node tree structure to appropriate ids, keys etc
   *
   * @param node
   * @return
   */
  private
  def annotateNodeTreeStructure(node: ZapCubePlanNode): (ArrayBuffer[ZapCubeDimensionKey], ArrayBuffer[ZapCubeAggregationKey]) = {
    val name = node.path
    val childCubeIds = new ArrayBuffer[FeltCubeId]
    val dimensionJoinKeys = new ArrayBuffer[ZapCubeDimensionKey]
    val aggregationJoinKeys = new ArrayBuffer[ZapCubeAggregationKey]
    dimensionJoinKeys ++= node.dimensionKeys
    aggregationJoinKeys ++= node.aggregationKeys
    node.childNodes foreach {
      child =>
        childCubeIds += child.cubeId
        val r = annotateNodeTreeStructure(child)
        dimensionJoinKeys ++= r._1
        aggregationJoinKeys ++= r._2
    }
    _cubeChildIds(node.cubeId) = childCubeIds.toArray
    node.dimensionJoinKeys ++= dimensionJoinKeys
    node.aggregationJoinKeys ++= aggregationJoinKeys
    (dimensionJoinKeys, aggregationJoinKeys)
  }

  /**
   * recursive routine to setup initial node tree structure
   *
   * @param level
   * @param cube
   * @param parentNode
   * @return
   */
  private
  def buildNodeTreeStructure(level: Int, cube: FeltCubeDecl, parentNode: ZapCubePlanNode): ZapCubePlanNode = {
    val newNode = ZapCubePlanNode(cube, this, parentNode).initialize
    this._childCubeNodeList += newNode
    cube.subCubes foreach (newNode.childNodes += buildNodeTreeStructure(level + 1, _, newNode))
    newNode
  }

}
