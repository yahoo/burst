/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test

import org.burstsys.vitals.instrument.prettyTimeFromNanos
import org.scalatest.Ignore

import scala.jdk.CollectionConverters._

@Ignore
class VitalsAsScalaVsStreamToArrayTest extends VitalsAbstractSpec {

  private val lists = for (length <- 10000 until(100000, 10000)) yield {
    val l = new java.util.ArrayList[Object](length)
    for (_ <- 0 until length) { l.add(new Object())}
    l.asInstanceOf[java.util.List[Object]]
  }

  it should "use asScala to create an array" in {
    lists foreach { list =>
      val avgTime = iterateTiming(list.asScala.map(_.hashCode().toString).toArray)
      log info s"Mapped array of ${list.size} in ${prettyTimeFromNanos(avgTime)} with asScala"
    }
  }

  it should "use java streams to create an array" in {
    lists foreach { list =>
      val avgTime = iterateTiming(list.stream.map(_.hashCode().toString).toArray(new Array[String](_)))
      log info s"Mapped array of ${list.size} in ${prettyTimeFromNanos(avgTime)} with stream"
    }
  }

  it should "use java streams to create an array2" in {
    lists foreach { list =>
      val avgTime = iterateTiming(list.stream.map(_.hashCode().toString).toArray(new Array[String](_)))
      log info s"Mapped array of ${list.size} in ${prettyTimeFromNanos(avgTime)} with stream"
    }
  }

  it should "use asScala to create an array2" in {
    lists foreach { list =>
      val avgTime = iterateTiming(list.asScala.map(_.hashCode().toString).toArray)
      log info s"Mapped array of ${list.size} in ${prettyTimeFromNanos(avgTime)} with asScala"
    }
  }

  private def iterateTiming(work: => Unit): Long = {
    // warmup
    for (_ <- 0 until 100) yield {
      work
    }

    val timings = for (_ <- 0 until 1000) yield {
      val start = System.nanoTime()
      work
      System.nanoTime() - start
    }

    timings.sum / timings.length
  }
}
