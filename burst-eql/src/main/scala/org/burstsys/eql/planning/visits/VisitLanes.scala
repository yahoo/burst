/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.planning.visits

import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.planning.lanes
import org.burstsys.eql.planning.lanes.{LaneActions, _}
import org.burstsys.eql.generators._

import scala.collection.mutable

/**
  * This is all the lanes for a particular path visit in a Hydra query.   Lanes are organized by name with most
  * queries having a "RESULT" lane for calculating the final cube results for a particular blob scan.  However other
  * lanes can exist as the result of subexpressions, like aggregates, or calculations for routes and segments.
  */
class VisitLanes  {
  val laneMap: mutable.Map[lanes.LaneName, LaneActions] = mutable.Map[LaneName, LaneActions]()

  def this(v: VisitLanes) = {
    this()
    laneMap ++= v.laneMap
  }

  def apply(lane: LaneName): Option[LaneActions] = laneMap.get(lane)

  /**
    * Add a generator step to the visit at a given path
    * @param name lane name visit
    * @param generator generator step
    */
  def addGenerator(name: LaneName, generator: ActionSourceGenerator): VisitLanes = {
    getOrCreate(name) add generator
    this
  }

  /**
    * Add a dimension write step to the visit
    * @param name lane name add write
    * @return
    */
  def addDimensionWrite(name: LaneName): VisitLanes = {
    // look it up
    getOrCreate(name).addDimensionWrite()
    this
  }

  def getOrCreate(lane: LaneName): LaneActions = {
    laneMap.getOrElseUpdate(lane, {lane.createLane()})
  }
}

