/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.metadata.model.over

import org.burstsys.fabric.wave.metadata.model.{FabricDomainKey, FabricViewKey}
import org.burstsys.vitals.json.VitalsJsonRepresentable
import org.burstsys.vitals.time.{VitalsLocale, defaultLocale}

/**
 * all the metadata required for a scan
 */
trait FabricOver extends VitalsJsonRepresentable[FabricOver] {

  /**
   * the primary key for the domain
   *
   * @return
   */
  def domainKey: FabricDomainKey

  /**
   * the primary key for the domain
   *
   * @param l
   */
  def domainKey_=(l: FabricDomainKey): Unit

  /**
   * the primary key for the view
   *
   * @return
   */
  def viewKey: FabricViewKey

  /**
   * the primary key for the view
   *
   * @param l
   */
  def viewKey_=(l: FabricViewKey): Unit

  /**
   * the locale for this scan
   *
   * @return
   */
  def locale: VitalsLocale

  /**
   * the locale for this scan
   *
   * @param l
   */
  def locale_=(l: VitalsLocale): Unit

  final override def toString: String = s"Over(domainKey=$domainKey, viewKey=$viewKey, locale=$locale)"

}

object FabricOver {

  def apply(
             domain: FabricDomainKey = 0L,
             view: FabricViewKey = 0L,
             locale: VitalsLocale = defaultLocale
           ): FabricOver =
    FabricOverContext(domain, view, locale)

}

private final case
class FabricOverContext(
                         var domainKey: FabricDomainKey,
                         var viewKey: FabricViewKey,
                         var locale: VitalsLocale
                       ) extends FabricOver {
  override def toJson: FabricOver = JsonFabricOver(domainKey, viewKey, locale)
}
