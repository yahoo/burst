/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.kryo

import java.util.concurrent.atomic.AtomicInteger
import org.burstsys.alloy.store.exceptional.{ExceptionalSliceContext, StoreFailureMode}
import org.burstsys.alloy.store.mini.MiniSliceContext
import org.burstsys.vitals.kryo._
import org.burstsys.vitals.kryo.VitalsKryoCatalogProvider

/**
  * Kryo Serialized Class Register
  */
class UnitKryoCatalog extends VitalsKryoCatalogProvider {

  val key = new AtomicInteger(unitCatalogStart)
  val kryoClasses: Array[VitalsKryoClassPair] =
    key synchronized {
      Array(
        (key.getAndIncrement, classOf[MiniSliceContext]),
        (key.getAndIncrement, classOf[ExceptionalSliceContext]),
        (key.incrementAndGet, classOf[StoreFailureMode])
      )
    }

}
