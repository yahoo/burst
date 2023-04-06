/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.service.burnin

import org.burstsys.fabric.wave.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.wave.metadata.model.domain.FabricDomain
import org.burstsys.fabric.wave.metadata.model.view.FabricView

case class BurnInRunDataset(
                             domain: FabricDomain,
                             view: FabricView,
                             loadQuery: String,
                             queries: Array[String],
                             queriesBeforeReload: Option[Int],
                             var ready: Boolean = false,
                           ) {

  private var _queryCount = 0

  def generationKey: FabricGenerationKey = FabricGenerationKey(view.domainKey, view.viewKey)

  def willRunQuery(): Unit = _queryCount += 1

  def shouldFlush: Boolean = queriesBeforeReload.exists(_queryCount > _)

  def flushed(): Unit = _queryCount = 0
}
