/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import org.burstsys.fabric.metadata.model
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.metadata.model.domain.FabricDomain
import org.burstsys.fabric.metadata.model.datasource
import org.burstsys.fabric.metadata.model.view.FabricView
import org.burstsys.vitals.logging.{VitalsLog, VitalsLogger}

package object test extends VitalsLogger {


  val domain: FabricDomain = model.domain.FabricDomain(5)

  val view: FabricView = model.view.FabricView(
    domainKey = domain.domainKey,
    viewKey = 6,
    storeProperties = Map("burst.store.name" -> "mock")
  )

  val datasource: FabricDatasource = FabricDatasource(domain = domain, view = view)

  trait FabricSpecLog {

    VitalsLog.configureLogging("fabric", consoleOnly = true)

  }

}
