/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla

import java.util.concurrent.atomic.AtomicInteger

import org.burstsys.vitals.logging._

package object pool  {

  final private val poolIdGenerator = new AtomicInteger

  def newPoolId: Int = poolIdGenerator.getAndIncrement()

  type TeslaPoolId = Int

  /**
    * this is a __universal trait__ so we can mix it into value classes
    */
  trait TeslaPooledResource extends Any {

    /**
      * the identity of the home pool for this resource
      *
      * @return
      */
    def poolId: TeslaPoolId

  }

}
