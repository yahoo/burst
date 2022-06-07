/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.kryo

import java.util.concurrent.atomic.AtomicInteger

import org.burstsys.brio.model.schema.types._
import org.burstsys.brio.types.BrioPath
import org.burstsys.vitals.kryo.VitalsKryoCatalogProvider
import org.burstsys.vitals.kryo._

/**
  * Kryo Serialized Class Register
  */
class BrioKryoCatalog extends VitalsKryoCatalogProvider {

  val key = new AtomicInteger(brioCatalogStart)
  val kryoClasses: Array[VitalsKryoClassPair] =
    key synchronized {
      Array(

        // paths
        (key.getAndIncrement, classOf[BrioPath]),
        (key.getAndIncrement, BrioReferenceVectorRelation.getClass),
        (key.getAndIncrement, BrioReferenceScalarRelation.getClass),
        (key.getAndIncrement, BrioValueVectorRelation.getClass),
        (key.getAndIncrement, BrioValueMapRelation.getClass),
        (key.getAndIncrement, BrioValueScalarRelation.getClass)

      )
    }
}
