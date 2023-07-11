/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.flex

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.part.TeslaPartBuilder
import org.burstsys.tesla.pool.TeslaPoolId


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
  extends Any with TeslaFlexProxy[Builder, Collector] with TeslaFlexCollector[Builder, Collector] {

  @inline
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

  //
  @inline
  def runWithRetry[R](body: => R): R = {
    assert(!internalCollector.itemLimited)
    var r: R = body
    var runCount = 1
    while (internalCollector.itemLimited && runCount < 11) {
      coupler.upsize(this.index, internalCollector.itemCount, internalCollector.builder)
      r = body
      runCount += 1
    }
    if (runCount > 10) {
      log warn s"Too many upsize attempts"
    }
    r
  }

  override def itemCount: TeslaPoolId = internalCollector.itemCount

  override def size(): TeslaMemorySize = internalCollector.size()

  override def itemLimited: Boolean = internalCollector.itemLimited
}
