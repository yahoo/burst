/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.main

import org.burstsys.vitals.logging.VitalsLog
import org.burstsys.vitals.logging.VitalsLogger

object ChooseWorkload extends VitalsLogger {
  def main(args: Array[String]): Unit = {
    VitalsLog.configureLogging("WorkloadSelector")

    log info "You must set the WORKLOAD env var to either 'supervisor' or 'worker'"
  }
}
