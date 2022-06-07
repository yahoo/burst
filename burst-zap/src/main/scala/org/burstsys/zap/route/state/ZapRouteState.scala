/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.route.state


import org.burstsys.felt.model.collectors.route._
import org.burstsys.tesla.TeslaTypes.TeslaMemoryOffset
import org.burstsys.tesla.offheap
import org.burstsys.tesla.pool._
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.zap.route._

import scala.collection.mutable

/**
 * This Universal trait manages all off heap state associated with the [[org.burstsys.zap.route.ZapRouteContext]] value class.
 */
trait ZapRouteState extends Any with ZapRoute {

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Pool Id
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def poolId: TeslaPoolId = offheap.getInt(basePtr + poolIdFieldOffset)

  @inline final
  def poolId(w: TeslaPoolId): Unit = offheap.putInt(basePtr + poolIdFieldOffset, w)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // rewrite needed flag
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def rewriteNeeded: Boolean = offheap.getByte(basePtr + rewriteNeededFieldOffset) > 0

  @inline final
  def rewriteNeeded_=(s: Boolean): Unit = offheap.putByte(basePtr + rewriteNeededFieldOffset, if (s) 1 else 0)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // commitCursor
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def commitCursor: TeslaMemoryOffset = offheap.getInt(basePtr + commitCursorFieldOffset)

  @inline final
  def commitCursor(w: TeslaMemoryOffset): Unit = offheap.putInt(basePtr + commitCursorFieldOffset, w)

  @inline final
  def commitEntry: ZapRouteEntry = ZapRouteEntry(basePtr + journalStartOffset + commitCursor)

  @inline final
  def commitEntry(e: ZapRouteEntry): Unit = commitCursor((e.basePtr - basePtr - journalStartOffset).toInt)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // dirtyCursor
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def dirtyCursor: TeslaMemoryOffset = offheap.getInt(basePtr + dirtyCursorFieldOffset)

  @inline final
  def dirtyCursor(w: TeslaMemoryOffset): Unit = offheap.putInt(basePtr + dirtyCursorFieldOffset, w)

  @inline final
  def dirtyEntry: ZapRouteEntry = ZapRouteEntry(basePtr + journalStartOffset + dirtyCursor)

  @inline final
  def dirtyEntry(e: ZapRouteEntry): Unit = dirtyCursor((e.basePtr - basePtr - journalStartOffset).toInt)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // current path ordinal
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def currentPathOrdinal: FeltRoutePathOrdinal = offheap.getInt(basePtr + currentPathFieldOffset)

  @inline final
  def currentPathOrdinal_=(sk: FeltRoutePathOrdinal): Unit = offheap.putInt(basePtr + currentPathFieldOffset, sk)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // full path count
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def routeCompletePaths: Int = offheap.getInt(basePtr + completePathsFieldOffset)

  @inline final
  def routeCompletePaths_=(count: Int): Unit =
    offheap.putInt(basePtr + completePathsFieldOffset, count)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // step count
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def routeStepCount: Int = if (isEmpty) 0 else commitEntry.stepOrdinal + 1

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // current step key
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def currentStepKey: FeltRouteStepKey = offheap.getInt(basePtr + currentStepKeyFieldOffset)

  @inline final
  def currentStepKey_=(key: FeltRouteStepKey): Unit = offheap.putInt(basePtr + currentStepKeyFieldOffset, key)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // current time
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final
  def currentTime: FeltRouteStepTime = offheap.getLong(basePtr + currentTimeFieldOffset)

  @inline final
  def currentTime_=(st: FeltRouteStepTime): Unit = offheap.putLong(basePtr + currentTimeFieldOffset, st)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // route path time
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def routePathStartTime: FeltRouteStepTime = offheap.getLong(basePtr + currentPathStartTimeFieldOffset)

