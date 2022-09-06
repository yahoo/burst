/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.part.factory

import org.burstsys.tesla.part.{TeslaPartPool, debugTending}
import org.burstsys.vitals.instrument.{prettyByteSizeString, prettySizeString}

import scala.collection.mutable

/**
 * stats reporting for tesla parts
 */
trait TeslaFactoryPrinter[Part, PartPool <: TeslaPartPool[Part]] {

  self: TeslaPartFactory[Part, PartPool] =>

  /**
   *
   * @return
   */
  def printSummaryReport: String = {
    implicit val stringBuilder: StringBuilder = new StringBuilder
    freePartsNotBeingUsed
    summarizePoolStats(collectPoolStats)
    stringBuilder.toString()
  }

  /**
   *
   * @return
   */
  def collectPoolStats: mutable.HashMap[Long, (Long, Long)] = {
    val stats = new mutable.HashMap[Long, (Long, Long)]
    pools.filter(_ != null).sortBy(_.partByteSize) foreach {
      pool =>
        val size: Long = pool.partByteSize
        val allocated: Long = pool.partsAllocated
        val inUse: Long = pool.partsInUse
        var tally = stats.getOrElseUpdate(size, (0, 0))
        stats += size -> (tally._1 + allocated, tally._2 + inUse)
    }
    stats
  }

  /**
   * Print out a summary of pool stats and return the total number of bytes in the pools
   *
   * @param stats
   * @param builder
   * @return
   */
  def summarizePoolStats(stats: mutable.HashMap[Long, (Long, Long)])(implicit builder: StringBuilder): Long = {
    var totalAllocatedBytes = 0L
    var totalInUseBytes = 0L

    if (debugTending) {
      builder ++= s"\n*****************************************\n"
      builder ++= s"'$partName' part factory stats:\n"
    }

    stats.filter(_._2._1 > 0) foreach {
      case (size, (allocated, inUse)) =>

        if (debugTending)
          builder ++= s"\tpartSize=${
            prettyByteSizeString(size)
          }, partsAllocated=${
            prettySizeString(allocated)
          }, partsInUse=${
            prettySizeString(inUse)
          }, totalAllocatedBytes=${
            prettyByteSizeString(size * allocated)
          }, totalInUseBytes=${
            prettyByteSizeString(size * inUse)
          }\n"

        totalAllocatedBytes += size * allocated
        totalInUseBytes += size * inUse
    }

    if (debugTending && totalAllocatedBytes == 0)
      builder ++= s"\tall part pools empty...\n"

    totalAllocatedBytes
  }

  ///////////////////////////////////////////////////////////////////////////////
  // JMX Bean
  ///////////////////////////////////////////////////////////////////////////////

  sealed trait PartFactoryMBean {
    def printReport: String
  }

  final class PartFactory extends PartFactoryMBean {
    def printReport: String = {
      implicit val builder: StringBuilder = new StringBuilder
      summarizePoolStats(collectPoolStats)
      builder ++= s"*****************************************\n"
      builder.result()
    }
  }

}
