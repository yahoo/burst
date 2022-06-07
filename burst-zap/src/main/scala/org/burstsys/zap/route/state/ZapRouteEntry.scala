/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.route.state

import org.burstsys.felt.model.collectors.route._
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaNullMemoryPtr}
import org.burstsys.tesla.offheap

final case
class ZapRouteEntry(basePtr: TeslaMemoryPtr = TeslaNullMemoryPtr) extends AnyVal with FeltRouteEntry {

  @inline override
  def pathOrdinal: FeltRoutePathOrdinal = offheap.getInt(basePtr + journalPathOrdinalFieldOffset)

  @inline override
  def pathOrdinal_=(po: FeltRoutePathOrdinal): Unit = offheap.putInt(basePtr + journalPathOrdinalFieldOffset, po)

  @inline override
  def stepOrdinal: FeltRouteStepOrdinal = offheap.getInt(basePtr + journalStepOrdinalFieldOffset)

  @inline override
  def stepOrdinal_=(po: FeltRouteStepOrdinal): Unit = offheap.putInt(basePtr + journalStepOrdinalFieldOffset, po)

  @inline override
  def stepKey: FeltRouteStepKey = offheap.getInt(basePtr + journalStepKeyFieldOffset)

  @inline override
  def stepKey_=(key: FeltRouteStepKey): Unit = offheap.putInt(basePtr + journalStepKeyFieldOffset, key)

  @inline override
  def stepTag: FeltRouteStepTag = offheap.getInt(basePtr + journalStepTagFieldOffset)

  @inline override
  def stepTag_=(tag: FeltRouteStepTag): Unit = offheap.putInt(basePtr + journalStepTagFieldOffset, tag)

  @inline override
  def stepTime: FeltRouteStepTime = offheap.getLong(basePtr + journalStepTimeFieldOffset)

  @inline override
  def stepTime_=(time: FeltRouteStepTime): Unit = offheap.putLong(basePtr + journalStepTimeFieldOffset, time)

  @inline override
  def isComplete: Boolean = offheap.getByte(basePtr + journalIsCompleteFieldOffset) > 0

  @inline override
  def isComplete_=(state: Boolean): Unit = offheap.putByte(basePtr + journalIsCompleteFieldOffset, if (state) 1 else 0)

  @inline override
  def isRewritten: Boolean = offheap.getByte(basePtr + journalIsRewrittenFieldOffset) > 0

  @inline override
  def isRewritten_=(state: Boolean): Unit = offheap.putByte(basePtr + journalIsRewrittenFieldOffset, if (state) 1 else 0)

  @inline override
  def isLastStepInPath: Boolean = offheap.getByte(basePtr + journalIsLastStepInPathFieldOffset) > 0

  @inline override
  def isLastStepInPath_=(state: Boolean): Unit = offheap.putByte(basePtr + journalIsLastStepInPathFieldOffset, if (state) 1 else 0)

  @inline override
  def isTacit: Boolean = offheap.getByte(basePtr + journalIsTacitStepFieldOffset) > 0

  @inline override
  def isTacit_=(state: Boolean): Unit = offheap.putByte(basePtr + journalIsTacitStepFieldOffset, if (state) 1 else 0)

  @inline override
  def next: ZapRouteEntry = ZapRouteEntry(basePtr + ZapRouteJournalEntrySize)

  @inline override
  def previous: ZapRouteEntry = ZapRouteEntry(basePtr - ZapRouteJournalEntrySize)

  /**
   * @deprecated don't use for future stuff
   * @return
   */
  @inline override
  def validEntry: Boolean = pathOrdinal != ZapRoutePathEndOfJournal

  @inline override
  def initialize(): Unit = {
    pathOrdinal = ZapRoutePathEndOfJournal
    stepOrdinal = FeltRouteNotInPathOrdinal
    stepKey = FeltRouteNotInPathStep
    stepTag = FeltRouteInvalidTag
    stepTime = FeltRouteNoTime
    isComplete = false
    isRewritten = false
    isTacit = false
    isLastStepInPath = false
  }

  override
  def toString: String =
    s"""|ZapRouteEntry(
        |   pathOrdinal=$pathOrdinal,
        |   stepOrdinal=$stepOrdinal,
        |   stepKey=$stepKey,
        |   stepTag=$stepTag,
        |   stepTime=$stepTime,
        |   isTacit=$isTacit
        |   isComplete=$isComplete
        |   isRewritten=$isRewritten
        |)""".stripMargin

}

