/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.service

import org.burstsys.samplesource.SampleSourceId
import org.burstsys.vitals.VitalsService

/**
  * implemented by each sample source to provide lifecycle and sample source specific operations
  */
trait SampleSourceService extends VitalsService with SampleSourceMasterService with SampleSourceWorkerService {

  /**
    * TODO shahd  - please document
    * @return
    */
  def id: SampleSourceId

}
