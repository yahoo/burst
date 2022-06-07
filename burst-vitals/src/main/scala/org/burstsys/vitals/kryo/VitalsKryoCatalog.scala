/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.kryo

import org.burstsys.vitals.errors.{VitalsHealingFault, VitalsHealingFaultContext}

import java.util.concurrent.atomic.AtomicInteger

/**
  * Kryo Serialized Class Register
  */
class VitalsKryoCatalog extends VitalsKryoCatalogProvider {

  val key: AtomicInteger = new AtomicInteger(commonCatalogStart)
  val kryoClasses: Array[VitalsKryoClassPair] =
    key synchronized {
      Array(

        // base types
        (key.getAndIncrement, classOf[Boolean]),
        (key.getAndIncrement, classOf[Byte]),
        (key.getAndIncrement, classOf[Short]),
        (key.getAndIncrement, classOf[Int]),
        (key.getAndIncrement, classOf[Long]),
        (key.getAndIncrement, classOf[Double]),
        (key.getAndIncrement, classOf[String]),

        // arrays
        (key.getAndIncrement, classOf[Array[Boolean]]),
        (key.getAndIncrement, classOf[Array[Byte]]),
        (key.getAndIncrement, classOf[Array[Short]]),
        (key.getAndIncrement, classOf[Array[Int]]),
        (key.getAndIncrement, classOf[Array[Long]]),
        (key.getAndIncrement, classOf[Array[Double]]),
        (key.getAndIncrement, classOf[Array[String]]),

        // arrays of arrays
        (key.getAndIncrement, classOf[Array[Array[Byte]]]),

        (key.getAndIncrement, Map().getClass),

        // Exceptions
        (key.getAndIncrement, classOf[VitalsHealingFaultContext])


      )
    }
}
