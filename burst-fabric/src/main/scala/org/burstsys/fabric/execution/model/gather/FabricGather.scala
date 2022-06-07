/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.gather

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.execution.FabricResourceHolder
import org.burstsys.fabric.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.execution.model.gather.metrics.{FabricGatherMetrics, FabricGatherMetricsContext, FabricOutcome}
import org.burstsys.fabric.execution.model.result.FabricResult
import org.burstsys.fabric.execution.model.result.state.FabricScanState
import org.burstsys.fabric.execution.model.scanner.FabricScanner
import org.burstsys.fabric.execution.model.wave.FabricWave
import org.burstsys.vitals.errors.{VitalsException, safely}
import org.burstsys.vitals.logging.burstStdMsg

/**
 * [[FabricGather]] instances are what are collected from workers and returned to the master
 * as part of a [[FabricWave]] / [[org.burstsys.fabric.execution.model.wave.FabricParticle]]
 * scatter/gather
 * <p/>
 * They come in two basic subtypes [[org.burstsys.fabric.execution.model.gather.control.FabricControlGather]]
 * and [[org.burstsys.fabric.execution.model.gather.data.FabricDataGather]] depending if they represent
 * returns that contain data (normal) or control (exceptional) information
 */
trait FabricGather extends AnyRef
  with FabricMerge with FabricResult {

  /**
   * the identity of the group execuFabricWaveExecute.scalation this gather is part of
   *
   * @return
   */
  def groupKey: FabricGroupKey

  /**
   * the metrics associated with this gather
   *
   * @return
   */
  def gatherMetrics: FabricGatherMetrics

  /**
   * the current scan state of this gather
   *
   * @return
   */
  def scanState: FabricScanState

  /**
   * the scanner that created this gather
   *
   * @return
   */
  def scanner: FabricScanner

  /**
   * initialize this reusable gather
   *
   * @param scanner
   * @return
   */
  def initialize(scanner: FabricScanner): this.type

}


/**
 * abstract base class for all standard gathers
 */
abstract
class FabricGatherContext extends AnyRef with FabricGather with KryoSerializable {

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  final var _scanner: FabricScanner = _

  private[this]
  final var _groupKey: FabricGroupKey = _

  private[this]
  final var _gatherMetrics: FabricGatherMetrics = FabricGatherMetrics()

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def scanner: FabricScanner = _scanner

  final override
  def groupKey: FabricGroupKey = _groupKey

  final override
  def gatherMetrics: FabricGatherMetrics = _gatherMetrics

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def initialize(scanner: FabricScanner): this.type = {
    this._scanner = scanner
    _groupKey = _scanner.group
    _gatherMetrics.initMetrics(_scanner.datasource)
    initializeOutcome()
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Merging
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def regionMerge(gather: FabricMerge): Unit = {
    try {
      if (hadException) return
      if (gather == null) {
        val msg = burstStdMsg(s"regionMerge gather==null")
        log error msg
        this.markException(new RuntimeException(msg).fillInStackTrace())
        return
      }
      mergeOutcome(gather)
    } catch safely {
      case t: Throwable =>
        val msg = burstStdMsg(t)
        log error(msg, t)
        this.markException(t)
    }
  }

  override
  def sliceMerge(gather: FabricMerge): Unit = {
    try {
      if (hadException) return
      if (gather == null) {
        val msg = burstStdMsg(s"sliceMerge gather==null")
        log error msg
        this.markException(new RuntimeException(msg).fillInStackTrace())
        return
      }
      mergeOutcome(gather)
    } catch safely {
      case t: Throwable =>
        val msg = burstStdMsg(t)
        log error(msg, t)
        this.markException(t)
    }
  }

  override
  def waveMerge(gather: FabricMerge): Unit = {
    try {
      if (hadException) return
      if (gather == null) {
        val msg = burstStdMsg(s"scatterMerge gather==null")
        log error msg
        this.markException(new RuntimeException(msg).fillInStackTrace())
        return
      }
      mergeOutcome(gather)
    } catch safely {
      case t: Throwable =>
        val msg = burstStdMsg(t)
        log error(msg, t)
        this.markException(t)
    }
  }


  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * serialize on worker to send back to master
   *
   * @param kryo
   * @param output
   */
  override
  def write(kryo: Kryo, output: Output): Unit = {
    try {
      kryo.writeClassAndObject(output, _groupKey)
      writeOutcome(kryo, output)
      kryo.writeClassAndObject(output, _gatherMetrics)
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

  /**
   * deserialize on master after receipt from worker
   *
   * @param kryo
   * @param input
   */
  override
  def read(kryo: Kryo, input: Input): Unit = {
    try {
      _groupKey = kryo.readClassAndObject(input).asInstanceOf[FabricGroupKey]
      readOutcome(kryo, input)
      _gatherMetrics = kryo.readClassAndObject(input).asInstanceOf[FabricGatherMetricsContext]
      _scanner = null // this asserts this as being a master side operation (not on executor)
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }


}
