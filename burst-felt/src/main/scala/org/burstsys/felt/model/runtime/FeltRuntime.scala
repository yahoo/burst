/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.runtime

import org.burstsys.brio.blob.BrioBlob
import org.burstsys.brio.lattice.BrioLatticeReference
import org.burstsys.fabric.wave.data.model.snap.FabricSnap
import org.burstsys.fabric.wave.execution.model.execute.invoke.{FabricDebugLevel, FabricInvocation, FabricReportLevel}
import org.burstsys.fabric.wave.execution.model.runtime.{FabricRuntime, FabricRuntimeContext}
import org.burstsys.felt.model.collectors.FeltCollectorRuntime
import org.burstsys.felt.model.control.FeltCtrlRuntime
import org.burstsys.felt.model.mutables.FeltBindingRuntime
import org.burstsys.vitals.text.VitalsTextCodec
import org.burstsys.vitals.time.VitalsTimeZones._
import org.joda.time.MutableDateTime

/**
 * All meta-data and data that is specific to a single sweep. It is not thread safe so these need to be allocated
 * per thread. They should only
 */
trait FeltRuntime extends AnyRef
  with FabricRuntime with FeltBindingRuntime with FeltCtrlRuntime with FeltCollectorRuntime {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // per traversal data access
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * the root of the brio object tree being scanned as a brio lattice reference
   *
   * @return
   */
  def lattice: BrioLatticeReference

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // coarse grained lifecycle operations
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * called before each use of the runtime. This is needed since felt runtimes are meant to be
   * cached and re-used.
   *
   * @param snap
   * @param blob
   */
  def prepare(snap: FabricSnap, blob: BrioBlob): FeltRuntime

  /**
   * called before each use of the runtime - designed for the generated code runtime - called by '''prepare()'''
   *
   * @param blob
   * @return
   */
  def generatedPrepare(blob: BrioBlob): FeltRuntime

  /**
   * called after each use of the runtime.
   */
  def release: FeltRuntime

  /**
   * called after each use of the runtime - designed for the generated code runtime - called by  '''release()'''
   *
   * @return
   */
  def generatedRelease: FeltRuntime

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // per invocation information
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * immutable per invocation specific parameters e.g. this can be different for
   * each query call with different parameters etc.
   *
   * @return
   */
  def invocation: FabricInvocation

  /**
   * cached epoch time for ''now'' at beginning of scan so it is consistent across scan
   *
   * @return
   */
  def now: Long

  /**
   * cached and reusable UTF8 string management. Scans are singled threaded so this is never thread shared
   *
   * @return
   */
  implicit def text: VitalsTextCodec

  /**
   * cached and reusable datetime helper to optimize the cost of creating this time information
   *
   * @return
   */
  implicit def time: MutableDateTime

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // debugging reports
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   *
   * @param text
   * @param reportLevel
   */
  @inline final
  def report(text: String, reportLevel: FabricReportLevel = FabricDebugLevel): Unit = invocation.report(text, reportLevel)

}

abstract
class FeltRuntimeContext(val invocation: FabricInvocation) extends FabricRuntimeContext with FeltRuntime {

  ////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  ////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _lattice: BrioLatticeReference = _

  ////////////////////////////////////////////////////////////////////////////////////////////
  // accessors
  ////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def lattice: BrioLatticeReference = _lattice

  @inline final override
  def now: Long = invocation.now

  ////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  ////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def prepare(snap: FabricSnap, blob: BrioBlob): FeltRuntime = {
    super.prepare(snap, blob)
    _lattice = blob.reference
    this.prepareBrioThreadRuntime(invocation.timeZone)
    // this gives us access to the initialization of artifacts in the code generated runtime object
    generatedPrepare(blob)
    this
  }

  // this gives us access to the finalization of artifacts in the code generated runtime object
  @inline final override
  def release: FeltRuntime = {
    generatedRelease
    this
  }

}
