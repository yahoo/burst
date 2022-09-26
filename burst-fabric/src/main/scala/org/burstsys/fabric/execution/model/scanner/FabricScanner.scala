/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.scanner

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.brio.blob.BrioBlob
import org.burstsys.fabric.data.model.snap.FabricSnap
import org.burstsys.fabric.execution.model.execute.group.{FabricGroupKey, FabricGroupKeyContext}
import org.burstsys.fabric.execution.model.gather.FabricGather
import org.burstsys.fabric.execution.worker.FabricWorkerScanner
import org.burstsys.fabric.metadata.model.datasource.{FabricDatasource, FabricDatasourceContext}
import org.burstsys.vitals.errors._

/**
 * Base for all fabric scan algorithms
 */
trait FabricScanner extends (BrioBlob => FabricGather) with FabricWorkerScanner {

  /**
   * The identity of the 'group' scan
   *
   * @return
   */
  def group: FabricGroupKey

  /**
   * The datasource definition
   *
   * @return
   */
  def datasource: FabricDatasource

  /**
   * the snap for this scan - valid only on worker - null on supervisor
   *
   * @return
   */
  def snap: FabricSnap

  /**
   * called on the supervisor before the scan is sent to the worker
   *
   * @param group
   * @param datasource
   * @return
   */
  def initialize(group: FabricGroupKey, datasource: FabricDatasource): this.type

  /**
   * called on the worker '''before''' all scans on a dataset
   *
   * @param snap
   * @return
   */
  def beforeAllScans(snap: FabricSnap): this.type

  /**
   * called on the worker '''after''' all scans on a dataset
   *
   * @param snap
   * @return
   */
  def afterAllScans(snap: FabricSnap): this.type

}

abstract
class FabricScannerContext extends AnyRef with FabricScanner with KryoSerializable {

  def scannerName: String

  final override
  def toString: String = s"$scannerName:${group.groupName}$datasource"

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  @transient private[this]
  var _group: FabricGroupKey = _

  @transient private[this]
  var _datasource: FabricDatasource = _

  @transient private[this]
  var _snap: FabricSnap = _

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // accessors
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def group: FabricGroupKey = _group

  final override
  def datasource: FabricDatasource = _datasource

  final
  def snap: FabricSnap = _snap

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def initialize(group: FabricGroupKey, datasource: FabricDatasource): this.type = {
    _group = group
    _datasource = datasource
    this
  }

  override
  def beforeAllScans(snap: FabricSnap): this.type = {
    _snap = snap
    this
  }

  override
  def afterAllScans(snap: FabricSnap): this.type = {
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * The scanner is written on the supervisor to send to the workers to read. It is not sent back from the
   * worker to the supervisor.
   *
   */
  override
  def write(kryo: Kryo, output: Output): Unit = {
    try {
      kryo.writeClassAndObject(output, _group)
      kryo.writeClassAndObject(output, _datasource)
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
      _group = kryo.readClassAndObject(input).asInstanceOf[FabricGroupKeyContext]
      _datasource = kryo.readClassAndObject(input).asInstanceOf[FabricDatasourceContext]
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }
  }

}
