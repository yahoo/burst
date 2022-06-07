/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.scatter.slot

import org.burstsys.tesla.scatter.TeslaScatterContext
import org.burstsys.vitals.errors.VitalsException

import scala.collection.mutable.ArrayBuffer

/**
 * manage the changing states of a ''scatter slot''
 */
trait TeslaSlotMachine extends Any {

  self: TeslaScatterContext =>

  final def slotBegin(slot: TeslaScatterSlot, update: TeslaScatterSlotBegin): Unit = {
    lockScatter("slot begin")
    try {
      log debug s"TeslaScatter.slotBegin $this, $slot BEGIN"
      pushUpdate(update)
    } finally unlockScatter("slot begin")
  }

  final def slotProgress(slot: TeslaScatterSlot, update: TeslaScatterSlotProgress): Unit = {
    lockScatter("slot progress")
    try {
      log debug s"TeslaScatter.slotProgress $this, $slot PROGRESS"
      pushUpdate(update)
    } finally unlockScatter("slot progress")
  }

  final def slotCancelled(slot: TeslaScatterSlot, update: TeslaScatterSlotCancel): Unit = {
    lockScatter("slot cancel")
    try {
      log debug s"TeslaScatter.slotCancelled $this, $slot CANCEL"
      pushUpdate(update)
      markSlotZombie(slot)
    } finally unlockScatter("slot cancel")
  }

  final def slotFail(slot: TeslaScatterSlot, update: TeslaScatterSlotFail): Unit = {
    lockScatter("slot fail")
    try {
      if (cleanupZombieSlot(slot, update) || !isActive(slot)) {
        // prevent zombie slots and completed slots from reporting
        return
      }

      log error s"TeslaScatter.slotFail $this, $slot EXCEPTION"

      if (_updates.remainingCapacity < _activeSlots.size) {
        // if we might not be able to report this failure we go ahead and print it out right now so
        // somebody has a chance to see it before everything goes pear shaped
        log error(s"TeslaScatter.slotFail $this, $slot", update.throwable)
      }

      pushUpdate(update)
      moveSlotTo(slot, _failSlots)
      receivedPossibleTerminalUpdate()
    } finally unlockScatter("slot fail")
  }

  final def slotTardy(slot: TeslaScatterSlot, update: TeslaScatterSlotTardy): Unit = {
    lockScatter("slot tardy")
    try {
      if (isActive(slot)) {
        log warn s"TeslaScatter.slotTardy $this $slot TARDY"
        pushUpdate(update)
      }
    } finally unlockScatter("slot tardy")
  }

  final def slotRetry(slot: TeslaScatterSlot, update: TeslaScatterSlotRetry): Unit = {
    lockScatter("slot retry")
    try {
      log debug s"TeslaScatter.slotRetry $this $slot RETRY"
      pushUpdate(update)
      // this should remove the slot from the failed slots list since we're re-opening the request
    } finally unlockScatter("slot retry")

  }

  final def slotSuccess(slot: TeslaScatterSlot, update: TeslaScatterSlotSucceed): Unit = {
    lockScatter("slot success")
    try {
      if (cleanupZombieSlot(slot, update) || !isActive(slot)) {
        // prevent zombie slots and completed slots from reporting
        return
      }
      log debug s"TeslaScatter.slotSuccess $this, $slot SUCCESS"

      pushUpdate(update)
      moveSlotTo(slot, _successSlots)
      receivedPossibleTerminalUpdate()
    } finally unlockScatter("slot success")
  }

  // Helper methods - these should all be called within a lock/unlock block above

  private def moveSlotTo(slot: TeslaScatterSlot, collection: ArrayBuffer[TeslaScatterSlot]): Unit = {
    _activeSlots -= slot.slotId
    collection += slot
  }

  private def cleanupZombieSlot(slot: TeslaScatterSlot, update: TeslaScatterSlotUpdate): Boolean = {
    val isZombie = _zombieSlots.contains(slot.slotId)
    if (isZombie) {
      log info s"$slot ZOMBIE $update"
      _zombieSlots -= slot.slotId
      makeSlotIdle(slot)
    }
    isZombie
  }

  private def receivedPossibleTerminalUpdate(): Unit = {
    if (_activeSlots.isEmpty) {
      if (_failSlots.isEmpty)
        scatterSucceed()
      else
        scatterFail(VitalsException(s"Scatter completed with ${_failSlots.size} failed slots"))
    }
  }

}
