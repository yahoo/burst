/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.generate.calculus

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.model.schema.types.BrioValueScalarRelation
import org.burstsys.brio.types.BrioPath.BrioPathKey
import org.burstsys.felt.model.brio.reference.FeltBrioRef
import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.schema.FeltSchema
import org.burstsys.vitals.bitmap.VitalsBitMapAnyVal
import org.burstsys.vitals.errors.VitalsException

import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable

/**
 * =Join/Merge Calculus=
 * This metadata object is used during code generation to set up (calculate) appropriate
 * zero GC '''joins''' and '''merges'''.
 * <p/>
 * ==metadata==
 * A hyper cube is a set of nested cubes that define a plan for how data scanned/collected from an brio defined
 * object tree (and other joinable collectors) is assembled into a final cube result set during the tree traversal.
 * There are two '''calculated''' operations '''merges''' and '''joins'''.
 * <p/>
 * ==merges==
 * A merge is an operation on two cubes that does conflation of aggregations from any two rows that share the
 * same identical dimensional ''key''. The ''merge'' respects the defined merge '''semantics''' (e.g. sum, max, min)
 * <p/>
 * ==joins==
 * A join is an operation on two cubes that creates a third cube that does that makes sure that rows exist for
 * all possible combinations of the dimensional keys from all possible row pairs. The aggregations are copied in
 * from the source row verbatim.
 * <p/>
 * ==masks==
 * Masks are bitmaps defined for the set of '''active''' dimensions and aggregations associated with merges and joins.
 * The nested cube model sets up what dimensions and aggregations are visible/impactful for each stage of the associated
 * merge or join. Only the dimensions/aggregations active at any particular join or merge point '''plus''' the ones
 * active at child levels are active at the level desired. This means parent cubes have all the active mask bits from
 * any cubes below them.
 */
trait FeltCubeCalculus extends Any {

  /**
   * the complete set of joins (for unit tests)
   *
   * @return
   */
  def joins: Array[Join]

  /**
   * the complete set of merges (for unit tests)
   *
   * @return
   */
  def merges: Array[Merge]

  /**
   * used to set up a join at code generation
   *
   * @param path
   * @return
   */
  def childJoinAt(path: BrioPathKey): Option[Join]

  /**
   * did the calculus show that we need a child join at a specific
   * path key?
   *
   * @param path
   * @return
   */
  def isChildJoinAt(path: BrioPathKey): Boolean

  /**
   * the child merge op at a given path key
   *
   * @param path
   * @return
   */
  def childMergeAt(path: BrioPathKey): Option[Merge]

  /**
   * did we calculate a given path is a child merge op?
   *
   * @param path
   * @return
   */
  def isChildMergeAt(path: BrioPathKey): Boolean

  /**
   * the member merge op at a given path key
   *
   * @param path
   * @return
   */
  def memberMergeAt(path: BrioPathKey): Option[Merge]

}

object FeltCubeCalculus {
  def apply(rootCubeDecl: FeltCubeDecl): FeltCubeCalculus =
    FeltCubeCalculusContext(rootCubeDecl: FeltCubeDecl)
}

/**
 * just the information Hydra needs during code generation. We generate dimension and aggregation masks
 * for each path where a join or merge is necessary
 *
 * @param cubeDecl
 * @param schema
 */
private final case
class FeltCubeCalculusContext(cubeDecl: FeltCubeDecl) extends FeltCubeCalculus {

  override def toString: String = _cubeMap.values.toList.sortBy(_.pathKey).mkString("{\n\t", ",\n\t", "\n}")

  val schema: FeltSchema = cubeDecl.global.feltSchema

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _childJoinMap = new mutable.HashMap[BrioPathKey, Join]

  private[this]
  val _childMergeMap = new mutable.HashMap[BrioPathKey, Merge]

  private[this]
  val _memberMergeMap = new mutable.HashMap[BrioPathKey, Merge]

