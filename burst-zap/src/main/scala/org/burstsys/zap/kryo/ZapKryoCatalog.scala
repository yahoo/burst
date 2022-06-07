/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.kryo

import java.util.concurrent.atomic.AtomicInteger

import org.burstsys.vitals.kryo.{VitalsKryoCatalogProvider, _}
import org.burstsys.zap.cube.{ZapCubeContext, ZapCubeBuilder}
import org.burstsys.zap.route.ZapRouteBuilder
import org.burstsys.felt.model.collectors.route.decl.graph.{FeltRouteEdge, FeltRouteTransition}

/**
 *
 * Kryo Serialized Class Register
 */
class ZapKryoCatalog extends VitalsKryoCatalogProvider {

  val key = new AtomicInteger(zapCatalogStart)

  val kryoClasses: Array[VitalsKryoClassPair] = {
    key synchronized {
      Array(

        (key.getAndIncrement, classOf[ZapCubeContext]),
        (key.getAndIncrement, classOf[ZapCubeBuilder]),
        (key.getAndIncrement, classOf[ZapRouteBuilder])

      )

    }
  }

}
