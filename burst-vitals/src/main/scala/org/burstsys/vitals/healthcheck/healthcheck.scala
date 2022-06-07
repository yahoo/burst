/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._
import org.burstsys.vitals.threading.burstThreadGroupGlobal
import org.burstsys.vitals.time.Now
import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.joda.time.Period

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

package object healthcheck extends VitalsLogger {

  /**
   * TODO
   *
   * @param statusCode
   */
  sealed case class VitalsHealthStatus(statusCode: Int, notHealthy: Boolean)

  final object VitalsHealthHealthy extends VitalsHealthStatus(200, false) {
    override def toString: String = "healthy"
  }

  final object VitalsHealthMarginal extends VitalsHealthStatus(200, true) {
    override def toString: String = "marginal"
  }

  final object VitalsHealthUnhealthy extends VitalsHealthStatus(503, true) {
    override def toString: String = "unhealthy"
  }

  final case class VitalsComponentHealth(status: VitalsHealthStatus = VitalsHealthHealthy, message: String = "ok") {
    def asJson: String = s"""{"health": "$status", "msg": "${message.replace("\n", "\\n").replace("\"", "\\\"")}"}"""
  }

  lazy val threadFactory: ThreadFactory = new ThreadFactory {
    def newThread(r: Runnable): Thread = new Thread(burstThreadGroupGlobal, r, "health-check-service")
  }

  lazy val executor: ExecutorService = Executors.newFixedThreadPool(1, threadFactory)

  trait VitalsHealthMonitoredComponent {

    def componentName: String = this.getClass.getSimpleName.stripPrefix("Burst").stripSuffix("$").stripSuffix("Context").stripSuffix("Provider")

    def componentHealth: VitalsComponentHealth = VitalsComponentHealth()

  }

  trait VitalsHealthStatusDelegate {

    /**
     *
     * @return
     */
    def healthCheckPort: Int

    /**
     *
     * @param port
     */
    def healthCheckPort_=(port: Int): Unit

    /**
     * TODO
     *
     * @param left
     * @param right
     * @return
     */
    final def aggregateHealth(left: VitalsHealthStatus, right: VitalsHealthStatus): VitalsHealthStatus = {
      if (left == VitalsHealthUnhealthy || right == VitalsHealthUnhealthy) {
        VitalsHealthUnhealthy
      } else if (left == VitalsHealthMarginal || right == VitalsHealthMarginal) {
        VitalsHealthMarginal
      } else left
    }

    /**
     *
     * @param componentResults
     * @return
     */
    def overallHealth(componentResults: ConcurrentHashMap[String, VitalsComponentHealth]): VitalsComponentHealth = {
      var health: VitalsHealthStatus = VitalsHealthHealthy
      var healthySystems = 0
      var marginalSystems = 0
      var unhealthySystems = 0
      val iter = componentResults.entrySet.iterator
      while (iter.hasNext) {
        val entry = iter.next
        val status = entry.getValue
        health = aggregateHealth(health, status.status)
        status.status match {
          case VitalsHealthHealthy => healthySystems += 1
          case VitalsHealthMarginal => marginalSystems += 1
          case VitalsHealthUnhealthy => unhealthySystems += 1
          case _ => ???
        }
      }
      val healthMessage = if (healthySystems == componentResults.size) {
        "All systems healthy"
      } else {
        s"$healthySystems healthy system(s), $marginalSystems marginal system(s), $unhealthySystems unhealthy system(s)"
      }
      VitalsComponentHealth(health, healthMessage)
    }

  }

  trait VitalsHealthMonitoredService extends VitalsService with VitalsHealthMonitoredComponent {
    override def componentName: String = serviceName

    override def componentHealth: VitalsComponentHealth = {
      if (isRunning) {
        VitalsComponentHealth(VitalsHealthHealthy, "running")
      } else {
        VitalsComponentHealth(VitalsHealthUnhealthy, "not running")
      }
    }

  }

  /**
   * Limit the lifetime of the running process to a given duration. Useful for long running processes that need
   * to be restarted every so often. When both `time` and `period` are provided this component will never report a
   * healthy status for more that `period` length of time. If the component detects that expiring at the requested `time`
   * would cause it to exceed `period` uptime, then it will expire within 24 hours at `time`. In other words if-when
   * the service starts-`time` is in the future, then the service will expire at `time` o'clock on the day it starts.
   * The only exceptions to this rule are when `period` includes a month or year component (as these are vary in length).
   *
   * Examples:
   * <pre>
   * | Current time     | time  | period | next restart     |
   * | ---------------- | ----- | ------ | ---------------- |
   * | 2022-03-01T13:06 | null  | P1D    | 2022-03-02T13:06 |
   * | 2022-03-01T13:06 | 13:00 | P1D    | 2022-03-02T13:00 |
   * | 2022-03-01T13:06 | 23:00 | P1D    | 2022-03-01T23:00 |
   * | 2022-03-01T13:06 | 13:00 | P5D    | 2022-03-06T13:00 |
   * | 2022-03-01T13:06 | 23:00 | P5D    | 2022-03-01T23:00 |
   * | 2022-03-01T13:06 | 23:00 | P1M    | 2022-04-01T23:00 |
   * | 2022-03-01T13:06 | 13:00 | P40D   | 2022-04-10T13:00 |
   * | 2022-03-01T13:06 | 23:00 | P40D   | 2022-03-01T23:00 |
   * | 2022-03-01T13:06 | 23:00 | P1M    | 2022-04-01T23:00 |
   * | 2022-03-01T13:06 | 23:00 | P1Y    | 2023-03-01T23:00 |
   * </pre>
   * @param time   the time of day the lifetime will expire, if null then the current time
   * @param period how long from now until this component will report an unhealthy state
   */
  final case class VitalsHealthLifetimeComponent(time: LocalTime, period: Period)(implicit now: DateTime = Now) extends VitalsHealthMonitoredComponent {
    val expireTime: DateTime = {
      try {
        if (period.toStandardSeconds.getSeconds < 0)
          log error burstStdMsg(s"negative lifetime period $period")
        if (time != null && period.toStandardDays.getDays < 1)
          log warn burstStdMsg("setting an expire time for a period of less than 1 day will cause confusing behavior")
      } catch safely {
        case _ => log warn burstStdMsg(s"period cannot be converted to seconds: $period")
      }

      val afterPeriod = now.plus(period)
      if (time == null) {
        // no time of day specified, expire after one period
        afterPeriod
      } else if (period.getMonths > 0 || period.getYears > 0) {
        // months or years included, this is already a squishy expiration deadline
        afterPeriod.withTime(time)
      } else if (afterPeriod.withTime(time).getMillis > afterPeriod.getMillis) {
        // check if adjusting the end time to the requested time would cause us to exceed the specified period
        // if so, restart at the requested time so the next iteration will not exceed 1 period
        now.withTime(time)
      } else {
        // this period will be slightly shortened by setting time of day
        afterPeriod.withTime(time)
      }
    }
    log warn s"lifetime health check expires at $expireTime. now=$now time=$time"

    override def componentName = "lifetime"

    override def componentHealth: VitalsComponentHealth = {
      val now = Now
      if (now.getMillis - expireTime.getMillis > 0) {
        VitalsComponentHealth(VitalsHealthUnhealthy, s"lifetime of $period expired at $expireTime")
      } else {
        VitalsComponentHealth(VitalsHealthHealthy, s"lifetime of $period will expire at $expireTime")
      }
    }
  }

}
