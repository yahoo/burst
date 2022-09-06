/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test.performance

import java.util.concurrent._
import java.util.concurrent.atomic.{AtomicInteger, LongAdder}

import org.burstsys.brio.test.{BrioAbstractSpec, performance}
import org.burstsys.tesla
import org.burstsys.vitals.threading.burstThreadGroupGlobal

import scala.jdk.CollectionConverters._
import scala.collection.mutable.ArrayBuffer

object BrioBufferPerformanceMeter extends BrioAbstractSpec {

  import org.burstsys.vitals.instrument._

  def main(args: Array[String]): Unit = {
    BrioBufferPerformanceMeter.executeRun()
  }

  @transient val threadId = new AtomicInteger(1)

  @transient private var _ex: ExecutorService = _
  private final val taskCount = 1000.toInt
  private final val iterations = 5e8.toInt

  val counts: Int = taskCount * iterations

  val allocateTally = new LongAdder
  val releaseTally = new LongAdder

  implicit def ex: ExecutorService = {
    if (_ex == null) {
      _ex = Executors.newFixedThreadPool(taskCount, new ThreadFactory {
        def newThread(r: Runnable): Thread = {
          val t = new Thread(burstThreadGroupGlobal, r, f"Test${threadId.getAndIncrement}%04d")
          t.setDaemon(true)
          t
        }
      })
    }
    _ex
  }


  def executeRun(): Unit = {
    val counter = new CountDownLatch(taskCount)
    val tasks = new ArrayBuffer[Callable[Boolean]]
    var i = 0
    while (i < taskCount) {
      tasks += new Callable[Boolean] {
        def call(): Boolean = {
          {
            var j = 0
            while (j < iterations) {

              val t0 = System.nanoTime
              val b = tesla.buffer.factory.grabBuffer(500)
              allocateTally.add(System.nanoTime - t0)

              val t1 = System.nanoTime
              tesla.buffer.factory.releaseBuffer(b)
              releaseTally.add(System.nanoTime - t1)
              j += 1
            }
            counter.countDown()
          }
          true
        }
      }
      i += 1
    }

    val start = System.nanoTime
    ex.invokeAll(tasks.asJavaCollection)
    counter.await()


    performance.log info
      s"""
         |Allocate=${prettyRateString("allocation", counts, allocateTally.longValue)}
         |Release=${prettyRateString("releases", counts, releaseTally.longValue)}
       """.stripMargin

  }


  //  log info s"run complete... pressed ${prettySizeString(taskCount)} item(s) at a rate of ${prettyRateString("object", items, end - start)}"

}
