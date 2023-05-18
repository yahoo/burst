/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{ExecutorService, ThreadFactory}

import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import scala.concurrent.{ExecutionContext, Future}

package object thread extends VitalsLogger {

  /**
   * we would like to have all threads in mainstream code paths be marked with this trait.
   */
  private[thread] trait TeslaThread

  /**
   * framework for all thread pools
   */
  private[thread] trait TeslaThreadPool {

    /** @return helpful pool name  */
    def poolName: String

    def priority: Int = Thread.NORM_PRIORITY

    /** @return the java executor service used by the ExecutionContext  */
    def pool: ExecutorService

    final def threadName = f"$poolName-${id.incrementAndGet}%02d"

    /** monotonically incrementing id to keep track of threads */
    private[this] final val id = new AtomicLong

    protected final lazy val factory: ThreadFactory = (r: Runnable) => {
      val t = instantiateThread(r, threadName)
      t.setDaemon(true)
      t.setPriority(priority)
      t
    }

    /** allocate a custom thread for part pool usage */
    protected def instantiateThread(r: Runnable, name: String): Thread

    /** this is picked up by scala futures etc */
    final lazy val executor: ExecutionContext = {
      assert(pool != null)
      ExecutionContext.fromExecutorService(pool,
        t => log warn burstStdMsg(s"currentThread=${Thread.currentThread.getName} $poolName threw ${t: String} \n${printStack(t)}")
      )
    }
    log info s"TESLA_THREAD_POOL_START ${getClass.getSimpleName}($poolName)"
  }

}
