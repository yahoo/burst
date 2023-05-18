/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.scatter

import org.burstsys.tesla.scatter.TeslaScatter.maxSlotsPerScatter
import org.burstsys.tesla.scatter.machine.{TeslaScatterLifecycle, TeslaScatterMachine, TeslaScatterTerminator}
import org.burstsys.tesla.scatter.slot.{TeslaScatterSlot, TeslaScatterSlotUpdate, TeslaSlotMachine}
import org.burstsys.vitals.errors.{VitalsException, safely}
import org.burstsys.vitals.uid._
import org.jctools.queues.MpmcArrayQueue

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.{ArrayBlockingQueue, ConcurrentHashMap, TimeUnit}
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

/**
 * ===overview===
 * Designed to encapsulate a set of N requests to N worker nodes to perform
 * some worker side operation and '''synchronously''' return a set of N related '''asynchronous''' results when all are either successful or
 * have had some sort of error or timeout.
 * Also required are a set of async ''update'' messages that inform the waiter as to progress of each of the N different
 * scattered requests.
 * Scatters should also be able to re vector individual requests to a different worker in the face of a lack of progress
 * or error condition.
 * ===design goals===
 * <ol>
 * <li>'''allocate''' as few objects as possible </li>
 * <li>'''re-use''' objects where already allocated</li>
 * <li>support incremental '''progress''' events during cycle</li>
 * <li>efficient early '''abort''' of scatter upon one or more request failure</li>
 * <li>scatter '''cancel'''</li>
 * <li>efficient '''retries''' of requests (slots) that fail or timeout</li>
 * <li>support wait/cleanup of '''zombie''' requests that are still coming in once the scatter has completed</li>
 * <li>support speculative execution i.e. add '''speculative''' alternate request to cover for existing tardy one</li>
 * </ol>
 */
trait TeslaScatter extends Any with TeslaScatterTerminator with TeslaScatterLifecycle {

  /**
   * immutable '''scatter''' id - assigned when slot is first created
   */
  def scatterId: TeslaScatterId

  /**
   * '''scatter''' GUID for associated top level operation - changes with each new allocation
   */
  def guid: VitalsUid

  def activeSlots: Int

  /**
   * current '''scatter''' state
   */
  def scatterState: TeslaScatterState

  /**
   * get a '''scatter slot''' for this scatter
   */
  def addRequestSlot(request: TeslaScatterRequest[_]): TeslaScatterSlot

  /**
   * '''scatter slots''' that have succeeded
   * this collection is not persistent across executions
   */
  def successes: Array[TeslaScatterSlot]

  /**
   * '''scatter slots''' that have failed
   * this collection is not persistent across executions
   */
  def failures: Array[TeslaScatterSlot]

  /**
   * '''scatter slots''' that have failed
   * this collection is persistent across executions
   */
  def zombies: Array[TeslaScatterSlot]

  /**
   * the duration after which a scatter should be stopped,
   * even if it is continuing to receive updates
   */
  def timeout: Duration

  def timeout_=(tardy: Duration): Unit

  /**
   * Blocking call waiting for the next update from any of the active requests.
   * used by the scatter initiator, this is presumably a ''single thread loop''
   * used to wait for any and all updates associated with any and all slots.
   * If this is ''not'' single threaded then you have to handle thorny race conditions.
   *
   * @return an update, or the timeout update if the provided timeout is reached waiting
   *         for an update to be received
   * @param timeout max valid wait for an update. default is forever
   */
  def nextUpdate(timeout: Duration = timeout): TeslaScatterUpdate

}


object TeslaScatter {

  private final val idGenerator = new AtomicInteger()

  final val maxSlotsPerScatter: Int = 1e3.toInt

  def apply(): TeslaScatter = TeslaScatterContext(idGenerator.incrementAndGet)

}

