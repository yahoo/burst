/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.part.factory

import org.burstsys.tesla.part.TeslaPartPool
import org.burstsys.vitals.host
import org.burstsys.vitals.reporter.instrument.{prettyByteSizeString, prettySizeString}

import scala.collection.mutable

/**
 * stats reporting for tesla parts
 */
trait TeslaFactoryPrinter[Part, PartPool <: TeslaPartPool[Part]] {

  self: TeslaPartFactory[Part, PartPool] =>

  /**
   *
   */
  def collectPoolStats: mutable.HashMap[Long, (Long, Long)] = {
    val stats = new mutable.HashMap[Long, (Long, Long)]
    pools.filter(_ != null).sortBy(_.partByteSize) foreach {
      pool =>
        val size: Long = pool.partByteSize
        val allocated: Long = pool.partsAllocated
        val inUse: Long = pool.partsInUse
        val tally = stats.getOrElseUpdate(size, (0, 0))
        stats += size -> (tally._1 + allocated, tally._2 + inUse)
    }
    stats
  }

  /**
   * Print out a summary of pool stats and return the total number of bytes in the pools
   *
   */
  def summarizePoolStats(stats: mutable.HashMap[Long, (Long, Long)], builder: Option[StringBuilder]): Long = {
    var totalAllocatedBytes = 0L
    var totalInUseBytes = 0L

    if (builder.isDefined) {
      builder.get ++= s"\n*****************************************\n"
      builder.get ++= s"'$partName' part factory stats:  (directSize=${host.directMemoryUsed} (${prettyByteSizeString(host.directMemoryUsed)}))\n"
    }

    stats.filter(_._2._1 > 0) foreach {
      case (size, (allocated, inUse)) =>

        if (builder.isDefined) {
          builder.get ++= s"\tpartSize=$size (${prettyByteSizeString(size)}), partsAllocated=$allocated (${prettySizeString(allocated)})"
          builder.get ++= s", partsInUse=$inUse (${prettySizeString(inUse)}), totalAllocatedBytes=${size*allocated} (${prettyByteSizeString(size * allocated)})"
          builder.get ++= s", totalInUseBytes=${size*inUse} (${prettyByteSizeString(size * inUse)})\n"
        }

        totalAllocatedBytes += size * allocated
        totalInUseBytes += size * inUse
    }

    if (builder.isDefined && totalAllocatedBytes == 0)
      builder.get ++= s"\tall part pools empty...\n"

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
      val builder: StringBuilder = new StringBuilder
      summarizePoolStats(collectPoolStats, Some(builder))
      builder ++= s"*****************************************\n"
      builder.result()
    }
  }

}
