/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.kryo

import java.util.concurrent.atomic.AtomicInteger
import org.burstsys.samplestore.model.{SampleStoreLocusContext, SampleStoreSliceContext}
import org.burstsys.vitals.kryo._
import org.burstsys.vitals.kryo.VitalsKryoCatalogProvider

import scala.annotation.unused

/**
  * Kryo Serialized Class Register
  */
@unused
class CellSampleStoreKryoCatalog extends VitalsKryoCatalogProvider {

  val key = new AtomicInteger(fabricCellSampleStoreStart)
  val kryoClasses: Array[VitalsKryoClassPair] =
    key synchronized {
      Array(
        (key.getAndIncrement, classOf[SampleStoreSliceContext]),
        (key.getAndIncrement, classOf[SampleStoreLocusContext])
      )
    }
}
