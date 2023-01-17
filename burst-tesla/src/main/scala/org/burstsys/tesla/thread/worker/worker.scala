/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.thread

import io.opentelemetry.context.Context
import org.burstsys.tesla.configuration
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._
import org.burstsys.vitals.threading.burstThreadGroupGlobal

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

package object worker {

  private lazy
  val teslaCommonWorkerPool: TeslaWorkerThreadPool = TeslaWorkerThreadPool(
    poolName = "tesla-worker", configuration.burstTeslaWorkerThreadCountProperty.get
  )

  private implicit lazy
  val teslaCommonWorkerExecutor: ExecutionContext = teslaCommonWorkerPool.executor

  private[thread] trait TeslaWorkerThread extends TeslaThread

  /**
   * is the current thread is in fact a [[TeslaWorkerThread]]?
   *
   * @return
   */
  final
  def inTeslaWorkerThread: Boolean = Thread.currentThread.isInstanceOf[TeslaWorkerThread]

  /**
   * assert that the current thread is in fact a [[TeslaWorkerThread]]
   */
  final
  def assertInTeslaWorkerThread(): Unit = if (!inTeslaWorkerThread) throw VitalsException(s"not in worker thread!")

  /**
   * Thread Pool/Factory with a ''fixed'' number of threads. Each of these threads is a [[TeslaWorkerThread]] which means
   * it is ''bound'' to a specific native thread
   *
   * @param poolName
   * @param threadCount
   */
  private case
  class TeslaWorkerThreadPool(poolName: String, threadCount: Int) extends TeslaThreadPool {

    override val priority: Int = Thread.NORM_PRIORITY - 1 // worker threads should run at lower priority

    override protected def instantiateThread(r: Runnable, name: String): Thread = {
      new Thread(burstThreadGroupGlobal, r, name) with TeslaWorkerThread
    }

    override lazy val pool: ExecutorService = Context.taskWrapping(Executors.newFixedThreadPool(threadCount, factory))
  }

  /**
   * couple an arbitrary incoming thread and pass synchronous control to the shared tesla common worker pool.
   * relatively untested exception pass - exception in tesla thread gets thrown in incoming thread...
   */
  object TeslaWorkerCoupler {

    def workerCount: Int = teslaCommonWorkerPool.threadCount

    /**
     *
     * @param body    code to execute in the new future
     * @param timeout how long to wait (default is infinite)
     * @tparam ResultType the return type
     * @return the return value
     */
    final def apply[ResultType <: Any](body: => ResultType, timeout: Duration = Duration.Inf): ResultType = {
      var pass: Throwable = null
      var result: ResultType = null.asInstanceOf[ResultType]
      Await.result(Future {
        assertInTeslaWorkerThread()
        try {
          result = body
        } catch safely {
          case t: Throwable =>
            pass = t
        }
        result
      }, timeout)
      // check for a throw in body
      if (pass != null) throw pass
      result
    }

  }


  /**
   * A future that is hosted on the shared tesla common worker pool
   */
  object TeslaWorkerFuture {

    def workerCount: Int = teslaCommonWorkerPool.threadCount

    final def apply[T](body: => T): Future[T] = {
      Future {
        assertInTeslaWorkerThread()
        body
      }
    }
  }

}
