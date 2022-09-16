/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.kryo

import org.burstsys.vitals.kryo.{VitalsKryoCatalogProvider, _}
import org.burstsys.zap.cube2.ZapCube2Builder
import org.burstsys.zap.route.ZapRouteBuilder

import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * Kryo Serialized Class Register
 */
class ZapKryoCatalog extends VitalsKryoCatalogProvider {

  val key = new AtomicInteger(zapCatalogStart)

  val kryoClasses: Array[VitalsKryoClassPair] = {
    key synchronized {
      Array(
        (key.getAndIncrement, classOf[ZapCube2Builder]),
        (key.getAndIncrement, classOf[ZapRouteBuilder])
      )
    }
  }

}
