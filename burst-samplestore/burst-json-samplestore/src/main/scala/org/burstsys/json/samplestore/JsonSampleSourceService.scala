/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.json.samplestore

import org.burstsys.json.samplestore.master.JsonSampleSourceMasterService
import org.burstsys.json.samplestore.worker.JsonSampleSourceWorkerService
import org.burstsys.samplesource.service.SampleSourceService

class JsonSampleSourceService()
  extends SampleSourceService[JsonSampleSourceMasterService, JsonSampleSourceWorkerService] {

  override def name: String = JsonBrioSampleSourceName

  /**
   * @return the class responsible for computing view generations
   */
  override def coordinatorClass: Class[JsonSampleSourceMasterService] = classOf[JsonSampleSourceMasterService]

  /**
   * @return the class responsible for feeding streams sent to cell workers
   */
  override def workerClass: Class[JsonSampleSourceWorkerService] = classOf[JsonSampleSourceWorkerService]
}
