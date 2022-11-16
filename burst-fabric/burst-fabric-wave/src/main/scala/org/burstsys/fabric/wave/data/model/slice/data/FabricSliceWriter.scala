/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.slice.data

import org.burstsys.fabric.wave.data.model.slice.region.hose.FabricWriteMetrics
import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.reporter.instrument.prettyTimeFromNanos
import org.burstsys.vitals.logging._

import scala.language.postfixOps

/**
 * write operation for a slice in a fabric cache
 */
trait FabricSliceWriter extends AnyRef with FabricWriteMetrics {

  self: FabricSliceDataContext =>

  //////////////////////////////////////////
  // private state
  //////////////////////////////////////////

  private[this]
  var _isOpenForWrites = false

  private[this]
  var _start: Long = _

  private[this]
  var _finish: Long = _

  //////////////////////////////////////////
  // API
  //////////////////////////////////////////

  final override
  def isOpenForWrites: Boolean = _isOpenForWrites

  final override
  def timeSkew: Double = {
    if (regions.isEmpty) return 0.0
    val minElapsed = regions.map(_.elapsedNs.toDouble).min
    val maxElapsed = regions.map(_.elapsedNs.toDouble).max
    if (minElapsed == 0.0) return 0.0
    maxElapsed / minElapsed
  }

  final override def elapsedNs: Long = _finish - _start

  final override def parcelCount: Long = regions.map(_.parcelCount).sum

  final override def itemCount: Long = regions.map(_.itemCount).sum

  final override def inflatedByteCount: Long = regions.map(_.inflatedByteCount).sum

  final override def deflatedByteCount: Long = regions.map(_.deflatedByteCount).sum

  final override
  def syncWaitNs: Long =
    if (regions.length == 0) 0L else regions.map(_.syncWaitNs).sum / regions.length

  final override
  def ioWaitNs: Long =
    if (regions.length == 0) 0L else regions.map(_.ioWaitNs).sum / regions.length

  final override
  def openForWrites(): Unit = {
    lazy val tag = s"FabricSliceWriter.openForWrites($parameters)"
    _start = System.nanoTime()
    synchronized {
      if (isOpenForWrites) throw VitalsException(s"ALREADY_OPEN_FOR_WRITE! $tag")
      try {
        initializeRegions()
        regions.foreach(_.openRegionForWrites())
        log info s"SLICE_OPEN_WRITER regionCount=$regionCount, firstRegion=${nextRegion.regionIndex} $tag"
      } catch safely {
        case t: Throwable =>
          val msg = s"FAIL $t $tag"
          log error burstStdMsg(msg, t)
          throw VitalsException(msg, t)
      }
      _isOpenForWrites = true
    }
  }

  final override
  def closeForWrites(): Unit = {
    lazy val tag = s"FabricSliceWriter.closeForWrites($parameters)"
    if (!isOpenForWrites) throw VitalsException(s"NOT_OPEN_FOR_WRITE! $tag")
    try {

      // close region first in order to correctly catch runt regions
      regions.foreach(_.closeRegionForWrites())

      // get rid of runt region files
      regions foreach { region =>
        if (region.regionIsRunt) {
          region.flushRegionFromDisk()
          this -= region
        }
      }

      // flag runt regions...
      if (regions.isEmpty)
        log info burstStdMsg(s"NO_DATA (region set is empty) $tag")

      _isOpenForWrites = false

      log info
        s"""|
            |------------- SLICE_WRITE_SUCCESS -----------------
            |$sprayMetrics
            |   timeSkew=$timeSkew
            |--------------- $tag """.stripMargin
    } catch safely {
      case t: Throwable =>
        val msg = s"FAIL $t $tag"
        log error burstStdMsg(msg, t)
        throw VitalsException(msg, t)
    }
  }

  final override
  def waitForWritesToComplete(): Unit = {
    lazy val tag = s"FabricSliceWriter.waitForWritesToComplete($parameters)"
    if (!isOpenForWrites) throw VitalsException(s"NOT_OPEN_FOR_WRITE! $tag")
    try {
      val start = System.nanoTime
      regions foreach (_.markAllParcelsQueued())
      regions foreach (_.waitForWritesToComplete())
      val elapsedNs = System.nanoTime - start
      if (elapsedNs > slowWrite.toNanos)
        log warn s"SLOW_WRITE ($slowWrite) elapsedNs=$elapsedNs (${prettyTimeFromNanos(elapsedNs)}) $tag"
      sliceOnDisk = true
      _finish = System.nanoTime()
    } catch safely {
      case t: Throwable =>
        val msg = s"FAIL $t $tag"
        log error burstStdMsg(msg, t)
        throw VitalsException(msg, t)
    }
  }

  /**
   * queue up a mem buffer to be written to a region.
   * note that this buffer is released somewhere down the road
   * after this routine is called - do not free it yourself!
   *
   * @param parcel
   * @return
   */
  final override
  def queueParcelForWrite(parcel: TeslaParcel): Unit = {
    lazy val tag = s"FabricSliceWriter.queueParcelForWrite($parameters)"
    if (!isOpenForWrites) throw VitalsException(s"NOT_OPEN_FOR_WRITE! $tag")
    try {
      nextRegion queueParcelForWrite parcel
    } catch safely {
      case t: Throwable =>
        val msg = s"FAIL $t $tag"
        log error burstStdMsg(msg, t)
        throw VitalsException(msg, t)
    }
  }

}
