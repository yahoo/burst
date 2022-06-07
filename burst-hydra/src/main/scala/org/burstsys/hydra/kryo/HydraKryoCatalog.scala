/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.kryo

import java.util.concurrent.atomic.AtomicInteger

import org.burstsys.hydra.runtime.{HydraGather, HydraScanner}
import org.burstsys.vitals.kryo._
import org.burstsys.vitals.kryo.VitalsKryoCatalogProvider

/**
  * Kryo Serialized Class Register
  */
class HydraKryoCatalog extends VitalsKryoCatalogProvider {

  val key = new AtomicInteger(hydraCatalogStart)
  val kryoClasses: Array[VitalsKryoClassPair] =
    key synchronized {
      Array(
        (key.getAndIncrement, classOf[HydraScanner]),
        (key.getAndIncrement, classOf[HydraGather])
      )
    }
}