private[scatter] final case
class TeslaScatterContext(scatterId: TeslaScatterId) extends AnyRef
  with TeslaSlotMachine with TeslaScatter with TeslaScatterMachine {

  override def toString: String = s"TeslaScatter(scatterId=$scatterId, guid=$guid)"

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _guid: VitalsUid = _

  private[scatter]
  var _scatterState: TeslaScatterState = TeslaScatterStopState

  private[this]
  var _timeoutAfter: Duration = Duration.Inf

  private[scatter]
  var _startNanos: Long = -1

  private[scatter]
  val _begin = TeslaScatterBegin()

  private[scatter]
  val _success = TeslaScatterSucceed()

  private[scatter]
  val _cancelled = TeslaScatterCancel()

  private[scatter]
  val _timeout = TeslaScatterTimeout()

  private[scatter]
  val _fail = TeslaScatterFail()

  private[scatter]
  val _gate = new ReentrantLock(true)

  private[this]
  val _slotId = new AtomicInteger(0)

  /**
   * slots that are currently in use
   * this collection is not persistent across executions
   */
  private[scatter]
  val _activeSlots = new ConcurrentHashMap[Int, TeslaScatterSlot].asScala

  /**
   * slots that have succeeded
   * this collection is not persistent across executions
   */
  private[scatter]
  val _successSlots = new ArrayBuffer[TeslaScatterSlot]

  /**
   * slots that have failed
   * this collection is not persistent across executions
   */
  private[scatter]
  val _failSlots = new ArrayBuffer[TeslaScatterSlot]

  /**
   * slots that are in the zombie state
   * this collection is persistent across executions
   */
  private[scatter]
  val _zombieSlots = new ConcurrentHashMap[Int, TeslaScatterSlot].asScala

  def printSlotCounts: String =
    s"""activeSlots=${_activeSlots.size}, successSlots=${_successSlots.size}, failSlots=${_failSlots.size}, zombieSlots=${_zombieSlots.size} """

  /**
   * slots that are currently not being used
   * this collection is not persistent across executions
   */
  private[scatter]
  val _idleSlots = new MpmcArrayQueue[TeslaScatterSlot](maxSlotsPerScatter)
  for (_ <- 0 until maxSlotsPerScatter) _idleSlots add TeslaScatterSlot(this, _slotId.incrementAndGet)

  /**
   * the updates as they arrive
   */
  private[scatter]
  val _updates = new ArrayBlockingQueue[TeslaScatterUpdate](maxSlotsPerScatter * 10)

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def guid: VitalsUid = _guid

  override
  def scatterState: TeslaScatterState = _scatterState

  override
  def successes: Array[TeslaScatterSlot] = _successSlots.toArray

  override
  def failures: Array[TeslaScatterSlot] = _failSlots.toArray

  override
  def zombies: Array[TeslaScatterSlot] = _zombieSlots.values.toArray

  override
  def timeout: Duration = _timeoutAfter

  override
  def timeout_=(tardy: Duration): Unit = _timeoutAfter = tardy

  override def activeSlots: TeslaScatterId = _activeSlots.size

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // locking
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  def lockStatusMsg: String = {
    if (_gate.isLocked)
      if (_gate.isHeldByCurrentThread)
        s"here ${_gate.getHoldCount}"
      else {
        val lockStr = _gate.toString
        lockStr.substring(lockStr.indexOf("@"))
      }
    else s"Unlocked"
  }

  def lockScatter(from: String = ""): this.type = {
    if (log.isDebugEnabled) log debug s"$from lock ($lockStatusMsg)"

    _gate.lock()

    if (log.isDebugEnabled) log debug s"$from locked ($lockStatusMsg)"

    this
  }

  def unlockScatter(from: String = ""): this.type = {
    _gate.unlock()

    if (log.isDebugEnabled) log debug s"$from unlocked ($lockStatusMsg)"

    this
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // update loop
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  def timeoutExceeded: Boolean = timeout.isFinite && (System.nanoTime - _startNanos) > timeout.toNanos

  private[scatter]
  def pushUpdate(update: TeslaScatterUpdate): Unit = {
    lazy val tag = s"TeslaScatter.pushUpdate(guid=${_guid}, update=$update)"
    if (debugScatters)
      log debug s"pushing $tag"

    lockScatter("push update")
    try {
      // TODO if the scatter is in a failed state and we get a retry update we should:
      //  - transition the scatter back to TeslaScatterRunState
      //  - remove the fail update from the queue
      if (_scatterState == TeslaScatterRunState) {
        update match {
          case u: TeslaScatterSlotUpdate =>
            if (_activeSlots.contains(u.slot.slotId)) _updates add u
            else log info s"TESLA_SCATTER_UPDATE_INACTIVE slot=${u.slot} $tag"
          case u: TeslaScatterUpdate => _updates add u
        }
      } else log info s"TESLA_SCATTER_UPDATE_NOT_RUNNING $tag"
    } finally unlockScatter("push update")
  }

  override
  def nextUpdate(pollTimeout: Duration): TeslaScatterUpdate = {
    lazy val tag = s"TeslaScatter.nextUpdate(guid=${_guid}, pollTimeout=$pollTimeout)"
    val update = _updates.poll(pollTimeout.toNanos, TimeUnit.NANOSECONDS)
    lockScatter("next update")
    try {
      if (update != null) {
        if (debugScatters)
          log debug s"TESLA_SCATTER_UPDATE_POLL update=$update $tag"
        update
      } else {
        val message = s"TESLA_SCATTER_UPDATE_POLL_TIMEOUT update=$update $tag"
        log info message
        scatterTimeout(message)
        // _updates now contains _timeout, and possibly other messages generated by shutting down the scatter
        _updates.poll()
      }
    } finally unlockScatter("next update")
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // slot management
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def addRequestSlot(request: TeslaScatterRequest[_]): TeslaScatterSlot = {
    lockScatter("add request")
    try {
      val slot = try {
        _idleSlots.remove
      } catch safely {
        case _: NoSuchElementException => throw VitalsException(s"ran out of slots! $printSlotCounts")
      }
      _activeSlots += slot.slotId -> slot
      slot.open(request)
    } finally unlockScatter("add request")
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * internal open routine used by the framework
   */
  private[scatter]
  def open(guid: VitalsUid): TeslaScatter = {
    lazy val tag = s"TeslaScatter.open(guid=$guid)"
    lockScatter("open")
    try {
      _guid = guid
      if (debugScatters)
        log info s"current slots: zombie=${_zombieSlots.size} idle=${_idleSlots.size} $tag"
      if (_idleSlots.size + _zombieSlots.size < maxSlotsPerScatter) {
        log warn s"leaking slots $this idle=${_idleSlots.size} zombie=${_zombieSlots.size} $tag"
        while ((_zombieSlots.size + _idleSlots.size) < maxSlotsPerScatter)
          _idleSlots add TeslaScatterSlot(this, _slotId.getAndIncrement)
      }
      _zombieSlots.foreach { s =>
        makeSlotIdle(s._2)
        _zombieSlots.remove(s._1)
      }
    } finally unlockScatter("open")
    TeslaScatterReporter.scatterOpen()
    this
  }

  /**
   * internal close routine used by the framework.
   * After the scatter is closed all slots should have been returned to either the
   * _idleSlots or _zombieSlots collections
   */
  private[scatter]
  def close: TeslaScatter = {
    lazy val tag = s"TeslaScatter.close(guid=${_guid})"
    lockScatter("close")
    TeslaScatterReporter.scatterClose(_guid, _successSlots, _failSlots)
    try {
      _scatterState = TeslaScatterStopState

      zombifySlots()
      log info s"TESLA_SCATTER_STOPPED zombieSlots=${_zombieSlots.size} $tag"

      _successSlots foreach makeSlotIdle
      _successSlots.clear()

      _failSlots foreach makeSlotIdle
      _failSlots.clear()

      _updates.clear()

      _guid = null
      _timeoutAfter = Duration.Inf
    } finally unlockScatter("close")
    this
  }

  /**
   * Converts any remaining active slots to zombie slots.
   * `request.cancel` is responsible for acutally notifying the slot that it has been cancelled.
   */
  private[scatter] def zombifySlots(): Unit = {
    for ((_, slot) <- _activeSlots) {
      slot.request.cancel()
    }
  }

  private[scatter] def isActive(slot: TeslaScatterSlot): Boolean = _activeSlots.contains(slot.slotId)

  private[scatter] def markSlotZombie(slot: TeslaScatterSlot): Unit = {
    _zombieSlots += (slot.slotId -> slot)
    _activeSlots -= slot.slotId
  }

  private[scatter] def makeSlotIdle(slot: TeslaScatterSlot): Unit = {
    if (debugScatters)
      log info s"TeslaScatter.makeSlotIdle $slot"
    _idleSlots add slot.close
  }

}
