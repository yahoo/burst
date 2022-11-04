/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.alloy.store

import org.burstsys.fabric.wave.metadata.model.domain.FabricDomain
import org.burstsys.fabric.wave.metadata.model.view.FabricView
import org.burstsys.fabric.wave.metadata.model.{FabricDomainKey, FabricViewKey}

trait AlloyView {

  def domain: FabricDomain

  def view: FabricView

  def domainKey: FabricDomainKey

  def viewKey: FabricViewKey
}
