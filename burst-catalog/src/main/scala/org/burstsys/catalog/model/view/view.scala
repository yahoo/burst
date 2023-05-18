/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.model

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.burstsys.catalog.api.BurstCatalogApiView
import org.burstsys.catalog.cannedDataLabel
import org.burstsys.catalog.persist.ScopedUdkCatalogEntity
import org.burstsys.fabric
import org.burstsys.fabric.wave.data.model.generation.FabricGenerationIdentity
import org.burstsys.fabric.wave.data.model.store.FabricStoreNameProperty
import org.burstsys.fabric.wave.metadata.model.FabricDomainKey
import org.burstsys.fabric.wave.metadata.model.FabricGenerationClock
import org.burstsys.fabric.wave.metadata.model.FabricViewKey
import org.burstsys.fabric.wave.metadata.model.view.FabricView
import org.burstsys.relate.RelateEntity
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.json.VitalsJsonSanatizers._
import org.burstsys.vitals.properties.VitalsLabelsMap
import org.burstsys.vitals.properties.VitalsPropertyKey
import org.burstsys.vitals.properties.VitalsPropertyMap

import scala.collection.immutable.Map
import scala.language.implicitConversions

package object view {

  type CatalogView = BurstCatalogApiView.Proxy with ScopedUdkCatalogEntity with FabricView

  object CatalogView {
    def apply(pk: FabricDomainKey, moniker: String, domainFk: FabricDomainKey, schemaName: String, generationClock: FabricDomainKey = 0, storeProperties: Map[String, String] = Map(), viewMotif: String = "", viewProperties: Map[String, String] = Map(), labels: Option[Map[String, String]] = None, udk: Option[String] = None): BurstCatalogApiView =
      BurstCatalogApiView(pk, moniker, domainFk, generationClock, storeProperties,
        viewMotif, viewProperties, labels, schemaName, udk = udk
      )

    def apply(
               pk: Long, udk: String, moniker: String, domainFk: Long, generationClock: Long, storeProperties: Map[String, String],
               viewMotif: String, viewProperties: Map[String, String], labels: Map[String, String], schemaName: String
             ): CatalogView =
      viewApiToProxy(BurstCatalogApiView(
        pk, moniker, domainFk, generationClock, storeProperties,
        viewMotif, viewProperties, Some(labels), schemaName, udk = Some(udk)
      ))

    def apply(v: FabricView, moniker: String, udk: Option[String], labels: Option[Map[String, String]]): CatalogView =
      BurstCatalogApiView(
        v.viewKey, moniker, v.domainKey, v.generationClock, v.storeProperties,
        v.viewMotif, viewProperties = v.viewProperties, labels, v.schemaName, udk = udk
      )
    /**
     * Filter out the properties owned and updated internally by Burst.
     * They are not interesting for the purposes of determining if a view needs to be saved
     * as user modification to these properties is neither expected nor supported.
     *
     * @param props the viewProperties of a catalog view
     * @return the properties of a view that are not owned by Burst
     */
    def withoutLoadProps(props: VitalsPropertyMap): VitalsPropertyMap =
      props.view.filterKeys(key => !loadProps.contains(key)).toMap

    // TODO remove this, it's only used in canned data
    final val ViewLoadTimeoutMsProperty: VitalsPropertyKey = "burst.load.timeout.ms"

    /**
     * The set of properties that should be ignored when deciding if a view's generation clock needs to be updated
     */
    private val loadProps: Set[String] = Set(
      ViewLoadTimeoutMsProperty,
      fabric.wave.metadata.ViewNextLoadStaleMsProperty,
      fabric.wave.metadata.ViewLastLoadStaleMsProperty,
      fabric.wave.metadata.ViewEarliestLoadAtProperty,
      fabric.wave.metadata.ViewLastColdLoadAtProperty
    )
  }

  final case
  class CatalogSqlView(_underlying_BurstCatalogApiView: BurstCatalogApiView)
    extends BurstCatalogApiView.Proxy with ScopedUdkCatalogEntity with FabricView {

    override def domainKey: FabricDomainKey = _underlying_BurstCatalogApiView.domainFk

    override def viewKey: FabricViewKey = pk

    override def init(domainKey: FabricDomainKey, viewKey: FabricViewKey, generationClock: FabricGenerationClock): FabricView = ???

    override def init(gm: FabricGenerationIdentity): FabricView = ???

    /**
     * Determines if the view needs to be saved to the DB. Only called during upsert.
     *
     * @param storedEntity the version of this entity that exists in the database
     * @return true if this version of the entity should be written tot the database
     */
    override def shouldUpdate(storedEntity: RelateEntity): Boolean = {
      storedEntity match {
        case fromDb: CatalogView =>
          val domainDiffers = domainKey > 0 && domainKey != fromDb.domainKey
          val clockDiffers = generationClock > 0 && generationClock != fromDb.generationClock
          val udkDiffers = udk.isDefined && udk != fromDb.udk
          val monikerDiffers = moniker != "" && moniker != fromDb.moniker
          val schemDiffers = schemaName != "" && schemaName != fromDb.schemaName
          val motifDiffers = viewMotif != "" && viewMotif != fromDb.viewMotif
          val labelsDiffer = labels.isDefined && labels != fromDb.labels
          val storePropsDiffer = storeProperties.nonEmpty && storeProperties != fromDb.storeProperties
          val viewPropsDiffer = viewProperties.nonEmpty && CatalogView.withoutLoadProps(viewProperties) != CatalogView.withoutLoadProps(fromDb.viewProperties)

          domainDiffers || clockDiffers || udkDiffers || monikerDiffers || schemDiffers || motifDiffers ||
            labelsDiffer || storePropsDiffer || viewPropsDiffer
        case _ => throw VitalsException(s"Something has gone horribly wrong. Expected a ${this.getClass} but found a ${storedEntity.getClass}")
      }
    }

    override def toJson: FabricView = ???
  }

  implicit def viewApiToProxy(c: BurstCatalogApiView): CatalogView =
    CatalogSqlView(c)

  implicit def viewProxyToApi(c: CatalogView): BurstCatalogApiView =
    BurstCatalogApiView(c.pk, c.moniker, c.domainFk, c.generationClock, c.storeProperties, c.viewMotif, c.viewProperties, c.labels, c.schemaName, c.createTimestamp, c.modifyTimestamp, c.accessTimestamp, c.udk)

  final case
  class CatalogJsonView(pk: Long,
                        @JsonSerialize(using = classOf[Values]) moniker: String,
                        @JsonSerialize(keyUsing = classOf[Keys], contentUsing = classOf[Values]) labels: Map[String, String],
                        domainFk: Long,
                        generationClock: Long,
                        @JsonSerialize(using = classOf[Values]) schemaName: String,
                        @JsonSerialize(keyUsing = classOf[Keys], contentUsing = classOf[Values]) storeProperties: Map[String, String],
                        viewMotif: String,
                        @JsonSerialize(keyUsing = classOf[Keys], contentUsing = classOf[Values]) viewProperties: Map[String, String],
                        @JsonSerialize(using = classOf[Values]) udk: String)

  implicit def viewProxyToJson(c: CatalogView): CatalogJsonView =
    CatalogJsonView(c.pk, c.moniker, c.labels.map(_.toMap).getOrElse(Map.empty), c.domainFk,
      c.generationClock, c.schemaName, c.storeProperties.toMap, c.viewMotif, c.viewProperties.toMap, c.udk.orNull)

  implicit def viewJsonToProxy(c: CatalogJsonView): CatalogView =
    BurstCatalogApiView(c.pk, c.moniker, c.domainFk, c.generationClock, c.storeProperties, c.viewMotif, c.viewProperties,
      if (c.labels.isEmpty) None else Some(c.labels), c.schemaName, udk = Option(c.udk))

  private val cannedLabels = Map(cannedDataLabel -> "true")

  final case
  class CatalogCannedView(moniker: String, domainMoniker: String, generationClock: Long = 0, storeProperties: VitalsPropertyMap = Map.empty,
                          viewMotif: String, viewProperties: VitalsPropertyMap = Map.empty, var labels: Option[VitalsLabelsMap] = None,
                          schemaName: String, udk: Option[String] = None) {
    labels = labels.orElse(Some(cannedLabels))
  }

}
