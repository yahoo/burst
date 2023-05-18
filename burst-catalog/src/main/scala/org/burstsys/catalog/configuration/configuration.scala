/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog

import java.util.concurrent.TimeUnit

import org.burstsys.vitals.configuration.burstCellSupervisorHostProperty
import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostName, VitalsHostPort, getPublicHostName}
import org.burstsys.vitals.properties._
import org.burstsys.vitals.time.VitalsMs

import scala.concurrent.duration._
import scala.language.postfixOps

package object configuration extends VitalsLogger with VitalsPropertyRegistry {

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Thrift API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  val burstCatalogApiHostProperty: VitalsPropertySpecification[VitalsHostAddress] = VitalsPropertySpecification[VitalsHostAddress](
    key = "burst.catalog.api.host",
    description = "host for catalog thrift api",
    default = Some(getPublicHostName)
  )

  private[catalog]
  val burstCatalogApiPortProperty: VitalsPropertySpecification[VitalsHostPort] = VitalsPropertySpecification[VitalsHostPort](
    key = "burst.catalog.api.port",
    description = "port for catalog thrift api",
    default = Some(37010)
  )

  val burstCatalogApiSslEnableProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.catalog.api.ssl.enable",
    description = "enable catalog thrift SSL",
    default = Some(false)
  )

  private[catalog]
  val burstCatalogApiTimeoutMsProperty: VitalsPropertySpecification[VitalsMs] = VitalsPropertySpecification[VitalsMs](
    key = "burst.catalog.api.timeout.ms",
    description = "timeout for catalog thrift api requests",
    default = Some((120 seconds).toMillis)
  )

  private[catalog]
  def burstCatalogApiTimeoutDuration: Duration = Duration(burstCatalogApiTimeoutMsProperty.get, TimeUnit.MILLISECONDS)

  private[catalog]
  val burstCatalogServerConnectionLifeMsProperty: VitalsPropertySpecification[VitalsMs] = VitalsPropertySpecification[VitalsMs](
    key = "burst.catalog.server.connect.life.ms",
    description = "catalog server connect lifetime",
    default = Some((5 minutes).toMillis)
  )

  private[catalog]
  def burstCatalogServerConnectionLifeDuration: Duration = Duration(burstCatalogServerConnectionLifeMsProperty.get, TimeUnit.MILLISECONDS)

  private[catalog]
  val burstCatalogServerConnectionIdleMsProperty: VitalsPropertySpecification[VitalsMs] = VitalsPropertySpecification[VitalsMs](
    key = "burst.catalog.server.connect.idle.ms",
    description = "catalog server connect idletime",
    default = Some((5 minutes).toMillis)
  )

  private[catalog]
  def burstCatalogServerConnectionIdleDuration: Duration = Duration(burstCatalogServerConnectionIdleMsProperty.get, TimeUnit.MILLISECONDS)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // RDBMS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  val burstCatalogGenerationStaleMsProperty: VitalsPropertySpecification[Long] = VitalsPropertySpecification[Long](
    key = "burst.catalog.generation.stale.ms",
    description = "ms before a generation is considered stale",
    default = Some((4 hours).toMillis)
  )

  val burstCatalogDbNameProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.catalog.db.name",
    description = "burst catalog sql db name",
    default = Some("burst_catalog")
  )

  val burstCatalogDbUserProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.catalog.db.user",
    description = "burst catalog sql db user",
    default = Some("burst")
  )

  val burstCatalogDbPasswordProperty: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.catalog.db.password",
    description = "burst catalog sql db password",
    sensitive = true,
    default = Some("burst")
  )

  val burstCatalogDbHostProperty: VitalsPropertySpecification[VitalsHostName] = VitalsPropertySpecification[VitalsHostName](
    key = "burst.catalog.db.host",
    description = "burst catalog sql db hostname",
    default = Some(burstCellSupervisorHostProperty.get)
  )

  val burstCatalogDbPortProperty: VitalsPropertySpecification[VitalsHostPort] = VitalsPropertySpecification[VitalsHostPort](
    key = "burst.catalog.db.port",
    description = "burst catalog sql db port",
    default = Some(3306)
  )

  val burstCatalogCannedImportStandaloneOnlyProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.catalog.canned.importStandAloneOnly",
    description = "burst catalog should load only canned catalog elements for standalone",
    default = None
  )

  private[catalog]
  val burstCatalogDbConnectionsProperty: VitalsPropertySpecification[Int] = VitalsPropertySpecification[Int](
    key = "burst.catalog.db.connections.max",
    description = "burst catalog sql db max connections",
    default = Some(500)
  )

}
