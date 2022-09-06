/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.planning.visits

import org.burstsys.eql.generators._
import org.burstsys.eql.paths._
import org.burstsys.eql.planning.lanes.{LaneActions, _}
import org.burstsys.motif.paths.Path
import org.burstsys.motif.paths.schemas.SchemaPathBase
import org.burstsys.motif.schema.model._

import scala.collection.mutable

/**
  * Code placement for Motif code is done using paths and visits.  A visit is a chunk of code that
  * needs to be executed at some path in the schema during an object scan.  For some query in a
  * Hydra analysis,  this object is a map of all the code organized into visits.
  *
  * Every visit has a set of *lanes* which are code performing calculations for the result cube, subexpression, routes,
  * segments, etc.   Lanes may or may not reside together in the same visit of the same Hydra query depending on the
  * hydra generation strategy done later.  At this state lanes allow the individual calculations to be kept separate
  * for analysis
  *
  * @param schema Brio Schema
  */
final class Visits(val schema: MotifSchema) extends Iterable[(VisitPath, VisitLanes)] {
  val visitMap: mutable.Map[VisitPath, VisitLanes] = mutable.Map[VisitPath, VisitLanes]()

  def apply(path: VisitPath): Option[VisitLanes] = visitMap.get(path)

  def apply(path: VisitPath, lane: LaneName): Option[LaneActions] = if (visitMap.contains(path)) visitMap(path)(lane) else None

  /**
    * Add a generator step to the visit at a given path
    * @param path path visit
    * @param generator generator step
    */
  def addGenerator(lane: LaneName)(path: Path, generator: ActionSourceGenerator): Visits = {
    getOrCreate(VisitPath(path)).addGenerator(lane, generator)
    this
  }

  def getSchemaRoot: VisitPath = {
    new SchemaVisitPath(SchemaPathBase.rootPath(schema))
  }

  def getDynamicMap: Option[VisitPathLookup] = {
    val lookups = visitMap.keys.flatMap{
      case dvp: DynamicVisitPath =>
        Some(dvp.getAttachmentPath -> dvp.getLocalRoot)
      case _ => None
    }
    val lookupMap = lookups.groupBy(_._1).view.mapValues(vpl => vpl.map(t => t._2).toList).mapValues(vpl => vpl.distinct)
    if (lookupMap.isEmpty)
      None
    else
      Some(lookupMap.toMap)
  }

  /**
    * Add a dimension write step to the visit
    * @param path path add write
    * @return
    */
  def addDimensionWrite(lane: LaneName)(path: VisitPath): Visits = {
    // look it up
    getOrCreate(path).addDimensionWrite(lane)
    this
  }

  /**
    * Get all the lanes defined in this visit set
    * @return
    */
  def getAllLaneNames: Array[LaneName] = {
    visitMap.values.flatMap(v => v.laneMap.keys).toSet.toArray
  }

  /**
    * Walk a specific lane in the visits in the visit map in schema DFS order.  Invoke an action
    * on each structure or collection before all the children are processed
    *
    * @param action the action
    * @tparam B type of the result from the actions
    * @return value of the last action
    */
  def preWalkVisits[B <: AnyRef](lane: LaneName)(action: (VisitPath, Option[LaneActions], Option[B])=> Option[B]): Option[B] = {
    val dynamicMap: Option[VisitPathLookup] = getDynamicMap
    getSchemaRoot.walkPaths(None, Some({ (path: VisitPath, parent: Option[B]) =>
              action(path, this (path, lane), parent)
            }), None, dynamicMap)
  }

  /**
   * Walk the visits in the visit map in schema DFS order.  Invoke an action
   * on each structure or collection before all the children are processed
   *
   * @param action the action
   * @tparam B type of the result from the actions
   * @return value of the last action
   */
  def preWalkVisits[B <: AnyRef](action: (VisitPath, Option[VisitLanes], Option[B])=> Option[B]): Option[B] = {
    val dynamicMap: Option[VisitPathLookup] = getDynamicMap
    getSchemaRoot.walkPaths(None, Some({ (path: VisitPath, parent: Option[B]) =>
      action(path, this(path), parent)
    }), None, dynamicMap)
  }

  /**
   * Walk a specific lane in the visits in the visit map in schema DFS order.  Invoke an action
    * on each structure or collection after all the children have been processed
    *
    * @param action the action
    * @tparam B type of the result from the actions
    * @return value of the last action
    */
  def postWalkVisits[B <: AnyRef](lane: LaneName)(action: (VisitPath, Option[LaneActions], List[B])=> Option[B]): Option[B] = {
    val dynamicMap: Option[VisitPathLookup] = getDynamicMap
    getSchemaRoot.walkPaths(None, None, Some({(path: VisitPath, children: List[B]) =>
              action(path, this(path, lane), children)}), dynamicMap)
  }

  /**
   * Walk a specific lane in the visits in the visit map in schema DFS order.  Invoke an action
   * on each structure or collection after all the children have been processed
   *
   * @param action the action
   * @tparam B type of the result from the actions
   * @return value of the last action
   */
  def postWalkVisits[B <: AnyRef](action: (VisitPath, Option[VisitLanes], List[B])=> Option[B]): Option[B] = {
    val dynamicMap: Option[VisitPathLookup] = getDynamicMap
    getSchemaRoot.walkPaths(None, None, Some({(path: VisitPath, children: List[B]) =>
      action(path, this(path), children)}), dynamicMap)
  }

  def getOrCreate(path: VisitPath): VisitLanes = {
    visitMap.getOrElseUpdate(path, new VisitLanes())
  }

  def getOrCreate(path: VisitPath, lane: LaneName): LaneActions = {
   getOrCreate(path).getOrCreate(lane)
  }

  override def iterator: Iterator[(VisitPath, VisitLanes)] = visitMap.iterator
}

