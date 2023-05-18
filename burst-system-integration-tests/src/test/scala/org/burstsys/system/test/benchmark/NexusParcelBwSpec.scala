/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test.benchmark

import org.burstsys.system.benchmark.NexusBenchmarkMain
import org.burstsys.system.benchmark.NexusBenchmarkMain.NexusBenchmarkerArguments
import org.burstsys.vitals.logging._
import org.scalatest.Ignore
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

@Ignore
class NexusParcelBwSpec extends AnyFlatSpec with Matchers {

  VitalsLog.configureLogging("supervisor", true)

  "Nexus" should "be the simplest" in {
    NexusBenchmarkMain.executeBwRun(
      NexusBenchmarkerArguments(

        servers = 1,
        clients = 1,
        density = 1,
        bytes = 1e9.toLong
      )
    )
  }


  "Nexus" should "be the longest" in {
    NexusBenchmarkMain.executeBwRun(
      NexusBenchmarkerArguments(

        servers = 1,
        clients = 1,
        density = 1,
        bytes = 10e9.toLong
      )
    )
  }
  "Nexus" should "be the shortest" in {
    NexusBenchmarkMain.executeBwRun(
      NexusBenchmarkerArguments(

        servers = 1,
        clients = 1,
        density = 1,
        bytes = 1e6.toLong
      )
    )
  }

  "Nexus" should "push the most concurrency" in {
    NexusBenchmarkMain.executeBwRun(
      NexusBenchmarkerArguments(

        servers = 8,
        clients = 4,
        density = 1,
        bytes = 10e9.toLong
      )
    )
  }

  "Nexus" should "push the most bytes" in {
    NexusBenchmarkMain.executeBwRun(
      NexusBenchmarkerArguments(

        servers = 1,
        clients = 4,
        density = 30,
        bytes = 10e9.toLong
      )
    )
  }

  "Nexus" should "be the most blah" in {
    NexusBenchmarkMain.executeBwRun(
      NexusBenchmarkerArguments(

        servers = 1,
        clients = 4,
        density = 1,
        bytes = 10e9.toLong
      )
    )
  }

  "Nexus" should "be the most normal" in {
    NexusBenchmarkMain.executeBwRun(
      NexusBenchmarkerArguments(

        servers = 1,
        clients = 8,
        density = 1,
        bytes = 10e9.toLong
      )
    )
  }

}
