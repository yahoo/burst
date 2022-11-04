/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.worker.cache

import org.burstsys.vitals.logging._

import scala.concurrent.duration.{Duration, DurationInt}
import scala.language.postfixOps

package object lifecycle extends VitalsLogger {

  /**
   * Salient points
   * <ol>
   * <li>loads are not high transactions rates so this is likely not that (concurrency) performance sensitive</li>
   * <li>concurrency profile is only against the ''same'' dataset (all locking is per snap)</li>
   * <li>in order to make the design simpler and more fail proof, we allow a certain amount of ''busy wait'' because
   * we never block waiting on a read or write lock acquisition</li>
   * <li>the proportion of time that we don't get our waits woken by a state change is small -- the proportion of
   * time in the wait compared to the loop execution is high</li>
   * <li>most likely issue is where there are a bunch of waves against the same snap where one is doing a long cold load</li>
   * <li>in that case we allow for the max cold load time of loops</li>
   * </ol>
   *
   * @param waitQuantum how often we drop out of wait states
   * @param maxWait     max time to loop around
   * @param maxFails    max fails in snap
   */
  sealed case class FabCacheLoopTuner(waitQuantum: Duration, maxWait: Duration, maxFails: Int = 1) {
    def waitQuantumMs: Long = waitQuantum.toMillis

    def maxLoops: Long = maxWait.toMillis / waitQuantumMs
  }

  final object loadLoopTuner extends FabCacheLoopTuner(1 second, 15 minutes)

  final object tenderLoopTuner extends FabCacheLoopTuner(10 second, 1 minute)

}
