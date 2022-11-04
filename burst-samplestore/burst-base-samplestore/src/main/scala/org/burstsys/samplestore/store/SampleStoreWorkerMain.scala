/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store

import org.burstsys.samplestore.store.container.worker.FabricStoreWorkerContainerContext
import org.burstsys.vitals.logging.VitalsLog

object SampleStoreWorkerMain {
  def main(args: Array[String]): Unit = {
    VitalsLog.configureLogging("synthetic-worker")
    val wrkr = new FabricStoreWorkerContainerContext()
    wrkr.start.run
  }
}
