/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test

import io.opentelemetry.context.Context

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Executors, LinkedBlockingQueue, TimeoutException}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.errors.messageFromException
import org.burstsys.vitals.reporter.instrument._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutorService, Future}
import scala.language.postfixOps

object VitalsTest {

  private final
  class VitalsTestTask(val jobName: String, val taskId: Int, body: => Unit) {
    def execute(): Unit = body
  }

  /**
   * Execute a fixed set of tasks at a fixed concurrency (parallelism). Handle errors so that
   * unit tests still function correctly.
   *
   * @param jobName
   * @param item
   * @param concurrency
   * @param timeout
   * @param taskBody
   */
  def parallelExecute(jobName: String, item: String, concurrency: Int, timeout: Duration, taskBody: => Unit): Unit = {
    val completedTasks = new AtomicLong
    val taskIds = new AtomicLong
    val jobStart = System.nanoTime
    implicit val ex: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(
      Context.taskWrapping(Executors.newFixedThreadPool(concurrency))
    )
    val taskQueue = new LinkedBlockingQueue[VitalsTestTask]

    for (i <- 0 until concurrency) {
      taskQueue put new VitalsTestTask(jobName, i, taskBody)
    }

    def jobMsg: String = s"JOB[$jobName] $concurrency tasks, concurrency=$concurrency"

    val allTasks = for (i <- 0 until concurrency) yield
      Future[Throwable] {
        val tn = Thread.currentThread.getName
        Thread.currentThread.setName(s"VitalsTestWorker#$i")
        var continue = true
        var failure: Throwable = null
        while (continue) {
          val task = taskQueue.poll()
          if (task == null) {
            continue = false
          } else {
            val taskId = taskIds.getAndIncrement()
            val taskStart = System.nanoTime
            log info s"$jobMsg TASK #$taskId START"
            failure = try {
              task.execute()
              null
            } catch safely {
              case t: Throwable => t
            } finally completedTasks.incrementAndGet
            if (failure == null) {
              val taskElapsedTime = System.nanoTime - taskStart
              val jobElapsedTime = System.nanoTime - jobStart
              log info
                s"""
                   |-------------------------------------------------------
                   |$jobMsg
                   |TASK #$taskId COMPLETED IN ${prettyTimeFromNanos(taskElapsedTime)} ${completedTasks.get}/$concurrency COMPLETE IN ${prettyTimeFromNanos(System.nanoTime - jobStart)}
                   |${prettyRateString("task", completedTasks.get, jobElapsedTime)} / ${prettyPeriodString("task", completedTasks.get, jobElapsedTime)}
                   |-------------------------------------------------------""".stripMargin
            } else {
              log error
                s"""
                   |-------------------------------------------------------
                   |$jobMsg
                   |TASK #$taskId FAILED: '${failure.getMessage}'
                   |-------------------------------------------------------""".stripMargin
              continue = false
            }
          }
        }
        Thread.currentThread.setName(tn)
        failure
      }

    val failures = try {
      Await.result(Future.sequence(allTasks), timeout).filter(_ != null)
    } catch safely {
      case i: InterruptedException =>
        throw new RuntimeException(s"'$jobName' INTERRUPTED")
      case i: TimeoutException =>
        throw new RuntimeException(s"'$jobName' TIMEOUT")
    }
    ex.shutdown()
    if (failures.nonEmpty) {
      val msg = failures.map(messageFromException).mkString(", ")
      throw new RuntimeException(s"'$jobName' FAIL: $msg")
    }

    val jobElapsedTime = System.nanoTime - jobStart
    log info
      s"""
         |----------------------------------------------------
         |$jobMsg COMPLETED IN ${prettyTimeFromNanos(jobElapsedTime)}
         |${prettyRateString("task", completedTasks.get, jobElapsedTime)} / ${prettyPeriodString("task", completedTasks.get, jobElapsedTime)}
         |---------------------------------------------------- """.stripMargin
  }


}
