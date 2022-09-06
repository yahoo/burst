/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.torcher

import java.util.concurrent.TimeUnit

import org.burstsys.torquemada.{Driver, Parameters, log}
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging.{VitalsLog, _}

object Main {
  val header = "\n---------------------------------------------------------------------\n"
  val footer = "\n---------------------------------------------------------------------"


  def main(args: Array[String]): Unit = {
    Thread.currentThread setName "BurstTorcherMain"

    VitalsLog.configureLogging("torcher")

    val torcherParameters = Parameters.parseArguments(args) match {
      case None =>
        System.exit(-1)
        null
      case Some(p) => p
    }

    log info s"${header}Burst Torcher $torcherParameters$header"

    try {
      // open the clients
      val (catalogClient, agentClient) = Driver.openClients(torcherParameters)
      val driver = Driver(torcherParameters, agentClient, catalogClient)

      driver.addListener({ (level, msg) =>
        // just report the message to the log
        log.log(level, msg)
      })

      // create a reporter
      val reporterTimer = new java.util.Timer()
      val reporterTask = new java.util.TimerTask {
        def run(): Unit = {
          // report intermediary results
          log info driver.intermediaryStats()
        }
      }
      reporterTimer.schedule(reporterTask, TimeUnit.MINUTES.toMillis(1), torcherParameters.reportingInterval.toMillis)
      // try to clean up the temporary views if possible
      Runtime.getRuntime.addShutdownHook(new Thread {
        override def run(): Unit = {
          driver.cleanup()
          reporterTask.cancel()
        }
      })


      driver.start.run
      reporterTask.cancel()
      // report final results
      log info s"Concurrency:  ${torcherParameters.parallelism} ${driver.intermediaryStats()}"
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        System.exit(-1)
    }

    System.exit(0)

  }

  def reporter(): Unit = {
  }

}
