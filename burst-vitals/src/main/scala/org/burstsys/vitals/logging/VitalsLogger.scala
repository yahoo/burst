/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.logging

import org.burstsys.vitals.burstPackage
import org.burstsys.vitals.errors._
import org.burstsys.vitals.net._
import org.apache.logging.log4j.{LogManager, Logger}

trait VitalsLogger {

  private val logName = getClass.getName.stripSuffix(".package$")

  final lazy val log: Logger =
    if (VitalsLog.isInitialized)
      LogManager.getLogger(logName)
    else
      throw VitalsException(s"Use of logger $logName before initializing logging system")


  final
  def burstThreadName: String = s"'${Thread.currentThread.getName}'"

  final implicit
  lazy val burstModuleName: BurstModuleName = s"BURST${
    getClass.getPackage.getName.stripPrefix(
      burstPackage
    ).toUpperCase.replaceAll("\\.", "_")
  }:"

  final
  lazy val burstHost: VitalsHostName = s"$getPublicHostName ($getPublicHostAddress)"



}
