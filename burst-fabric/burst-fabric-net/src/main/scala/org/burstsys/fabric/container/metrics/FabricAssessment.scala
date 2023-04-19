/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.metrics

import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.KryoSerializable
import org.burstsys.fabric.container.model.metrics.FabricLastHourMetric
import org.burstsys.fabric.net.message.AccessParameters

import scala.collection.mutable

/**
  * A message that is sent from [[org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode]] to
 * [[org.burstsys.fabric.topology.model.node.supervisor.FabricSupervisorNode]] to provide a coarse grained
 * overall 'health' and 'performance' metric that can be used to determine if a worker is alive and
 * appropriately responsive/healthy.
  */
final case
class FabricAssessment(
                        var ping: FabricLastHourMetric,
                        var lav: FabricLastHourMetric,
                        var memory: FabricLastHourMetric,
                        var disk: FabricLastHourMetric,
                      ) extends KryoSerializable {

  override
  def read(kryo: Kryo, input: Input): Unit = {
    ping = kryo.readClassAndObject(input).asInstanceOf[FabricLastHourMetric]
    lav = kryo.readClassAndObject(input).asInstanceOf[FabricLastHourMetric]
    memory = kryo.readClassAndObject(input).asInstanceOf[FabricLastHourMetric]
    disk = kryo.readClassAndObject(input).asInstanceOf[FabricLastHourMetric]
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    kryo.writeClassAndObject(output, ping)
    kryo.writeClassAndObject(output, lav)
    kryo.writeClassAndObject(output, memory)
    kryo.writeClassAndObject(output, disk)
  }

}
