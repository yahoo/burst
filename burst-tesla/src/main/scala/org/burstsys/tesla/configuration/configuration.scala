/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla

import org.burstsys.tesla
import org.burstsys.vitals.properties.VitalsPropertyRegistry
import org.burstsys.vitals.properties.VitalsPropertySpecification

import java.lang.Runtime.getRuntime
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._
import scala.language.postfixOps

package object configuration extends VitalsPropertyRegistry {

  /////////////////////////////////////////////////////////////////////////////////////////////////////
  // UNIT TESTS
  /////////////////////////////////////////////////////////////////////////////////////////////////////

  def configureForUnitTests(): Unit = {
    tesla.configuration.burstTeslaWorkerThreadCountProperty.set(Runtime.getRuntime.availableProcessors())
    tesla.configuration.teslaPartsTenderProperty.set(30) // 30 seconds
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////
  // REQUEST THREADS
  /////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * maximum number of in flight request threads before rejecting the request.
   * This prevents the number of requests to overwhelm the system.
   */
  val burstTeslaMaxRequestThreadCountProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.tesla.request.max.threads",
    description = "the maximum number of threads in the shared tesla request pool",
    default = Some(5000)
  )

  /////////////////////////////////////////////////////////////////////////////////////////////////////
  // WORKER THREADS
  /////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * how many threads in the singleton JVM global fixed thread pool. This pool contains threads
   * that are the only ones allowed to do [[org.burstsys.tesla.part.TeslaPartShop]]
   * allocations and are dedicated to non-waiting, non-IO, '''100% CPU bound''' processing.
   * These are '''EXPENSIVE''' threads in that they allocate/free, thread-bound,
   * Tesla off heap memory freely, cache it for long periods of time, and do expensive CPU heavy processing
   * on that memory. Set this property carefully especially on shared containers/hosts. These threads run at
   * a slightly lower priority in order to avoid starvation in non worker pool functionality. The reason
   * there is only one of these pools per JVM is to be sure to share these expensive threads across as many
   * functions as possible.
   * '''MORE IS NOT ALWAYS BETTER'''!!
   */
  val burstTeslaWorkerThreadCountProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.tesla.worker.threads",
    description = "the number of threads in the shared tesla worker pool",
    default = Some(getRuntime.availableProcessors)
  )

  /////////////////////////////////////////////////////////////////////////////////////////////////////
  // WORKER PART MEMORY
  /////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * how often to tend tesla parts pools and free where appropriate
   */
  val teslaPartsTenderProperty: VitalsPropertySpecification[Long] = VitalsPropertySpecification[Long](
    key = "burst.tesla.parts.tender.frequency.sec",
    description = "how often to tend the tesla parts pools",
    default = Some((1 minutes).toSeconds)
  )

  /**
   * how often to tend tesla parts pools and free where appropriate
   */
  def teslaPartsTenderInterval: Duration = Duration(teslaPartsTenderProperty.get, TimeUnit.SECONDS)

  /**
   * TTL for LRU freeing policy
   */
  val teslaPartsTtlProperty: VitalsPropertySpecification[Long] = VitalsPropertySpecification[Long](
    key = "burst.tesla.parts.tender.ttl.sec",
    description = "how long before parts pool contents become stale",
    default = Some((3 minute).toSeconds)
  )

  /**
   * TTL for LRU freeing policy
   */
  def teslaPartsTtlInterval: Duration = Duration(teslaPartsTtlProperty.get, TimeUnit.SECONDS)

  /////////////////////////////////////////////////////////////////////////////////////////////////////
  // SCATTERS
  /////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * how often to check status of all slots in an ongoing scatter operation
   */
  val scatterTenderIntervalProperty: VitalsPropertySpecification[Long] = VitalsPropertySpecification[Long](
    key = "burst.tesla.scatter.tender.interval.ms",
    description = "the interval that scatters check for timeout and tardy slots",
    default = Some((5 seconds).toMillis)
  )

  /**
   * how often to check status of the status of all slots in an ongoing scatter operation
   */
  def scatterTenderPeriod: Duration = Duration(scatterTenderIntervalProperty.get, TimeUnit.MILLISECONDS)

}
