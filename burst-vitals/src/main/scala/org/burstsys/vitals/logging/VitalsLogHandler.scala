/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.logging

import org.apache.logging.log4j.Level

import scala.jdk.CollectionConverters._


object VitalsLogHandler {

  case class VitalsLogEvent(timestamp: String, level: Level, thread: String, source: String, message: String)

  def getEvents(lines: Int): List[VitalsLogEvent] = {
    VitalsLoggingAppender.getEvents(VitalsLoggingAppender.bufferSize).asScala.toList
  }

}
