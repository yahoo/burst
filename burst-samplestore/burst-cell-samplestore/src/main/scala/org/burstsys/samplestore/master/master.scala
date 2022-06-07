/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore

import java.util.concurrent.locks.ReentrantLock

import org.burstsys.fabric.data.model.store.FabricStoreProvider
import org.burstsys.{samplestore, vitals}
import org.burstsys.samplestore.api.SampleStoreGenerator
import org.burstsys.samplestore.worker.SampleStoreWorker
import org.burstsys.vitals.logging._

/**
  * spark independent part of sample store master side processing
  */
package object master extends VitalsLogger {

  val sampleStoreServerLock = new ReentrantLock

  private[this] var monkeyHashCounter = 0

  final val monkeyHashMode = false

  /**
   * for testing only - every once in a while if #monkeyHashMode is true, slip
   * in a different generation hash
   * @param generator
   * @return
   */
  private[master] def doMonkeyHash(generator: SampleStoreGenerator): String = {
    monkeyHashCounter += 1
    if (monkeyHashMode && monkeyHashCounter % 10 == 0) {
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
