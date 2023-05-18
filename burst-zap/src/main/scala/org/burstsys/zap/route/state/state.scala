/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.route

import org.burstsys.tesla.TeslaTypes._
import org.burstsys.vitals.stats._
import org.burstsys.vitals.logging.VitalsLogger


/**
 * Memory Layout of a Zap Route Off Heap Memory Structure
 */
package object state extends VitalsLogger {

  final val ZapRoutePathEndOfJournal: Int = -1

  final val ZapRouteNullJournalEntry: ZapRouteEntry = ZapRouteEntry()

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Route Structure
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final val poolIdFieldOffset: TeslaMemoryOffset = 0 // Int

  final val initialEntryFieldOffset: TeslaMemoryOffset = poolIdFieldOffset + SizeOfLong

  final val currentPathFieldOffset: TeslaMemoryOffset = initialEntryFieldOffset + SizeOfLong

  final val rewriteNeededFieldOffset: TeslaMemoryOffset = currentPathFieldOffset + SizeOfLong

  final val completePathsFieldOffset: TeslaMemoryOffset = rewriteNeededFieldOffset + SizeOfLong

  final val currentStepKeyFieldOffset: TeslaMemoryOffset = completePathsFieldOffset + SizeOfLong

  final val currentTimeFieldOffset: TeslaMemoryOffset = currentStepKeyFieldOffset + SizeOfLong

  final val currentPathStartTimeFieldOffset: TeslaMemoryOffset = currentTimeFieldOffset + SizeOfLong

  final val commitCursorFieldOffset: TeslaMemoryOffset = currentPathStartTimeFieldOffset + SizeOfLong

  final val dirtyCursorFieldOffset: TeslaMemoryOffset = commitCursorFieldOffset + SizeOfLong

  final val routeLimitedFieldOffset: TeslaMemoryOffset = dirtyCursorFieldOffset + SizeOfLong

  final val journalIteratorOffset: TeslaMemoryOffset = routeLimitedFieldOffset + SizeOfInteger

  final val journalStartOffset: TeslaMemoryOffset = journalIteratorOffset + SizeOfLong

  final val ZapRouteHeaderSize = journalStartOffset

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Journal Entry Structure
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final val journalPathOrdinalFieldOffset: TeslaMemoryOffset = 0

  final val journalStepOrdinalFieldOffset: TeslaMemoryOffset = journalPathOrdinalFieldOffset + SizeOfInteger

  final val journalStepKeyFieldOffset: TeslaMemoryOffset = journalStepOrdinalFieldOffset + SizeOfInteger

  final val journalStepTagFieldOffset: TeslaMemoryOffset = journalStepKeyFieldOffset + SizeOfInteger

  final val journalStepTimeFieldOffset: TeslaMemoryOffset = journalStepTagFieldOffset + SizeOfInteger

  final val journalIsRewrittenFieldOffset: TeslaMemoryOffset = journalStepTimeFieldOffset + SizeOfLong

  final val journalIsCompleteFieldOffset: TeslaMemoryOffset = journalIsRewrittenFieldOffset + SizeOfByte

  final val journalIsLastStepInPathFieldOffset: TeslaMemoryOffset = journalIsCompleteFieldOffset + SizeOfByte

  final val journalIsTacitStepFieldOffset: TeslaMemoryOffset = journalIsLastStepInPathFieldOffset + SizeOfByte

  final val ZapRouteJournalEntrySize: TeslaMemorySize = journalIsTacitStepFieldOffset + SizeOfByte

  final val UninitializedCursor: TeslaMemoryOffset = -1
}
