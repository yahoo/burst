/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.gather.plane

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.wave.execution.model.gather.FabricMerge
import org.burstsys.fabric.wave.execution.model.gather.control.FabricFaultGather
import org.burstsys.fabric.wave.execution.model.gather.data.{FabricDataGatherContext, FabricEmptyGather}
import org.burstsys.fabric.wave.execution.model.result.state._
import org.burstsys.fabric.wave.execution.model.scanner.FabricScanner
import org.burstsys.fabric.wave.execution.model.{FabricMergeLevel, FabricRegionMergeLevel, FabricSliceMergeLevel, FabricWaveMergeLevel}
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._

/**
 * A `Gather` that consists of a concurrent set of `Planes` each of which holds one result out of a `Group`
 */
trait FabricPlaneGather extends FabricDataGatherContext {

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _planes: Array[FabricPlane] = new Array[FabricPlane](FabricMaxPlanes)

  private[this]
  var _activePlanes: Int = 0

  private[this]
  var _isResultPlane: Boolean = false

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * the set of parallel planes in this gather
   *
   * @return
   */
  final
  def planes: Array[FabricPlane] = _planes

  /**
   * finish this operation
   */
  final
  def finalizePlanes: this.type = {
    var i = 0
    while (i < activePlanes) {
      val plane = planes(i)
      if (plane.scanState == FabricScanRunning) plane.scanState(FabricSuccessStatus)
      i += 1
    }
    this
  }

  override
  def releaseResourcesOnWorker(): Unit = {
    var i = 0
    while (i < activePlanes) {
      _planes(i).releaseResourcesOnWorker()
      i += 1
    }
  }

  override
  def releaseResourcesOnSupervisor(): Unit = {
    var i = 0
    while (i < activePlanes) {
      _planes(i).releaseResourcesOnSupervisor()
      i += 1
    }
  }

  /**
   * reset this object for reuse
   */
  final
  def clearPlanes: this.type = {
    var i = 0
    while (i < activePlanes) {
      _planes(i).clear()
      i += 1
    }
    this
  }

  /**
   * total rows in the plane
   *
   * @return
   */
  final
  def totalRows: Int = {
    var i = 0
    var tally = 0
    while (i < activePlanes) {
      tally += _planes(i).rowCount
      i += 1
    }
    tally
  }

  /**
   * count of parallel planes currently active in this reusable object
   *
   * @return
   */
  final
  def activePlanes: Int = _activePlanes

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * initialize this reusable object
   *
   * @param scanner
   * @param activePlanes
   * @return
   */
  final
  def initialize(scanner: FabricScanner, activePlanes: Int): this.type = {
    super.initialize(scanner)
    checkPlaneCount(activePlanes)
    _activePlanes = activePlanes
    _isResultPlane = false
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // FabricMerge
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  override final
  def regionMerge(gather: FabricMerge): Unit = {
    try {
      merge(gather, FabricRegionMergeLevel)
    } finally {
      gather.releaseResourcesOnWorker() // once the other gather is merged, we can free up the associated resources
    }
  }

  override final
  def sliceMerge(gather: FabricMerge): Unit = {
    try {
      merge(gather, FabricSliceMergeLevel)
    } finally {
      gather.releaseResourcesOnWorker() // once the other gather is merged, we can free up the associated resources
    }
  }

  override final
  def waveMerge(gather: FabricMerge): Unit = {
    try {
      merge(gather, FabricWaveMergeLevel)
    } finally {
      gather.releaseResourcesOnSupervisor() // once the other gather is merged, we can free up the associated resources
    }
  }

  private def merge(gather: FabricMerge, level: FabricMergeLevel): Unit = {
    try {
      level match {
        case FabricRegionMergeLevel =>
          super.regionMerge(gather)
        case FabricSliceMergeLevel =>
          super.sliceMerge(gather)
        case FabricWaveMergeLevel =>
          super.waveMerge(gather)
        case _ => ???
      }
      if (hadException) return

      gather match {
        case thatGather: FabricPlaneGather =>
          if (thisOutcomeOrThatOutcomeInvalid(thatGather))
            return

          var i = 0
          var continue = true
          while (i < activePlanes && continue) {
            val thisPlane = planes(i)
            val thatPlane = thatGather.planes(i)
            thisPlane.mergeOutcome(thatPlane)

            if (thisPlane.hadException) {
              this.markException(thisPlane.exception)
              continue = false
            } else level match {
              case FabricRegionMergeLevel =>
                thisPlane.regionMerge(thatPlane)
              case FabricSliceMergeLevel =>
                thisPlane.sliceMerge(thatPlane)
              case FabricWaveMergeLevel =>
                thisPlane.waveMerge(thatPlane)
              case _ => ???
            }

            i += 1
          }

        case _: FabricEmptyGather => // do nothing
        case faultGather: FabricFaultGather =>
          this.markException(faultGather.fault)
        case g =>
          this.markException(VitalsException(s"unknown gather ${g.getClass}").fillInStackTrace())
      }

    } catch safely {
      case t: Throwable =>
        val msg = burstStdMsg(s"MERGE_FAIL FabricPlaneGather.merge level=${level}", t)
        log error msg
        this.markException(t)
    }
  }


  /**
   * this is to allow final processing of the gather on the remote node
   * before sending it back to the supervisor.
   *
   * @return
   */
  final override
  def sliceFinalize(): Unit = {
    var i = 0
    while (i < activePlanes) {
      planes(i).sliceFinalize()
      i += 1
    }
  }

  /**
   * called on the supervisor for the very last
   *
   * @return
   */
  final override
  def waveFinalize(): Unit = {
    var i = 0
    while (i < activePlanes) {
      planes(i).waveFinalize()
      i += 1
    }
  }

  private def checkPlaneCount(activePlanes: Int): Unit = {
    if (activePlanes > FabricMaxPlanes)
      throw VitalsException(s"activePlanes requested greater than FabricMaxPlanes=$FabricMaxPlanes")
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * The gather is serialized from the worker nodes to the supervisor to collect final results. (It is
   * not serialized from the supervisor to the worker)
   *
   * @param kryo
   * @param output
   */
  override
  def write(kryo: Kryo, output: Output): Unit = {
    lazy val tag = s"FabricPlaneGather.write"
    try {
      super.write(kryo, output)
      output writeInt activePlanes
      var i = 0
      while (i < activePlanes) {
        kryo.writeClassAndObject(output, planes(i))
        i += 1
      }
    } catch safely {
      case t: Throwable =>
        val msg = s"FABRIC_PLANE_KRYO_WRITE_FAIL ${t.getMessage} $tag"
        log error msg
        throw VitalsException(msg, t)
    }
  }

  /**
   * This is read back on the supervisor node to collect results.
   * There is no scan context - we manage artifacts differently than the way
   * we do on the worker (executor) nodes.
   *
   * @param kryo
   * @param input
   */
  override
  def read(kryo: Kryo, input: Input): Unit = {
    lazy val tag = s"FabricPlaneGather.read"
    try {
      super.read(kryo, input)
      _activePlanes = input.readInt
      _planes = new Array[FabricPlane](_activePlanes)
      var i = 0
      while (i < activePlanes) {
        planes(i) = kryo.readClassAndObject(input).asInstanceOf[FabricPlane]
        i += 1
      }
    } catch safely {
      case t: Throwable =>
        val msg = s"FABRIC_PLANE_KRYO_READ_FAIL ${t.getMessage} $tag"
        log error msg
        throw VitalsException(msg, t)
    }
  }

}
