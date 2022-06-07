/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.runtime

import org.burstsys.brio.blob.BrioBlob
import org.burstsys.brio.runtime.{BrioItemRuntime, BrioThreadRuntime}
import org.burstsys.fabric.data.model.snap.FabricSnap

/**
 * generic fabric requirements for all runtimes
 */
trait FabricRuntime extends AnyRef with BrioItemRuntime with BrioThreadRuntime {

  /**
   * every scan of data has to have a [[FabricSnap]] to refer to
   *
   * @return
   */
  def snap: FabricSnap

  /**
   * called on worker before each use of runtime
   *
   * @param snap
   * @return
   */
  def prepare(snap: FabricSnap, blob: BrioBlob): FabricRuntime

}

abstract
class FabricRuntimeContext extends FabricRuntime  {

  ////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  ////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _snap: FabricSnap = _

  ////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def snap: FabricSnap = _snap

  @inline override
  def prepare(snap: FabricSnap, blob: BrioBlob): FabricRuntime = {
    prepare(blob)
    _snap = snap
    this
  }

}
