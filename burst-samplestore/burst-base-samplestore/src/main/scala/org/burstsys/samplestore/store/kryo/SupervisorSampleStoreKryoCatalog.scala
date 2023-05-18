/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store.kryo

import org.burstsys.samplesource.service.MetadataParameters
import org.burstsys.vitals.kryo._

import java.util.concurrent.atomic.AtomicInteger
import scala.annotation.unused

/**
 * Kryo Serialized Class Register
 */
@unused
class SupervisorSampleStoreKryoCatalog extends VitalsKryoCatalogProvider {

  val key = new AtomicInteger(fabricSupervisorSampleStoreStart)
  val kryoClasses: Array[VitalsKryoClassPair] =
    key.synchronized {
      Array(
        /////////////////////////////////////////////////////////////////////////////////
        // metdata
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement, classOf[Serializable]),
        (key.getAndIncrement, classOf[MetadataParameters]),
        (key.getAndIncrement, classOf[scala.collection.Map[String, String]]),
        (key.getAndIncrement, classOf[scala.collection.Map[String, Serializable]])
      )
    }
}
