/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.model.generation

import org.burstsys.fabric.metadata.model.{FabricDomainKey, FabricGenerationClock, FabricViewKey}

/**
 * establish a well known identity for a specific ''generation'' which is a dataset ''snapshot in time''
 * based on a [[org.burstsys.fabric.metadata.model.view.FabricView]])
 */
trait FabricGenerationIdentity extends Equals {

  /**
   * the primary key for the domain
   */
  def domainKey: FabricDomainKey

  /**
   * the primary key for the view
   */
  def viewKey: FabricViewKey

  /**
   * the generation clock
   */
  def generationClock: FabricGenerationClock

  ///////////////////////////////////////////////////////////////////////////
  // Identity/Equality
  ///////////////////////////////////////////////////////////////////////////

  final override
  def equals(obj: scala.Any): Boolean = obj match {
    case that: FabricGenerationIdentity =>
      this.domainKey == that.domainKey && this.viewKey == that.viewKey && this.generationClock == that.generationClock
    case _ => false
  }

  final override
  def hashCode(): Int = {
    var hash = 7L
    hash = 31 * hash + domainKey.hashCode
    hash = 31 * hash + viewKey.hashCode
    hash = 31 * hash + generationClock.hashCode
    hash.toInt
  }

  final override
  def canEqual(that: Any): Boolean = that.isInstanceOf[FabricGenerationIdentity]

}
