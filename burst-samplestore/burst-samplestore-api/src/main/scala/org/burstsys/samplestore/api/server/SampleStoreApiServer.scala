/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api.server

import org.burstsys.api.BurstApiServer
import org.burstsys.samplestore.api.configuration._
import org.burstsys.samplestore.api.{SampleStoreApi, SampleStoreApiService}

import scala.concurrent.duration.Duration

/**
  * server side implementation of the sample store Thrift server (this would be on the remote sample source side)
  *
  * @param service
  */
private[samplestore] final case
class SampleStoreApiServer(service: SampleStoreApiService)
  extends BurstApiServer with SampleStoreApi with SampleStoreViewReactor {
}
