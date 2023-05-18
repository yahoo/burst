/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.fabric.wave.metadata.model.{FabricDomainKey, FabricViewKey}

/**
 * necessary and sufficient to load any miniview
 */
trait AlloyDatasetSpec {

  /**
   * brio schema for the dataset
   * @return
   */
  def schema: BrioSchema

  /**
   * domain primary key
   * @return
   */
  def domainKey: FabricDomainKey

  /**
   * view primary key
   * @return
   */
  def viewKey: FabricViewKey

}

object AlloyDatasetSpec {

  def apply(schema: BrioSchema, domainKey: FabricDomainKey): AlloyDatasetSpec =
    AlloyDatasetSpecContext(schema, domainKey, domainKey: FabricViewKey)

}

private final case
class AlloyDatasetSpecContext(schema: BrioSchema, domainKey: FabricDomainKey, viewKey: FabricViewKey) extends AlloyDatasetSpec
