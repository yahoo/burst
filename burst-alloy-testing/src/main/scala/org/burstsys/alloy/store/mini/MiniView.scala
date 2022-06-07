/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.store.mini

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.{BrioPressInstance, BrioPressSource}
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.fabric.metadata._
import org.burstsys.fabric.metadata.model.domain.FabricDomain
import org.burstsys.fabric.metadata.model.view.FabricView
import org.burstsys.fabric.metadata.model.{FabricDomainKey, FabricViewKey}

trait MiniView {

  def domain:FabricDomain

  def view:FabricView

  def domainKey: FabricDomainKey

  def viewKey: FabricViewKey

  def items: Array[BrioPressInstance]

  def presser(root: BrioPressInstance): BrioPressSource

  def rootVersion: BrioVersionKey

  def schema: BrioSchema

}
