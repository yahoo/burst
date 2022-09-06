/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.scanner

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.data.model.snap.FabricSnap
import org.burstsys.fabric.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._

/**
 * a [[FabricScanner]] that has multiple data ''planes'' to support parallel scans
 */
trait FabricPlaneScanner extends FabricScanner {

  /**
   * the number of current active (parallel) planes in this scanner (this object is reused to it may change
   * over time)
   *
   * @return
   */
  def activePlanes: Int

  /**
   * called on master to initialize the scanner
   *
   * @param group
   * @param datasource
   * @param activePlanes
   * @return
   */
  def initialize(group: FabricGroupKey, datasource: FabricDatasource, activePlanes: Int): this.type

}

abstract
class FabricPlaneScannerContext extends FabricScannerContext with FabricPlaneScanner {

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  @transient private[this]
  var _activePlanes: Int = _

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // accessors
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def activePlanes: Int = _activePlanes

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def initialize(group: FabricGroupKey, datasource: FabricDatasource, activePlanes: Int): this.type = {
    super.initialize(group, datasource)
    _activePlanes = activePlanes
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * The scanner is written on the master to send to the workers to read. It is not sent back from the
   * worker to the master.
   *
   */
  override
  def write(kryo: Kryo, output: Output): Unit = {
    try {
      super.write(kryo, output)
      output writeInt _activePlanes
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }
  }

  /**
   * the scanner is read on the worker to start a query execution/scan
   *
   * @param kryo
   * @param input
   */
  override
  def read(kryo: Kryo, input: Input): Unit = {
    try {
      super.read(kryo, input)
      _activePlanes = input.readInt
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }
  }

}
