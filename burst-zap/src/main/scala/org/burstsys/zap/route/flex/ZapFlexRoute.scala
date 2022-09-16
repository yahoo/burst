/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.route.flex

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.felt.model.collectors.route._
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.flex.{TeslaFlexCoupler, TeslaFlexProxy, TeslaFlexProxyContext, TeslaFlexSlotIndex}
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.zap.route
import org.burstsys.zap.route.{ZapRoute, ZapRouteBuilder, ZapRouteContext}

/**
 * The Tesla Flex Proxy for the route
 */
trait ZapFlexRoute extends Any with ZapRoute with TeslaFlexProxy[ZapRouteBuilder, ZapRoute]

private final case
class ZapFlexRouteAnyVal(index: TeslaFlexSlotIndex) extends AnyVal with ZapFlexRoute
  with TeslaFlexProxyContext[ZapRouteBuilder, ZapRoute, ZapFlexRoute] {

  // not sure this should be overriden
  override def blockPtr: TeslaMemoryPtr =
    internalCollector.blockPtr

  override def poolId: TeslaPoolId =
    internalCollector.poolId

  override def currentMemorySize: TeslaMemorySize =
    internalCollector.currentMemorySize

  /// Proxy

  override def coupler: TeslaFlexCoupler[ZapRouteBuilder, ZapRoute, ZapFlexRoute] =
    route.flex.coupler

  /**
   * This is for unit tests...
   */
  override def results: Array[Long] =
    internalCollector.results

  /**
   * initialize the route for first use
   */
  override def initialize(id: TeslaPoolId): ZapRoute =
    internalCollector.initialize(id)

  /**
   * initialize the route for reuse
   */
  override def reset(builder: ZapRouteBuilder): Unit =
    internalCollector.reset(builder)

  /**
   * return the number of rows in the cube
   */
  override def itemCount: Int =
    internalCollector.itemCount

  /**
   * set the number of rows in the cube
   */
  override def itemCount_=(count: Int): Unit = ???

  /**
   * true is a fixed row limit was exceeded
   */
  override def itemLimited: Boolean =
    internalCollector.itemLimited

  override def itemLimited_=(s: Boolean): Unit = ???

  /**
   */
  override def isEmpty: Boolean =
    internalCollector.isEmpty

  /**
   * assert a possible new step in a route. Returns true if a transition occurred.
   * record step and optionally a time
   */
  override def routeFsmStepAssert(builder: FeltRouteBuilder, stepKey: FeltRouteStepKey, stepTime: FeltRouteStepTime): Boolean =
    internalCollector.routeFsmStepAssert(builder, stepKey, stepTime)

  /**
   * assert a possible new step in a route. Returns true if a transition occurred.
   * record step key, step tag, and step time
   */
  override def routeFsmStepAssert(builder: FeltRouteBuilder, stepKey: FeltRouteStepKey, stepTag: FeltRouteStepTag, stepTime: FeltRouteStepTime): Boolean = {
    // this is where a new step can overflow a route
    var r = internalCollector.routeFsmStepAssert(builder, stepKey, stepTag, stepTime)
    if (internalCollector.itemLimited) {
      coupler.upsize(this.index, this.itemCount, internalCollector.builder)
      r = internalCollector.routeFsmStepAssert(builder, stepKey, stepTag, stepTime)
    }
    r
  }

  /**
   * record a time (or new time) into the current step if the given step is the last recorded step
   */
  override def routeFsmAssertTime(builder: FeltRouteBuilder, stepKey: FeltRouteStepKey, stepTime: FeltRouteStepTime): Boolean =
    internalCollector.routeFsmAssertTime(builder, stepKey, stepTime)

  /**
   * Return true if the FSM is in a specific step.
   *
   * @param step
   * @return
   */
  override def routeFsmInStep(step: FeltRouteStepKey): Boolean =
    internalCollector.routeFsmInStep(step)

  /**
   * Tell the FSM to ''end'' whatever path you are in. Reset the current step to be 'no current step'
   */
  override def routeFsmEndPath(): Unit =
    internalCollector.routeFsmEndPath()

  /**
   * Returns true if Fsm is currently in an active path
   */
  override def routeFsmInPath(): Boolean =
    internalCollector.routeFsmInPath()

  /**
   * Tell the FSM to ''complete'' (make the path a 'full' path)
   * if the current step if it has a [[CompleteStepTrait]] otherwise this
   * does the same thing as the `routeFsmEndPath()` call.
   * This creates a full path. This is called either manually or ''before'' a visit.
   */
  override def routeFsmBackFill(builder: FeltRouteBuilder): Unit =
    internalCollector.routeFsmBackFill(builder)

  /**
   * a count of full paths recorded (committed)
   *
   * @return
   */
  override def routeCompletePaths: Int =
    internalCollector.routeCompletePaths

  /**
   * total steps recorded in route
   *
   * @return
   */
  override def routeStepCount: Int =
    internalCollector.routeStepCount

  /**
   * was the last path recorded (committed) complete?
   * a runtime error if this route is empty
   *
   * @return
   */
  override def routeLastPathIsComplete: Boolean =
    internalCollector.routeLastPathIsComplete

  /**
   * what was the path ordinal of the last path recorded (committed)?
   * a runtime error if this route is empty
   *
   * @return
   */
  override def routeLastPathOrdinal: FeltRoutePathOrdinal =
    internalCollector.routeLastPathOrdinal

  /**
   * what was the step key of the last route entry recorded (committed)?
   * a runtime error if this route is empty
   *
   * @return
   */
  override def routeLastStepKey: FeltRouteStepKey =
    internalCollector.routeLastStepKey

  /**
   * what was the step tag of the last route entry recorded (committed)?
   * a runtime error if this route is empty
   *
   * @return
   */
  override def routeLastStepTag: FeltRouteStepTag =
    internalCollector.routeLastStepTag

  /**
   * what was the step time of the last route entry recorded (committed)?
   * a runtime error if this route is empty
   *
   * @return
   */
  override def routeLastStepTime: FeltRouteStepTime =
    internalCollector.routeLastStepTime

  /**
   * EPOCH time the path was started
   *
   * @return
   */
  override def routePathStartTime: FeltRouteStepTime =
    internalCollector.routePathStartTime

  override def write(kryo: Kryo, output: Output): Unit =
    internalCollector.write(kryo, output)

  override def read(kryo: Kryo, input: Input): Unit =
    internalCollector.read(kryo, input)

  /**
   * Start a route transaction, all operations have a well defined semantic within this scope, including the
   * ability to roll back all operations removing all route specific side effects.
   */
  override def routeScopeStart(schema: FeltRouteBuilder): Unit = {
    internalCollector.routeScopeStart(schema)
  }

  /**
   * commit a route transaction, making all route operations permanent
   */
  override def routeScopeCommit(schema: FeltRouteBuilder): Unit =
    internalCollector.routeScopeCommit(schema)

  /**
   * abort a route transaction, making all route updates in scope revert leaving no side effects.
   */
  override def routeScopeAbort(schema: FeltRouteBuilder): Unit =
    internalCollector.routeScopeAbort(schema)

  /**
   * What is the current path ordinal within this scope
   *
   * @return
   */
  override def routeScopeCurrentPath: FeltRoutePathOrdinal =
    internalCollector.routeScopeCurrentPath

  /**
   * what was the path ordinal when this scope was started?
   *
   * @return
   */
  override def routeScopePreviousPath: FeltRoutePathOrdinal =
    internalCollector.routeScopePreviousPath

  /**
   * Had the path ordinal changed within this scope?
   *
   * @return
   */
  override def routeScopePathChanged: Boolean =
    internalCollector.routeScopePathChanged

  /**
   * what is the current step key?
   *
   * @return
   */
  override def routeScopeCurrentStep: FeltRouteStepKey =
    internalCollector.routeScopeCurrentStep

  /**
   * what was the step key when this scope was started?
   *
   * @return
   */
  override def routeScopePreviousStep: FeltRouteStepKey =
    internalCollector.routeScopePreviousStep

  /**
   * Had the step key changed within this scope?
   *
   * @return
   */
  override def routeScopeStepChanged: Boolean =
    internalCollector.routeScopeStepChanged

  /**
   * copy over data from a presumably too small collector to this bigger upsized collector
   */
  override def importCollector(sourceCollector: ZapRoute, sourceItems: Int, builder: ZapRouteBuilder): Unit =
    throw VitalsException(s"import collector not allowed")

  override def initialize(pId: TeslaPoolId, builder: ZapRouteBuilder): Unit =
    internalCollector.initialize(pId, builder)

  override def defaultBuilder: ZapRouteBuilder =
    internalCollector.defaultBuilder

  override def builder: ZapRouteBuilder =
    internalCollector.builder

  /**
   * Restart the iteration at the beginning
   *
   * @return
   */
  override def startIteration: Unit =
    internalCollector.startIteration

  /**
   * The Route stores a single cursor iteration pointer to a journal entry
   * use #nextIteration or #firstOrNextIterable to initialize this
   */
  override def currentIteration: FeltRouteEntry =
    internalCollector.currentIteration

  /**
   * get the first or the next iteration. Returns false if the route is empty
   * or if there are no more entries available.
   */
  override def firstOrNextIterable: Boolean =
    internalCollector.firstOrNextIterable

  override def toString: String = {
    s"FLEX[$index] $internalCollector"
  }

  def printEntries: String = {
      internalCollector.printEntries
  }
}
