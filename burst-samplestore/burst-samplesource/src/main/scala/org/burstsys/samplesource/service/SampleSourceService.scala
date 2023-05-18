/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource.service

/**
 * implemented by each sample source to provide lifecycle and sample source specific operations
 */
trait SampleSourceService[C <: SampleSourceSupervisorService, W <: SampleSourceWorkerService] {

  /**
   * @return the id of this sample source
   */
  def name: String

  /**
   * @return the class responsible for computing view generations
   */
  def supervisorClass: Class[C]

  /**
   * @return the class responsible for feeding streams sent to cell workers
   */
  def workerClass: Class[W]

}
