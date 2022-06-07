/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.test

import org.burstsys.tesla
import org.burstsys.tesla.block.{factory, _}
import org.burstsys.tesla.test.support.TeslaSpecLog
import org.burstsys.tesla.thread.worker.{TeslaWorkerCoupler, TeslaWorkerFuture}
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent._
import scala.concurrent.duration.Duration


class TeslaSpec extends AnyFlatSpec with Matchers with TeslaSpecLog {

  "burst memory block" should "initialize" in {
    //    memory.block.bindBlockPoolToThread()
    val ptr = tesla.offheap.allocateMemory(500)
    val block = TeslaBlockAnyVal(ptr).initialize(500, 55.toByte)

    //    block.magic should equal(memory.block.MAGIC)
    block.poolId should equal(55.toByte)

    block.dataSize should equal(488)

    block.dataStart should equal(ptr + SizeofBlockHeader)

  }

  "burst memory pool" should "allocate block twice" in {
    TeslaWorkerCoupler {

      //    memory.block.bindBlockPoolToThread()
      var blk = factory.grabBlock(300)

      //    blk.dataSize should equal(offheap.pageSize - blk.headerSize)

      factory.releaseBlock(blk)

      blk = factory.grabBlock(300)

      blk.dataSize should be > 300

      factory.releaseBlock(blk)
    }

  }

  "burst memory pool" should "allocate in two pools" in {
    TeslaWorkerCoupler {
      //    memory.factory.bindBlockPoolToThread()
      var blk = factory.grabBlock(300)

      blk.dataSize should be > 300

      factory.releaseBlock(blk)

      blk = factory.grabBlock(5000)

      blk.dataSize should be > 5000

      factory.releaseBlock(blk)
    }

  }

  "burst memory pool" should "allocate cross thread" in {
    TeslaWorkerCoupler {
      //    memory.factory.bindBlockPoolToThread()
      val blk1 = factory.grabBlock(300)
      var blk2: TeslaBlock = factory.TeslaNullMemoryBlock


      log info s"thread1 is ${Thread.currentThread.getName}"

      val f = TeslaWorkerFuture {
        log info s"thread2 is ${Thread.currentThread.getName}"
        //      memory.factory.bindBlockPoolToThread()
        blk2 = factory.grabBlock(600)
        factory.releaseBlock(blk1)
      }

      Await.result(f, Duration.Inf)

      factory.releaseBlock(blk2)
    }

  }

}
