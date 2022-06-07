/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.mock

import java.util.concurrent.atomic.AtomicInteger

import org.burstsys.vitals.kryo.VitalsKryoCatalogProvider
import org.burstsys.vitals.kryo._

/**
  * Kryo Serialized Class Register
  */
class MockKryoCatalog extends VitalsKryoCatalogProvider {

  val key = new AtomicInteger(fabricMockCatalogStart)
  val kryoClasses: Array[VitalsKryoClassPair] =
    key synchronized {
      Array(

        /////////////////////////////////////////////////////////////////////////////////
        // space phases
        /////////////////////////////////////////////////////////////////////////////////

        (key.getAndIncrement, classOf[MockScanner]),
        (key.getAndIncrement, classOf[MockSliceContext])
      )
    }
}
