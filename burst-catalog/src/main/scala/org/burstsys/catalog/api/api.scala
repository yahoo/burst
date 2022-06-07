/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog

import org.burstsys.api.BurstApi
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort}

import scala.concurrent.duration.Duration

package object api {

  /**
    * base catalog THRIFT API
    */
  trait CatalogApi extends VitalsService with BurstCatalogApiService.FutureIface with BurstApi  {

    def service: CatalogService

    final override def modality: VitalsService.VitalsServiceModality = service.modality

    final override def apiName: String = "catalog"

    final override def apiPort: VitalsHostPort = service.apiPort

    final override def apiHost: VitalsHostAddress = service.apiHost

    final override def enableSsl: Boolean = service.enableSsl

    final override def certPath: String = service.certPath

    final override def keyPath: String = service.keyPath

    final override def caPath: String = service.caPath

    final override def enableCompositeTrust: Boolean = service.enableCompositeTrust

    final override def maxConnectionIdleTime: Duration = service.maxConnectionIdleTime

    final override def maxConnectionLifeTime: Duration = service.maxConnectionLifeTime
  }

}
