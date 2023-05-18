/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.test.source

import org.burstsys.samplesource.service.SampleSourceService

/**
 * A sample source that returns generated data
 */
case class TestSampleSourceService()
  extends SampleSourceService[TestSampleSourceSupervisor, TestSampleSourceWorker] {

  /**
   * @return the id of this sample source
   */
  override def name: String = TestSampleSourceName

  /**
   * @return the class responsible for computing view generations
   */
  override def supervisorClass: Class[TestSampleSourceSupervisor] = classOf[TestSampleSourceSupervisor]

  /**
   * @return the class responsible for feeding streams sent to cell workers
   */
  override def workerClass: Class[TestSampleSourceWorker] = classOf[TestSampleSourceWorker]
}
