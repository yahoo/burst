/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore.source

import org.burstsys.samplesource.service.SampleSourceService
import scala.annotation.unused

/**
 * A sample source that returns generated data
 */
@unused // found by reflection
case class SyntheticSampleSourceService()
  extends SampleSourceService[SyntheticSampleSourceSupervisor, SyntheticSampleSourceWorker] {

  /**
   * @return the id of this sample source
   */
  override def name: String = SyntheticSampleSourceName

  /**
   * @return the class responsible for computing view generations
   */
  override def supervisorClass: Class[SyntheticSampleSourceSupervisor] = classOf[SyntheticSampleSourceSupervisor]

  /**
   * @return the class responsible for feeding streams sent to cell workers
   */
  override def workerClass: Class[SyntheticSampleSourceWorker] = classOf[SyntheticSampleSourceWorker]
}
