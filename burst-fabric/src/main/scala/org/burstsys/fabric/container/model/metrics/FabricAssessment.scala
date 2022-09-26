/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.model.metrics

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}

/**
  * A message that is sent from [[org.burstsys.fabric.topology.model.node.worker.FabricWorker]] to
 * [[org.burstsys.fabric.topology.model.node.supervisor.FabricSupervisor]] to provide a coarse grained
 * overall 'health' and 'performance' metric that can be used to determine if a worker is alive and
 * appropriately responsive/healthy.
  * @param ping round trip message time from supervisor to worker to supervisor
  * @param qps current waves per second
  * @param wls
  * @param cls
  * @param lav OS level load average
  * @param memory
  * @param disk
  * @param error
  */
final case
class FabricAssessment(
                        var ping: FabricLastHourMetric,
                        var qps: FabricLastHourMetric,
                        var wls: FabricLastHourMetric,
                        var cls: FabricLastHourMetric,
                        var lav: FabricLastHourMetric,
                        var memory: FabricLastHourMetric,
                        var disk: FabricLastHourMetric,
                        var error: FabricLastHourMetric
                      ) extends KryoSerializable {

  override
  def read(kryo: Kryo, input: Input): Unit = {
    ping = kryo.readClassAndObject(input).asInstanceOf[FabricLastHourMetric]
    qps = kryo.readClassAndObject(input).asInstanceOf[FabricLastHourMetric]
    wls = kryo.readClassAndObject(input).asInstanceOf[FabricLastHourMetric]
    cls = kryo.readClassAndObject(input).asInstanceOf[FabricLastHourMetric]
    lav = kryo.readClassAndObject(input).asInstanceOf[FabricLastHourMetric]
    memory = kryo.readClassAndObject(input).asInstanceOf[FabricLastHourMetric]
    disk = kryo.readClassAndObject(input).asInstanceOf[FabricLastHourMetric]
    error = kryo.readClassAndObject(input).asInstanceOf[FabricLastHourMetric]
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    kryo.writeClassAndObject(output, ping)
    kryo.writeClassAndObject(output, qps)
    kryo.writeClassAndObject(output, wls)
    kryo.writeClassAndObject(output, cls)
    kryo.writeClassAndObject(output, lav)
    kryo.writeClassAndObject(output, memory)
    kryo.writeClassAndObject(output, disk)
    kryo.writeClassAndObject(output, error)
  }

}
