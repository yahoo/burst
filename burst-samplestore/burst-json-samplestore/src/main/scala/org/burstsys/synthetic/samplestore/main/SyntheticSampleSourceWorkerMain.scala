/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore.main

import org.burstsys.synthetic.samplestore.SyntheticSampleStoreContainer
import org.burstsys.vitals.VitalsService.VitalsStandardClient
import org.burstsys.vitals.VitalsService.VitalsStandardServer
import org.burstsys.vitals.logging.VitalsLog

import java.util.concurrent.TimeUnit

object SyntheticSampleSourceWorkerMain {
  def main(args: Array[String]): Unit = {
    VitalsLog.configureLogging("synthetic-worker")
    SyntheticSampleStoreContainer(VitalsStandardClient).start.run()
  }
}
