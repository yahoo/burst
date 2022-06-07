/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.model.slice.region

import org.burstsys.vitals.instrument._
import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.time.nsToSec

import scala.concurrent.duration._
import scala.language.postfixOps

package object hose extends VitalsLogger with FabricRegionInflator {

  final val debugHose = false

  final val queuePutSize = 100

  final val queuePutWait = 15 seconds

  /**
   * common metrics across hoses and sprays
   */
  trait FabricWriteMetrics extends Any {

    /**
     * @return elapsed time of the complete write operation
     */
    def elapsedNs: Long

    /**
     * @return how many parcels were written
     */
    def parcelCount: Long

    /**
     * @return how many items were written
     */
    def itemCount: Long

    /**
     * @return how many inflated bytes were processed (inflation not required), this is what is written to disk
     */
    def inflatedByteCount: Long

    /**
     * @return how many deflated bytes were processed (inflation required), this is how much is inflated
     */
    def deflatedByteCount: Long

    /**
     * @return total time spent in various sync operations (these can overlap ioWaits)
     */
    def syncWaitNs: Long

    /**
     * @return total time spent in various IO operations
     */
    def ioWaitNs: Long

    /**
     * @return bytes per second rate for process inflated bytes (written to disk)
     */
    final def inflatedByteRate: Double = if(elapsedNs == 0) 0 else inflatedByteCount.toDouble / nsToSec(elapsedNs)

    /**
     * @return bytes per second rate for processing deflated bytes (inflated and then written to disk)
     */
    final def deflatedByteRate: Double = if(elapsedNs == 0) 0 else deflatedByteCount.toDouble / nsToSec(elapsedNs)

    /**
     * @return the ratio of inflated to deflated bytes processed
     */
    final def compressionRatio: Double = if(deflatedByteCount==0) 0 else inflatedByteCount / deflatedByteCount

    def sprayMetrics: String =
      s"""|   parcelCount=$parcelCount (${prettySizeString(parcelCount)})
          |   itemCount=$itemCount (${prettySizeString(itemCount)})
          |   elapsedNs=$elapsedNs (${prettyTimeFromNanos(elapsedNs)})
          |   syncWaitNs=$syncWaitNs (${prettyTimeFromNanos(ioWaitNs)})
          |   ioWaitNs=$ioWaitNs (${prettyTimeFromNanos(ioWaitNs)})
          |   ------------ DISK WRITES ------------
          |   inflatedByteCount=$inflatedByteCount (${prettyByteSizeString(inflatedByteCount)})
          |   inflatedByteRate=$inflatedByteRate (${prettyByteSizeString(inflatedByteRate)}/S)
          |   ------------ PARCEL INFLATION ------------
          |   deflatedByteCount=$deflatedByteCount (${prettyByteSizeString(deflatedByteCount)})
          |   deflatedByteRate=$deflatedByteRate (${prettyByteSizeString(deflatedByteRate)}/S)
          |   compressionRatio=$compressionRatio""".stripMargin

  }


}
