/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore

import org.burstsys.fabric.data.model.store.FabricStoreProvider
import org.burstsys.samplestore.api.SampleStoreGeneration
import org.burstsys.samplestore.worker.SampleStoreWorker
import org.burstsys.vitals.logging._
import org.burstsys.samplestore
import org.burstsys.vitals

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock

/**
  * spark independent part of sample store master side processing
  */
package object master extends VitalsLogger {

  val sampleStoreServerLock = new ReentrantLock

  private[this] val monkeyHashCounter = new AtomicLong

  final val monkeyHashMode = false

  /**
   * for testing only - every once in a while if [[monkeyHashMode]] is true, slip
   * in a different generation hash
   * @param generator
   * @return
   */
  private[master] def doMonkeyHash(generator: SampleStoreGeneration): String = {
    if (monkeyHashMode && monkeyHashCounter.incrementAndGet() % 10 == 0) {
      val hash = vitals.uid.newBurstUid
      log warn s"MONKEY_HASH changing ${generator.generationHash} to $hash"
      hash
    } else generator.generationHash
  }

  /**
   * SampleStore store plugin provider
   */
  final case class SampleStoreStoreProvider() extends FabricStoreProvider[SampleStoreMaster, SampleStoreWorker] {

    val storeName: String = samplestore.SampleStoreName

    val masterClass: Class[SampleStoreMaster] = classOf[SampleStoreMaster]

    val workerClass: Class[SampleStoreWorker] = classOf[SampleStoreWorker]

  }

}
