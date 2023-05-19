/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.scatter.machine

import java.util.concurrent.TimeUnit

import org.burstsys.tesla.scatter.TeslaScatterContext
import org.burstsys.tesla.scatter.slot.TeslaScatterSlotContext
import org.burstsys.vitals.background.VitalsBackgroundFunctions.BackgroundFunction
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import scala.language.postfixOps

/**
 * manage scatter timeout and slot tardiness, clean up zombies we've given up on
 */
trait TeslaScatterTender extends AnyRef {

  self: TeslaScatterContext =>

  private
  lazy val tendFunction: BackgroundFunction = () => try {
    lockScatter("tender")
    try {
      if (scatterState.isRunning) {
        if (this.timeoutExceeded)
          scatterTimeout(s"Scatter duration exceeded timeout $timeout")
        else
          checkForTardySlots()
      }

      cleanupOverdueZombies()
    } finally unlockScatter("tender")
  } catch safely {
    case t: Throwable =>
      log error(burstStdMsg(s"TESLA_SCATTER_TEND_FAIL $t", t), t)
  }

  // add our tend function to the background tender thread
  backgroundTender += tendFunction

  private def checkForTardySlots(): Unit = {
    _activeSlots.values foreach {
      // this cast is here to get access to the lock
      case slot: TeslaScatterSlotContext =>
        slot.lockSlot("check tardy")
        try {
          if (slot.isTardy)
            slot.slotTardy()
        } finally slot.unlockSlot("check tardy")
    }
  }

  private def cleanupOverdueZombies(): Unit = {
    val now = System.nanoTime
    _zombieSlots.filterInPlace((_, slot) => {
      val request = slot.request
      val elapsed = now - slot.lastUpdateNanos
      var stillWaiting = false
      if (request.tardyAfter.isFinite) {
        stillWaiting = elapsed < request.tardyAfter.toNanos * 10
      } else if (elapsed < TimeUnit.MINUTES.toNanos(5)) {
        stillWaiting = true
      }
      if (!stillWaiting) {
        makeSlotIdle(slot)
      }
      stillWaiting
    })
  }
}
