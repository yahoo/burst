/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.thread

import io.opentelemetry.context.Context

import java.util.concurrent.{Executor, ExecutorService, Executors, SynchronousQueue, TimeUnit}
import org.burstsys.tesla.part.TeslaPartPool
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.threading.burstThreadGroupGlobal

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, CanAwait, ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.Try

package object request {

  private lazy
  val teslaCommonRequestPool: TeslaRequestThreadPool = TeslaRequestThreadPool(poolName = "tesla-request")

  lazy val teslaRequestExecutorService: ExecutorService = teslaCommonRequestPool.pool

  /**
   * the shared request pool executor
   */
  implicit lazy
  val teslaRequestExecutor: ExecutionContext = teslaCommonRequestPool.executor

  /**
   * request threads are for use in IO or waiting, non [[TeslaPartPool]] allocating work. There are
   * essentially an unlimited number for these. Generally you do NOT want to use these for
   * work that ties up CPU CORES for any length of time. Use [[org.burstsys.tesla.thread.worker.TeslaWorkerThread]]
   * instances for that
   */
  private[thread] trait TeslaRequestThread extends TeslaThread

  /**
   * is the current thread is in fact a [[TeslaRequestThread]]?
   *
   * @return
   */
  final
  def inTeslaRequestThread: Boolean = Thread.currentThread.isInstanceOf[TeslaRequestThread]

  /**
   * assert that the current thread is in fact a [[TeslaRequestThread]]
   */
  final
  def assertInTeslaRequestThread(): Unit = if (!inTeslaRequestThread)
    throw VitalsException(s"not in request thread!")

  /**
   * wait for an array of futures to all complete using a tesla request thread callback
   *
   * @param futures
   * @tparam T
   * @return
   */
  implicit def teslaRequestFutureFromFutures[T](futures: Array[Future[T]]): Future[List[T]] =
    Future.sequence(futures.toList)

  implicit class FutureSemanticsEnhancer[T](val f: Future[T]) {
    def chainWithFuture[S](mapFn: T => Future[S])(implicit exec: ExecutionContext): Future[S] = f.flatMap(mapFn)(exec)
  }

  /**
   * A future that is restricted to the common shared tesla request thread pool
   */
  object TeslaRequestFuture {
    final def apply[T](body: => T): Future[T] = {
      Future {
        assertInTeslaRequestThread()
        body
      }
    }
  }

  /**
   * couple an arbitrary incoming thread and pass synchronous control to the common shared tesla request thread pool.
   * relatively untested exception pass - exception in tesla thread gets thrown in incoming thread...
   */
  object TeslaRequestCoupler {

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
        try {
          assertInTeslaRequestThread()
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
   * Thread Pool/Factory with a ''unlimited'' number of ''cached'' threads. Generally these are meant to be used where you have
   * a large number of ''requests'' that are best to allocate memory from the global [[TeslaPartPool]]
   *
   * @param poolName
   */
  private final case
  class TeslaRequestThreadPool(poolName: String) extends TeslaThreadPool {

    override protected def instantiateThread(r: Runnable, name: String): Thread = {
      new Thread(burstThreadGroupGlobal, r, name) with TeslaRequestThread
    }

    override lazy val pool: ExecutorService = {
      val pool = Executors.newCachedThreadPool(factory).asInstanceOf[java.util.concurrent.ThreadPoolExecutor]
      pool.setMaximumPoolSize(1000)
      Context.taskWrapping(pool)
      // pool
    }
  }


}
