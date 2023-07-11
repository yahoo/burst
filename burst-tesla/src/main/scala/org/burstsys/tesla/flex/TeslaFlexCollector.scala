/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.flex

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.block.TeslaBlockPart
import org.burstsys.tesla.part.TeslaPartBuilder
import org.burstsys.tesla.pool.TeslaPoolId

/**
  * minimal set of behaviors a collector needs to be a flex collector
  * <p/>'''NOTE 1:''' the assumption here is that a collector '''does not''' create JVM objects i.e. it
  * is a value class wrapping a memory ptr.
  * <p/>'''NOTE 2:''' the underlying collector '''must''' be able to flag an undersize condition and
  * then ''recover'' correctly by having its contents imported into a larger collector and error conditions reset.
  *
  * @tparam Builder    the custom config object passed during instantiations
  * @tparam Collector the underlying collector object
  */
trait TeslaFlexCollector[Builder <: TeslaPartBuilder, Collector <: TeslaFlexCollector[Builder, Collector]]
  extends Any with TeslaBlockPart {

  /**
    * the current memory size of this collector
    *
    * @return
    */
  def currentMemorySize: TeslaMemorySize

  /**
    * copy over data from a presumably too small collector to this bigger upsized collector
    *
    * @param sourceCollector
    */
  def importCollector(sourceCollector: Collector, sourceItems: Int, builder: Builder): Unit

  def initialize(pId: TeslaPoolId, builder: Builder = defaultBuilder): Unit

  def reset(builder: Builder = defaultBuilder): Unit

  def defaultBuilder: Builder

  def builder: Builder

  def itemCount: Int

  def size(): TeslaMemorySize

  def itemLimited: Boolean
}
