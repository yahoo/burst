/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.provider

import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

import org.burstsys.dash.endpoints.{ClientJsonObject, ClientWebsocketMessage}
import org.burstsys.vitals.time.VitalsTimeZones

package object profiler {
  val eventIndex = new AtomicInteger()

  final case
  class ProfilerAttachResponse(
                                source: String = "",
                                domain: Long = -1, view: Long = -1,
                                timezone: String = VitalsTimeZones.VitalsDefaultTimeZoneName,
                                running: Boolean = false,
                                concurrency: Int = 4, executions: Int = 50, loads: Int = 10,
                                events: Array[ProfilerEvent] = Array.empty
                              ) extends ClientJsonObject

  final case
  class ProfilerConfig(
                        source: String = "", domain: Long = -1, view: Long = -1,
                        timezone: String = VitalsTimeZones.VitalsDefaultTimeZoneName,
                        running: Boolean = false,
                        concurrency: Int = 4,
                        executions: Int = 50,
                        loads: Int = 10
                      ) extends ClientJsonObject

  final case
  class ProfilerRunResponse(
                             running: Boolean = true,
                             success: Boolean = true,
                             concurrency: Int = 1,
                             executions: Int = 1,
                             loads: Int = 1
                           ) extends ClientJsonObject

  final case
  class ProfilerStopResponse(running: Boolean = false) extends ClientJsonObject

  final case
  class ProfilerEvent(
                       isRunning: Boolean = true, isError: Boolean = false,
                       index: Int = eventIndex.incrementAndGet(),
                       time: Long = new Date().getTime, elapsed: Long = 0,
                       success: Long = 0, failure: Long = 0,
                       scanCount: Long = 0, scanTime: Long = 0,
                       loadCount: Long = 0, loadTime: Long = 0, loadSize: Long = 0,
                       message: String = "test"
                     ) extends ClientJsonObject


  object ProfilerRelayMessage {
    def apply(config: ProfilerConfig, events: Array[ProfilerEvent]): ClientWebsocketMessage =
      ProfilerAttachMessage("profiler-attach", config, events)

    def apply(event: ProfilerEvent): ClientWebsocketMessage =
      ProfilerEventMessage("profiler-event", event)
  }

  private final
  case class ProfilerAttachMessage(msgType: String, config: ProfilerConfig, events: Array[ProfilerEvent])
    extends ClientWebsocketMessage

  private final
  case class ProfilerEventMessage(msgType: String, event: ProfilerEvent)
    extends ClientWebsocketMessage


  trait BurstDashProfilerListener {
    def profilerStarted(config: ProfilerConfig): Unit = {}

    def profilerEvent(event: ProfilerEvent): Unit = {}

    def profilerStopped(): Unit = {}
  }


  trait BurstDashProfilerService {
    def startProfiler(source: String, domain: Long, view: Long, timezone: String,
                      concurrency: Int, executions: Int, reloadEvery: Int): ProfilerRunResponse

    def stopProfiler: ProfilerStopResponse

    def config: ProfilerConfig

    def getEvents: Array[ProfilerEvent]

    def talksTo(listener: BurstDashProfilerListener): Unit
  }

}
