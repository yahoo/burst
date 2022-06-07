/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.flex

import org.burstsys.tesla.part.TeslaPartBuilder


/**
  * a proxy to a collector that allows it to ''grow''.
  * <p/>'''NOTE 1:''' the assumption here is that a proxy does not create JVM objects i.e. it
  * is a value class wrapping a slot index.
 *
  * @tparam Builder the custom config object passed during instantiations
  * @tparam Collector the underlying collector object
  */
trait TeslaFlexProxy[Builder <: TeslaPartBuilder, Collector <: TeslaFlexCollector[Builder, Collector]] extends Any {

  /**
    * slot index for underlying off heap dictionary
    *
    * @return
    */
  def index: TeslaFlexSlotIndex

  /**
    * internal fixed size collector
    *
    * @return
    */
  def internalCollector: Collector

}

/**
  * base trait for the [[TeslaFlexProxy]] implementation
  *
  */
trait TeslaFlexProxyContext[Builder <: TeslaPartBuilder, Collector <: TeslaFlexCollector[Builder, Collector], Proxy <: TeslaFlexProxy[Builder, Collector]]
  extends Any with TeslaFlexProxy[Builder, Collector] {

  @inline private[this]
  def _internalCollector: Collector = coupler.lookupInternalCollector(this.index)

  @inline final override
  def internalCollector: Collector = _internalCollector

  /**
    * the singleton coupler
    *
    * @return
    */
  def coupler: TeslaFlexCoupler[Builder, Collector, Proxy]

  override
  def index: TeslaFlexSlotIndex

}
