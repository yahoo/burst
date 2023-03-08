/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore

import org.burstsys.fabric.wave.data.model.store.FabricStoreProvider
import org.burstsys.samplestore.api.SampleStoreGeneration
import org.burstsys.samplestore.worker.SampleStoreWorker
import org.burstsys.{samplestore, vitals}
import org.burstsys.vitals.logging._

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import scala.annotation.unused

/**
  * Platform independent part of sample store supervisor side processing
  */
package object supervisor extends VitalsLogger {

  val sampleStoreServerLock = new ReentrantLock

  private[this] val monkeyHashCounter = new AtomicLong

  final val monkeyHashMode = false

  /**
   * for testing only - every once in a while if [[monkeyHashMode]] is true, slip
   * in a different generation hash
   * @param generator
   * @return
   */
  private[supervisor] def doMonkeyHash(generator: SampleStoreGeneration): String = {
    if (monkeyHashMode && monkeyHashCounter.incrementAndGet() % 10 == 0) {
      val hash = vitals.uid.newBurstUid
      log warn s"MONKEY_HASH changing ${generator.generationHash} to $hash"
      hash
    } else generator.generationHash
  }

  /**
   * SampleStore store plugin provider
   * Found by reflection in the Fabric Store.
   */
  @unused
  final case class SampleStoreStoreProvider() extends FabricStoreProvider[SampleStoreSupervisor, SampleStoreWorker] {

    val storeName: String = samplestore.SampleStoreName

    val supervisorClass: Class[SampleStoreSupervisor] = classOf[SampleStoreSupervisor]

    val workerClass: Class[SampleStoreWorker] = classOf[SampleStoreWorker]

  }

}
