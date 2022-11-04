/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.runtime

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.blob.BrioBlob
import org.burstsys.fabric.wave.data.model.snap.FabricSnap
import org.burstsys.fabric.wave.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.wave.execution.model.execute.invoke.FabricInvocation
import org.burstsys.fabric.wave.execution.model.gather.FabricGather
import org.burstsys.fabric.wave.execution.model.gather.control.FabricFaultGather
import org.burstsys.fabric.wave.execution.model.result.state.FabricNoDataStatus
import org.burstsys.fabric.wave.execution.model.scanner.{FabricPlaneScanner, FabricPlaneScannerContext}
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.felt.model.sweep.FeltSweep
import org.burstsys.hydra.sweep.{HydraRuntime, HydraSweep}
import org.burstsys.vitals.errors.{VitalsException, _}

import scala.concurrent.TimeoutException
import scala.language.postfixOps

/**
 * specialized scanner for the Hydra FELT language binding. When creating a FELT language you need to implement your
 * own version of this [[FabricPlaneScanner]] and add collectors and specialized inputs etc.
 */
final case
class HydraScanner() extends FabricPlaneScannerContext with FabricPlaneScanner {

  override val scannerName: String = "Hydra"

  /////////////////////////////////////
  // worker side private state
  /////////////////////////////////////

  /**
   * single sweep instance shared across all threads...
   */
  private[this]
  var _sweep: FeltSweep = _

  /**
   * single fault instance shared across all threads...
   */
  private[this]
  var _fault: Throwable = _

  /**
   * we keep track of a per-thread runtime for each scanner
   */
  private[this]
  lazy val _runtime: ThreadLocal[HydraRuntime] = new ThreadLocal[HydraRuntime]

  /////////////////////////////////////
  // supervisor/worker (serialized) side private state
  /////////////////////////////////////

  private[this]
  var _invocation: FabricInvocation = _

  /////////////////////////////////////
  // lifecycle
  /////////////////////////////////////

  /**
   * setup for scan e.g. resource allocation, runtime value default settings
   *
   * @param group
   * @param datasource
   * @param invocation
   * @return
   */
  def initialize(
                  group: FabricGroupKey,
                  datasource: FabricDatasource,
                  activePlanes: Int,
                  invocation: FabricInvocation
                ): this.type = {
    super.initialize(group, datasource, activePlanes)
    _invocation = invocation
    this
  }

  /////////////////////////////////////
  // API
  /////////////////////////////////////

  /**
   * the invocation this scanner is executing
   *
   * @return
   */
  def invocation: FabricInvocation = _invocation

  /////////////////////////////////////
  // Sweep
  /////////////////////////////////////

  override
  def beforeAllScans(snap: FabricSnap): this.type = {
    super.beforeAllScans((snap))
    log debug s"WORKER_BEFORE_ALL_SCANS"

    if (_sweep != null)
      log warn s"WORKER_BEFORE_ALL_SCANS sweep not null!"

    fetchSweepWithReadLock() // we have a read lock on the sweep artifact in the cache

    // check for problem with fetchSweepWithReadLock()
    if (_fault != null)
      throw _fault

    this
  }

  override
  def afterAllScans(snap: FabricSnap): this.type = {
    super.afterAllScans((snap))
    if (StaticSweep == null) {
      // release read lock on sweep artifact in the cache
      if (_sweep != null)
        _sweep.artifact.releaseReadLock
      _sweep = null
    }
    this
  }

  /**
   * the sweep is instantiated on the worker side once per worker.
   * Note that the sweep is cached so that if the normalized hydra source
   * matches a key in the cache - no more compilation/reification of the sweep
   * is necessary.
   */
  private
  def fetchSweepWithReadLock(): Unit = {
    lazy val tag = s"HydraScanner.fetchSweepWithReadLock(group=$group, datasource=$datasource)"
    try {
      if (StaticSweep != null) {
        log info s"HYDRA_SWEEP_GEN_STATIC $tag"
        _sweep = StaticSweep
      } else {
        synchronized {
          log info s"HYDRA_SWEEP_GEN_BEGIN $tag"
          _sweep = try {
            // wait for a sweep with a read lock
            HydraSweep(source = _invocation.groupSource, schemaName = datasource.view.schemaName)
          } catch {
            case t: TimeoutException =>
              _fault = VitalsException(s"hydra generation timeout after $maxGenerateDuration")
              null
          }
          log info s"HYDRA_SWEEP_GEN_END $tag"
        }
      }
    } catch safely {
      case t: Throwable =>
        log info s"HYDRA_SWEEP_GEN_FAIL $tag"
        _fault = t
    }
  }

  /**
   * per-thread runtime for __this__ scanner
   *
   * @return
   */
  def runtime: HydraRuntime = {
    lazy val tag = s"HydraScanner.runtime(group=$group, datasource=$datasource)"
    try {
      var rt = _runtime.get
      if (rt == null) {
        rt = _sweep.newRuntime(_invocation).asInstanceOf[HydraRuntime]
        _runtime.set(rt)
      }
      rt
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
  }

  /////////////////////////////////////
  // scan
  /////////////////////////////////////

  /**
   * This is the signature in the Fabric Scanner that is called once per Brio item. It all starts here.
   *
   * @param blob
   * @return
   */
  @inline override
  def apply(blob: BrioBlob): FabricGather = {
    lazy val tag = s"HydraScanner.apply(group=$group, datasource=$datasource)"
    startSerialTraversal
    try {

      // gathers are a pooled resource
      val gather = grabGather(this)
      gather.activatePlanesOnWorker(_sweep.feltBinding, _sweep.collectorBuilders)

      // runtimes are a pooled resource taken from a thread local for this specific scanner
      val rt = runtime.prepare(snap, blob, gather)

      // assert a special no data state so downstream processing does not get confused
      if (blob.isEmpty) {
        gather.scanState(FabricNoDataStatus)
        return gather
      }

      try {
        _sweep apply rt // this is the scan/traversal
        gather // return our result gather
      } finally {
        rt release gather // return to pool in an appropriate state
      }
    } catch safely {
      case t: Throwable =>
        FabricFaultGather(this, t)
    } finally endSerialTraversal
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    try {
      super.write(kryo, output)
      kryo writeClassAndObject(output, _invocation)
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    try {
      super.read(kryo, input)
      _invocation = kryo.readClassAndObject(input).asInstanceOf[FabricInvocation]
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }
  }

}
