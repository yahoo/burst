/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.test

import org.burstsys.vitals.kryo._

import java.util.concurrent.atomic.AtomicInteger
import scala.annotation.unused

@unused
class TestStoreKryoCatalog extends VitalsKryoCatalogProvider {

  val key = new AtomicInteger(lastStart)
  val kryoClasses: Array[VitalsKryoClassPair] =
    key.synchronized {
      Array(
        (key.getAndIncrement, classOf[java.util.HashMap[java.lang.Long, java.lang.Long]])
      )
    }
}
