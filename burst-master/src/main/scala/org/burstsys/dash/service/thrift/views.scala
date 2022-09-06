/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.service.thrift

import org.burstsys.catalog.model.view.CatalogView
import org.burstsys.gen.thrift.api.client.domain.BTDomain
import org.burstsys.gen.thrift.api.client.view.BTView

import scala.jdk.CollectionConverters._

object views {

  def toThriftView(domainUdk: String, v: CatalogView): BTView = {
    val view = new BTView(v.udk.getOrElse("???"),
      v.moniker,
      domainUdk,
      v.storeProperties.asJava,
      v.viewMotif,
      v.viewProperties.asJava,
      v.labels.getOrElse(Map.empty).asJava,
      v.schemaName)
    view.setPk(v.pk)
    view.setGenerationClock(v.generationClock)
    v.createTimestamp.foreach(view.setCreateTimestamp)
    v.modifyTimestamp.foreach(view.setModifyTimestamp)
    v.accessTimestamp.foreach(view.setAccessTimestamp)
    view
  }

  def fromThriftView(dPk: Long, v: BTView): CatalogView = CatalogView(
    v.pk,
    v.udk,
    v.moniker,
    dPk,
    v.generationClock,
    asMap(v.storeProperties),
    v.viewMotif,
    asMap(v.viewProperties),
    asMap(v.labels),
    v.schemaName
  )

  def fromThriftDomain(dPk: Long, d: BTDomain): Array[CatalogView] = {
    d.views.asScala.map(fromThriftView(dPk, _)).toArray
  }
}
