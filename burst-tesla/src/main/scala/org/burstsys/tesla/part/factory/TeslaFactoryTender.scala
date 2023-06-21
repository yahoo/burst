/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.part.factory

import org.burstsys.tesla
import org.burstsys.tesla.configuration.teslaPartsTtlInterval
import org.burstsys.tesla.part
import org.burstsys.tesla.part.TeslaPartPool
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.host
import org.burstsys.vitals.reporter.instrument.prettyByteSizeString

import scala.concurrent.Future

/**
 * background tender for tesla part memory management
 */
trait TeslaFactoryTender[TenderPart, TenderPartPool <: TeslaPartPool[TenderPart]] {

  self: TeslaPartFactory[TenderPart, TenderPartPool] =>

  /////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////

  final
  def startPartTender: Future[Unit] = {
    val builder: Option[StringBuilder] = if (part.log.isInfoEnabled) Some(new StringBuilder) else None
    TeslaRequestFuture {
      Thread.currentThread setName s"tesla-$partName-tender"
      while (true) {
        Thread sleep tesla.configuration.teslaPartsTenderInterval.toMillis
        if (builder.isDefined)
          builder.get.clear()

        /**
         * collect stats on all the pools, and then print them out
         * if there is anything in any of the pools then perhaps
         * we need to free stuff
         */
        val totalPoolMemorySize = summarizePoolStats(collectPoolStats, builder)

        if (totalPoolMemorySize != 0) {

          if (testForRoom(totalPoolMemorySize))
            freePartsToMakeRoom(builder)
          else
            freePartsNotBeingUsed(builder)

        }
        if (builder.isDefined) {
          builder.get ++= s"*****************************************\n"
          part.log info builder.get.result()
        }
      }
    }
  }

  /////////////////////////////////////////////////////////////
  // implementation
  /////////////////////////////////////////////////////////////

  private
  def testForRoom(currPoolSize: Long): Boolean = {
    lazy val tag = s"TeslaPartTender.testForRoom(currPoolSize=$currPoolSize (${prettyByteSizeString(currPoolSize)}))"
    val physMem = host.osTotalPhysMemory
    /**
     * we may need room in which case we get aggressive
     * otherwise we just free stuff that no one is using
     */
    val maxPoolSize = (physMem * poolSizeAsPercentOfDirectMemory).toLong
    val overSize = currPoolSize > maxPoolSize

    if (overSize)
      part.log info s"TESLA_PART_OVER_SIZE osTotalPhysMemory=$physMem (${
        prettyByteSizeString(physMem)
      }), maxPoolSize=$maxPoolSize (${
        prettyByteSizeString(maxPoolSize)
      }) $tag "
    overSize
  }

  /**
   * get rid of parts aggressively because we need room
   *
   * @param builder if defined then we will print out the stats
   */
  private
  def freePartsToMakeRoom(builder: Option[StringBuilder]): Unit = {
    var freedBytes: Long = 0L
    var freedParts: Long = 0L
    pools.filter(_ != null) foreach {
      pool =>
        pool synchronized {
          val freed = pool.freeAllUnusedParts
          freedParts += freed._1
          freedBytes += freed._2
        }
    }
    if (builder.isDefined)
      if (freedParts > 0)
        builder.get ++= f"\tfreed ${
          prettyByteSizeString(freedBytes)
        } byte(s) in $freedParts%,d part(s) to make room\n"
      else
        builder.get ++= f"\tno part(s) could be freed to make room\n"
  }

  /**
   * get rid of parts that are just lying around
   *
   * @param builder if defined then we will print out the stats
   */
  private[part]
  def freePartsNotBeingUsed(implicit builder: Option[StringBuilder]): Unit = {
    var freedBytes: Long = 0L
    var freedParts: Long = 0L
    pools.filter(_ != null) foreach {
      pool =>
        pool synchronized {
          if (pool.nanosSinceLastPartGrab() > teslaPartsTtlInterval.toNanos) {
            val freed = pool.freeAllUnusedParts
            freedParts += freed._1
            freedBytes += freed._2
          }
        }
    }
    if (builder.isDefined)
      if (freedParts > 0)
        builder.get ++= f"\tfreed ${
          prettyByteSizeString(freedBytes)
        } byte(s) in $freedParts%,d unused part(s)\n"
      else
        builder.get ++= f"\tno unused part(s) to be freed\n"
  }


}
