/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore.service

import org.burstsys.samplesource.service.SampleSourceService

/**
 * A sample source that returns generated data
 */
case class SyntheticSampleStoreService()
  extends SampleSourceService[SyntheticSampleSourceCoordinator, SyntethicSampleSourceWorker] {

  /**
   * @return the id of this sample source
   */
  override def name: String = SynteticSampleSourceName

  /**
   * @return the class responsible for computing view generations
   */
  override def coordinatorClass: Class[SyntheticSampleSourceCoordinator] = classOf[SyntheticSampleSourceCoordinator]

  /**
   * @return the class responsible for feeding streams sent to cell workers
   */
  override def workerClass: Class[SyntethicSampleSourceWorker] = classOf[SyntethicSampleSourceWorker]
}
