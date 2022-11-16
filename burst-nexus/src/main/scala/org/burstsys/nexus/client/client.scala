/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus

import org.burstsys.nexus.bench.NexusNetBench
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.reporter.instrument._
import org.burstsys.vitals.logging._

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success}

package object client extends VitalsLogger with NexusClientPool {

  final val clientConfig = NexusConfig(
    isServer = false,
    maxBytesBetweenFlush = 1e3.toLong,
    maxPacketsBetweenFlush = 1,
    maxNsBetweenFlush = (100 millis).toNanos,
    lowWaterMark = 10,
    highWaterMark = 50
  )

  /**
   * run a quick benchmark on a new host endpoint
   *
   * @param client
   * @return
   */
  def benchmarkClient(client: NexusClient): NexusClient = {
    val promise = Promise[Unit]()
    val benchmarkSize = 500 * MB
    NexusNetBench(benchmarkSize).benchmark(client) onComplete {
      case Failure(t) => promise.failure(t)
      case Success(t) => promise.success(())
    }
    Await.result(promise.future, 5 minutes)
    client
  }


}
