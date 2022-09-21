/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api

import scala.concurrent.Future

/**
 * event listener for incoming thrift API events
 */
trait SampleStoreApiListener extends Any {

  /**
   * generate a view
   *
   * @param guid
   * @param dataSource
   * @return
   */
  def getViewGenerator(guid: String, dataSource: BurstSampleStoreDataSource): Future[SampleStoreGeneration]

}
