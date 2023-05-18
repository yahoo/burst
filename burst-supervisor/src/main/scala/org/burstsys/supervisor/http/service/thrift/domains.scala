/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.service.thrift

import org.burstsys.catalog.model.domain.CatalogDomain
import org.burstsys.gen.thrift.api.client.domain.BTDomain

import scala.jdk.CollectionConverters._

object domains {

  def toThriftDomain(d: CatalogDomain): BTDomain = {
    val domain = new BTDomain(d.udk.getOrElse("???"), d.moniker, d.domainProperties.asJava, d.labels.getOrElse(Map.empty).asJava)
    domain.setPk(d.pk)
    d.createTimestamp.foreach(domain.setCreateTimestamp)
    d.modifyTimestamp.foreach(domain.setModifyTimestamp)
    domain
  }

  def fromThriftDomain(d: BTDomain): CatalogDomain = CatalogDomain(
    d.pk,
    d.moniker,
    asMap(d.domainProperties),
    d.udk,
    asMap(d.labels)
  )
}
