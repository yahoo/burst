/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.metadata.model.domain.FabricDomain
import org.burstsys.fabric.metadata.model.view
import org.burstsys.fabric.metadata.model.view.FabricView
import org.burstsys.vitals.logging.{VitalsLog, VitalsLogger}

import scala.language.postfixOps

package object test extends VitalsLogger {
  lazy val quoSchema = BrioSchema("quo")

  VitalsLog.configureLogging("hydra", true)

}
