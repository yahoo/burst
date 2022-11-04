/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore

import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.wave.metadata.model.domain.FabricDomain
import org.burstsys.fabric.wave.metadata.model.view.FabricView
import org.burstsys.vitals.logging.{VitalsLog, VitalsLogger}

package object test extends VitalsLogger {


  val domain = FabricDomain(10)
  val view = FabricView(
    domainKey = domain.domainKey,
    viewKey = 9,
    generationClock = 0,
    storeProperties = Map("burst.store.name" -> "mock")
  )
  val datasource = FabricDatasource(domain, view)

  trait SampleStoreSpecLog {

    VitalsLog.configureLogging("samplestore", true)

  }

}
