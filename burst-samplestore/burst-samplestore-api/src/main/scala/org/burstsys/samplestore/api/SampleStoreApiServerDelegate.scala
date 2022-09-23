/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api

import scala.concurrent.Future

/**
 * Delegate responsible for doing view generation in response to thrift calls
 */
trait SampleStoreApiServerDelegate extends Any {

  /**
   * generate a view
   *
   * @param guid the unique id of the generation
   * @param dataSource the datasource for the view
   * @return a future that will eventually resolve into the view's generation
   */
  def getViewGenerator(guid: String, dataSource: BurstSampleStoreDataSource): Future[SampleStoreGeneration]

}