  @inline final
  def routePathStartTime_=(st: FeltRouteStepTime): Unit = offheap.putLong(basePtr + currentPathStartTimeFieldOffset, st)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Journal Entry Management
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def isEmpty: Boolean = commitCursor == UninitializedCursor && dirtyCursor == UninitializedCursor
  //def isEmpty: Boolean = initialEntry

  @inline final override
  def firstEntry: ZapRouteEntry = ZapRouteEntry(basePtr + journalStartOffset) // TODO deprecated as public route make private

  @inline final
  def createNewJournalEntry(isTacit: Boolean): Unit = {
    var stepOrdinal = 0
    if (dirtyCursor == UninitializedCursor) {
      dirtyCursor(0)
      currentPathOrdinal = 1
    } else {
      stepOrdinal = dirtyEntry.stepOrdinal
      if (!isTacit) {
        // non-tacit steps advance the ordinal
        stepOrdinal += 1
      }
      dirtyCursor(dirtyCursor + ZapRouteJournalEntrySize)
    }
    // make sure there is room for two more entries...
    checkPtr(basePtr + journalStartOffset + dirtyCursor + ZapRouteJournalEntrySize + ZapRouteJournalEntrySize)
    dirtyEntry.initialize()
    dirtyEntry.isTacit = isTacit
    dirtyEntry.stepOrdinal = stepOrdinal
    dirtyEntry.next.initialize()
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Journal Iteration
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def currentIteration: ZapRouteEntry = ZapRouteEntry(offheap.getLong(basePtr + journalIteratorOffset))

  @inline final private
  def currentIteration_=(e: ZapRouteEntry): Unit = offheap.putLong(basePtr + journalIteratorOffset, e.basePtr)

  @inline final private
  def currentIterationIsLast: Boolean = commitEntry.basePtr == currentIteration.basePtr

  @inline final override
  def firstOrNextIterable: Boolean = {
    do {
      if (commitCursor == UninitializedCursor) { // empty
        return false // no more entries
      } else if (currentIteration == ZapRouteNullJournalEntry) { // first entry?
        currentIteration = firstEntry
      } else if (!currentIterationIsLast) { // anything but first and last entry
        currentIteration = currentIteration.next
      } else { // last entry
        return false // no more entries
      }
      // check if the current iteration is tacit or not
      if (!currentIteration.isTacit)
        return true
    } while (true)
    false
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * done once at creation/allocation time
   *
   * @return
   */
  @inline final override
  def initialize(id: TeslaPoolId): ZapRoute = {
    poolId(id)
    reset
  }

  /**
   * this done each time the route is re-used.
   *
   * @return
   */
  @inline final override
  def reset: ZapRoute = {
    commitCursor(UninitializedCursor)
    dirtyCursor(UninitializedCursor)
    currentStepKey = FeltRouteNotInPathStep
    currentPathOrdinal = 0
    routeCompletePaths = 0
    currentTime = 0L
    commitEntry.initialize()
    currentIteration = ZapRouteNullJournalEntry
    rewriteNeeded = false
    routePathStartTime = -1
    this
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // LAST ENTRY STATE
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def routeLastPathIsComplete: Boolean = {
    if (isEmpty) throw VitalsException(s"can't call routeLastPathIsComplete on empty route!")
    commitEntry.isComplete
  }

  @inline final override
  def routeLastPathOrdinal: FeltRoutePathOrdinal = {
    if (isEmpty) throw VitalsException(s"can't call routeLastPathOrdinal on empty route!")
    commitEntry.pathOrdinal
  }

  @inline final override
  def routeLastStepKey: FeltRouteStepKey = {
    if (isEmpty) throw VitalsException(s"can't call routeLastStepKey on empty route!")
    commitEntry.stepKey
  }

  @inline final override
  def routeLastStepTag: FeltRouteStepTag = {
    if (isEmpty) throw VitalsException(s"can't call routeLastStepTag on empty route!")
    commitEntry.stepTag
  }

  @inline final override
  def routeLastStepTime: FeltRouteStepTime = {
    if (isEmpty) throw VitalsException(s"can't call routeLastStepTime on empty route!")
    commitEntry.stepTime
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Transaction Control
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline
  def scopeDirty: Boolean = dirtyCursor != commitCursor

  @inline final override
  def routeScopeStart(schema: FeltRouteBuilder): Unit = {
    dirtyEntry(commitEntry)
  }

  @inline final override
  def routeScopeCommit(schema: FeltRouteBuilder): Unit = {
    commitEntry(dirtyEntry)
    // log info s"routeScopeCommit${entryToString(dirtyEntry)}"
  }

  @inline final override
  def routeScopeAbort(schema: FeltRouteBuilder): Unit = {
    // log info s"routeScopeAbort-START ${entryToString(dirtyEntry)}"
    if (commitCursor == UninitializedCursor ) {
      // nothing has been comitted to the route
      reset
    } else if (dirtyCursor != commitCursor  && dirtyCursor != UninitializedCursor) {
      // an uncommitted entry is present so restore the committed state.
      currentPathOrdinal = commitEntry.pathOrdinal
      if (schema.exitSteps.contains(commitEntry.stepKey)) {
        currentStepKey = FeltRouteNotInPathStep
      } else {
        currentStepKey = commitEntry.stepKey
      }
      currentTime = commitEntry.stepTime
      commitEntry.next.initialize()
    }
    // discard the dirty entry by backing up the dirty cursor to the committed (may be uninizialized)
    dirtyEntry(commitEntry)
    // log info s"routeScopeAbort-EXIT ${entryToString(dirtyEntry)}"
  }

  @inline final override
  def routeScopeCurrentPath: FeltRoutePathOrdinal = dirtyEntry.pathOrdinal

  @inline final override
  def routeScopePreviousPath: FeltRoutePathOrdinal = commitEntry.pathOrdinal

  @inline final override
  def routeScopeCurrentStep: FeltRouteStepKey = dirtyEntry.stepKey

  @inline final override
  def routeScopePreviousStep: FeltRouteStepKey = commitEntry.stepKey

  @inline final override
  def routeScopePathChanged: Boolean = dirtyEntry.pathOrdinal != commitEntry.pathOrdinal

  @inline final override
  def routeScopeStepChanged: Boolean = dirtyEntry.stepKey != commitEntry.stepKey

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Unit Test Support
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def results: Array[Long] = {
    val r = new mutable.ArrayBuilder.ofLong
    var currentEntry = firstEntry
    while (currentEntry.validEntry) {
      r += currentEntry.pathOrdinal
      r += currentEntry.stepKey
      r += currentEntry.stepTag
      r += currentEntry.stepTime
      currentEntry = currentEntry.next
    }
    r.result()
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Debugging
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def toString: String =
    s"""ZapRoute(
       |  poolId=$poolId,
       |  currentPath=$currentPathOrdinal,
       |  currentStep=$currentStepKey,
       |  routeStepCount=$routeStepCount,
       |  routePathStartTime=$routePathStartTime,
       |  routeCompletePaths=$routeCompletePaths,
       |  commitCursor=$commitCursor,
       |  dirtyCursor=$dirtyCursor,
       |  currentTime=$currentTime
       |)""".stripMargin

  @inline
  def entryToString(entry: ZapRouteEntry): String = {
    val stepTacit = entry.isTacit
    val stepOrdinal = entry.stepOrdinal
    val pathOrdinal = entry.pathOrdinal
    val stepKey = entry.stepKey
    val stepTag = entry.stepTag
    val stepTime = entry.stepTime
    val stepPathComplete = entry.isComplete
    s"(basePtr=$basePtr)${if (rewriteNeeded) "R" else "NR"} pathOrdinal=$pathOrdinal, currentStepKey=$currentStepKey, step${if (stepTacit) "(T)" else ""}${if (stepPathComplete) "(C)" else ""}(ord=$stepOrdinal, Key=$stepKey, tag=$stepTag, time=$stepTime)"
  }


}