  private[this]
  val _cubeMap: mutable.HashMap[BrioPathKey, Cube] = new mutable.HashMap[BrioPathKey, Cube]

  /**
   * dimensions and aggregations are index'ed in hypercube tree search order
   */
  private[this]
  val _dimensionIndex = new AtomicInteger

  private[this]
  val _aggregationIndex = new AtomicInteger

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def joins: Array[Join] = _childJoinMap.values.toArray

  override
  def merges: Array[Merge] = _childMergeMap.values.toArray

  override
  def childJoinAt(path: BrioPathKey): Option[Join] = _childJoinMap.get(path)

  override
  def childMergeAt(path: BrioPathKey): Option[Merge] = _childMergeMap.get(path)

  override
  def isChildJoinAt(path: BrioPathKey): Boolean = _childJoinMap.contains(path)

  override
  def isChildMergeAt(path: BrioPathKey): Boolean = _childMergeMap.contains(path)

  override
  def memberMergeAt(path: BrioPathKey): Option[Merge] = _memberMergeMap.get(path)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Internals
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  initialize()

  private[this]
  def initialize(): Unit = {
    // organize all cubes by path key
    recurseCubes(cubeDecl)

    // must have cube defined at root
    val rootCube = _cubeMap.getOrElse(schema.rootNode.pathKey, throw VitalsException(s"no root cube found!"))

    recurseNodes(schema.rootNode, rootCube)
  }

  /**
   * algorithm to scan cube tree and build a database of bu
   *
   * @param cube to recurse into
   * @return Tuple2(dimensionMask, aggregationMask)
   */
  private[this]
  def recurseCubes(cube: FeltCubeDecl): (VitalsBitMapAnyVal, VitalsBitMapAnyVal) = {
    val ref = cube.refTarget.referenceGetOrThrow[FeltBrioRef]
    val pathName = ref.pathName
    val pathKey = ref.pathKey
    var dimensionMask = VitalsBitMapAnyVal()
    var aggregationMask = VitalsBitMapAnyVal()
    val aggs = if (cube.aggregations == null) Array.empty[Agg] else cube.aggregations.columns.map {
      d =>
        val aggIndex = _aggregationIndex.getAndIncrement
        aggregationMask = aggregationMask.setBit(aggIndex)
        Agg(d.refName.shortName, aggIndex)
    }
    val dims = if (cube.dimensions == null) Array.empty[Dim] else cube.dimensions.columns.map {
      d =>
        val dimIndex = _dimensionIndex.getAndIncrement
        dimensionMask = dimensionMask.setBit(dimIndex)
        Dim(d.refName.shortName, dimIndex)
    }
    cube.subCubes.foreach {
      c =>
        val (dm, am) = recurseCubes(c)
        dimensionMask = dimensionMask | dm
        aggregationMask = aggregationMask | am
    }
    _cubeMap += pathKey -> Cube(pathKey, pathName, dims, dimensionMask, aggs, aggregationMask)
    (dimensionMask, aggregationMask)
  }

  /**
   * find cube boundaries
   *
   * @param currentNode
   * @param currentCube
   * @return
   */
  private[this]
  def recurseNodes(currentNode: BrioNode, currentCube: Cube, parentCube: Option[Cube] = None): Unit = {
    // ignore value scalars
    if (currentNode.relation.relationForm == BrioValueScalarRelation) return

    val pathKey = currentNode.pathKey
    _memberMergeMap += pathKey -> Merge(currentCube)

    // handle root
    val pc = parentCube.getOrElse(currentCube)

    if (currentCube != pc) {
      _childJoinMap += pathKey -> Join(currentCube, pc)
    } else {
      _childMergeMap += pathKey -> Merge(currentCube)
    }

    currentNode.children foreach {
      childNode =>
        _cubeMap.get(childNode.pathKey) match {
          case None =>
            recurseNodes(childNode, currentCube, Some(currentCube))
          case Some(childCube) =>
            recurseNodes(childNode, childCube, Some(currentCube))
        }
    }
  }

}
