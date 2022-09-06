/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.planning.lanes

import org.burstsys.eql.actions.ControlExpression
import org.burstsys.eql.generators._

import scala.collection.mutable

/**
  * Lane actions contain the collection of actions for a lane in a visit.  The execution of actions in a lane are
  * controlled by conditions evaluated at this visit as well as conditions evaluated beforehand in any ancestor
  * visits and conditions evaluated by any children visits.   The actions are only evaluated if:
  *   1) the parent allows it
  *   2) the children all allow it
  *   3) conditions at this level are true
  *
  *
  */
class LaneActions extends LaneActionsSourceGenerator {
  val control: LaneControl = LaneControl()
  protected val actionQueue: mutable.Queue[ActionSourceGenerator] = mutable.Queue()
  protected var hasDimensionWrite: Boolean = false

  def this(laneAction: LaneActions) = {
    this()
    laneAction.actions.foreach(actionQueue.enqueue)
  }

  def transform(op: ActionSourceGenerator => ActionSourceGenerator): this.type = {
    this.actionQueue.mapInPlace(op)
    this
  }

  def add(step: ActionSourceGenerator): this.type = {
    step match {
      case cg: ControlExpression =>
        control.addControlExpression(cg)
      case _: ActionSourceGenerator =>
        addAction(step)
      case _ =>
        throw new IllegalArgumentException(s"unexpected step $step")
    }
    this
  }

  def addDimensionWrite(): this.type = {hasDimensionWrite = true; this}

  def doesDimensionWrite:Boolean = hasDimensionWrite

  def addAction(step: ActionSourceGenerator): this.type = {actionQueue.enqueue(step); this}

  def prependAction(step: ActionSourceGenerator): this.type = {actionQueue += step; this}

  def actions: Iterator[ActionSourceGenerator] = actionQueue.iterator
}



