/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.logging

import org.apache.logging.log4j.core.LoggerContext

import java.util.concurrent.atomic.AtomicBoolean
import org.burstsys.vitals.configuration.{burstLog4j2FileProperty, burstLog4j2NameProperty}
import org.burstsys.vitals.errors.{VitalsException, safely}
import org.burstsys.vitals.host
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.io.IoBuilder
import org.apache.logging.log4j.{Level, LogManager, Logger}

import scala.jdk.CollectionConverters.CollectionHasAsScala

object VitalsLog {

  private[this] val initialized = new AtomicBoolean(false)

  private[this] var context: LoggerContext = _

  def isInitialized: Boolean = initialized.get()

  def getJavaLogger(clazz: Class[_]): Logger = if (isInitialized) LogManager.getLogger(clazz) else
    throw VitalsException(s"Use of log ${clazz.getName} before initializing logging")

//  def setLogLevel(logger: String, level: Level): Unit = {
//    Configurator.setLevel(logger, level)
//  }
//
//  def allLogLevels(): Map[String, Level] = {
//    context.getLoggerRegistry.getLoggers.asScala.map(l => (l.getName, l.getLevel)).toMap
//  }

  /**
    *
    * @param logName unique of the log file for this process
    * @return
    */
  def configureLogging(logName: String, consoleOnly: Boolean = false): this.type = {
    if (initialized.getAndSet(true))
      return this

    System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")

    // if we are a test then just use the default console error only log
    if (consoleOnly)
      return this

    // set for workers
    burstLog4j2NameProperty.set(logName)
    val logFile = burstLog4j2FileProperty.get
    val burstHome = org.burstsys.vitals.configuration.burstHomeProperty.get
    log info s"INITIALIZING LOG FROM `$logFile` USING CONTEXT `$logName`"
    System.setProperty("burst.log.location", logName)
    System.setProperty("log.home", burstHome)
    try {
      context = Configurator.initialize(null, logFile)

      // set up Java Utils Logging
      bridgeJul
/*
      // redirect any out or err to the logger too
      System.out.println("redirecting System.out")
      System.setOut(
        IoBuilder.forLogger(LogManager.getLogger("system.out"))
          .setLevel(Level.INFO)
          .buildPrintStream()
      )
      System.out.println("redirected System.out")
      System.out.println("redirecting System.err")
      System.setErr(
        IoBuilder.forLogger(LogManager.getLogger("system.err"))
          .setLevel(Level.INFO)
          .buildPrintStream()
      )
      System.out.println("redirected System.err")
*/

      val location = System.getProperty("burst.log.location")
      log info s"INITIALIZED LOG FROM `${context.getConfiguration.getName}` USING CONTEXT `$location`"
    } catch safely {
      case t: Throwable =>
        System.err.print(s"BurstLog.VitalsLog.configureLogging('$logFile') threw $t")
        System.exit(-1)
    }

    // Adding a unique log to stderr, so splunk indexes it
    System.err.println(s"host=${host.hostName}, nanoTime=${System.nanoTime()}, uptime: ${host.uptime}")
    this
  }


}
