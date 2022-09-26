/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.runtime

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.execution.model.gather.plane.FabricPlaneGather
import org.burstsys.felt.model.collectors.runtime.FeltCollectorGather
import org.burstsys.tesla.pool.{TeslaPoolId, TeslaPooledResource}
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.errors.{VitalsException, _}

/**
 * specialize gather for the Hydra FELT language binding. When creating a FELT language you need to implement your
 * own version of  [[FabricPlaneGather]] and add collectors and specialized inputs etc.
 */
final case
class HydraGather() extends FeltCollectorGather with KryoSerializable with TeslaPooledResource {

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // state
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  val poolId: TeslaPoolId = hydraGatherPoolId

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Called the first time the gather is allocated from a parts shop.
   *
   * @return
   */
  @inline
  def initialize(scanner: HydraScanner): this.type = {
    super.initialize(scanner = scanner, activePlanes = scanner.activePlanes)
  }

  @inline override
  def releaseResourcesOnWorker(): Unit = {
    super.releaseResourcesOnWorker()
    releaseGather(this) // we pool gathers on worker - return to pool
  }

  @inline override
  def releaseResourcesOnSupervisor(): Unit = {
    super.releaseResourcesOnSupervisor()
    // we do not pool gathers on supervisors since they come in as kryo serialized objects over messaging
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    lazy val tag = s"HydraGather.write"
    try {
      TeslaWorkerCoupler {
        super.write(kryo, output)
        releaseResourcesOnWorker() // this op is on worker to send to supervisor
      }
    } catch safely {
      case t: Throwable =>
        val msg = s"HYDRA_KRYO_WRITE_FAIL ${t.getMessage} $tag"
        log error msg
        throw VitalsException(msg, t)
    }
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    lazy val tag = s"HydraGather.read"
    try {
      TeslaWorkerCoupler {
        super.read(kryo, input)
      }
    } catch safely {
      case t: Throwable =>
        val msg = s"HYDRA_KRYO_READ_FAIL ${t.getMessage} $tag"
        log error msg
        throw VitalsException(msg, t)
    }
  }

}
