/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.runtime

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize

/**
 * Factory for runtime alloc/free of collectors
 */
trait FeltCollectorFactory[B <: FeltCollectorBuilder, C <: FeltCollector] extends Any {

  /**
   * allocate a collector
   *
   * @param builder
   * @return
   */
  def grabCollector(builder: B, desiredSize: TeslaMemorySize): C

  /**
   * release a collector
   *
   * @param collector
   */
  def releaseCollector(collector: C): Unit

}
