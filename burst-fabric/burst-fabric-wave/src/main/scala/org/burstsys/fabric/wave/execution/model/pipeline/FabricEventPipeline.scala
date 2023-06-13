/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.pipeline

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{ArrayBlockingQueue, ConcurrentHashMap, TimeUnit}

import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsSingleton}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._
import org.burstsys.vitals.threading.burstThreadGroupGlobal
import org.burstsys.vitals.uid._

/**
  * The base trait for any pipeline event.
  *
  * Pipeline events are notifications of activity about a particular subject.
  */
trait PipelineEvent {
  /**
    * The guid identifying the subject that generated this event.
    */
  def guid: VitalsUid
}

trait PipelineListener[EventType <: PipelineEvent] {
  def onEvent: PartialFunction[EventType, Boolean]
}

/**
  * This is carefully designed to efficiently handle what may be a fairly high event rate. These
  * events are ''not'' supposed to be used for anything but ''information'' i.e. they are for
  * the dash or metrics etc. So we assume that some delays are ok in the name of efficiency and not
  * blocking anything. This may be a source of GC churn - we may need to massage that...
  */
class FabricEventPipeline[EventType <: PipelineEvent](name: String, queueSize: Int = 1e5.toInt) extends VitalsService {

  private[this]
  val _listeners = ConcurrentHashMap.newKeySet[PipelineListener[EventType]]

  private[this]
  val _events = new ArrayBlockingQueue[EventType](queueSize)

  private[this]
  var _talker: Talker = _

  override def toString: String = s"MessageTalker($name, $queueSize)"

  final def register(listener: PipelineListener[EventType]): Unit = _listeners.add(listener)

  final def publish(event: EventType): Unit = {
    val added = _events.offer(event)
    if (!added)
      log warn s"Failed to add event $this, consider increasing queue size from $queueSize"
  }

  /**
    * background thread handling one event at a time on a single daemon thread - async hopefully efficient
    * handling of a high rate of events...
    */
  private case class Talker() {
    private var _thread: Thread = _
    private val running = new AtomicBoolean(false)

    private def shouldContinue: Boolean = running.get()

    def talk(): Unit = {
      running.set(true)
      _thread = new Thread(burstThreadGroupGlobal, new Runnable {
        final override def run(): Unit = {
          log info s"$name starting..."

          while (shouldContinue) {
            // just poll quickly in case we have to shut down...
            _events.poll(2, TimeUnit.SECONDS) match {
              case null =>
                if (!shouldContinue) return
              case op =>
                if (!shouldContinue) return
                val iter = _listeners.iterator

                if (!iter.hasNext)
                  log debug "publishing event to nobody"

                while (iter.hasNext) {
                  val listener = iter.next
                  try {
                    if (listener.onEvent.isDefinedAt(op))
                      listener.onEvent(op)
                  } catch safely {
                    case t: Throwable =>
                      log error(burstStdMsg(s"$name error listener=${listener.getClass.getSimpleName} op=$op", t), t)
                  }
                }
            }
          }
          log info s"$name stopping, ${_events.size} undelivered events."
        }
      }, name)

      _thread.setDaemon(true)
      _thread.setName(name)
      _thread.start()
    }

    def stop(): Unit = running.set(false)
  }

  override def modality: VitalsServiceModality = VitalsSingleton

  override def start: this.type = {
    ensureNotRunning
    log info startingMessage
    _talker = Talker()
    _talker.talk()
    markRunning
    this

  }

  override def stop: this.type = {
    ensureRunning
    log info stoppedMessage
    _talker.stop()
    _talker = null
    markNotRunning
    this
  }
}
