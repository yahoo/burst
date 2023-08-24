/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import io.opentelemetry.context.Context

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Executors, ThreadFactory}
import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.threading.burstThreadGroupGlobal

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}
import scala.language.postfixOps

package object background extends VitalsLogger {

  private
  val requestThreadId = new AtomicInteger(1)

  lazy val backgrounderThreadFactory: ThreadFactory = new ThreadFactory {
    def newThread(r: Runnable): Thread = {
      val t = new Thread(burstThreadGroupGlobal, r, f"vitals-background-${requestThreadId.getAndIncrement}%04d")
      t.setDaemon(true)
      t
    }
  }

  implicit val backgrounderExecutor: ExecutionContextExecutorService = {
    val pool = Context.taskWrapping(Executors.newCachedThreadPool(backgrounderThreadFactory))
    ExecutionContext.fromExecutorService(pool)
  }

}
