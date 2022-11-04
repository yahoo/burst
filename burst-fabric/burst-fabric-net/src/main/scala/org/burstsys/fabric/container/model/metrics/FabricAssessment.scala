/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.model.metrics

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.net.message.AccessParameters

import scala.collection.mutable

/**
  * A message that is sent from [[org.burstsys.fabric.topology.model.node.worker.FabricWorker]] to
 * [[org.burstsys.fabric.topology.model.node.supervisor.FabricSupervisor]] to provide a coarse grained
 * overall 'health' and 'performance' metric that can be used to determine if a worker is alive and
 * appropriately responsive/healthy.
  */
final case
class FabricAssessment(
                        var ping: FabricLastHourMetric,
                        var lav: FabricLastHourMetric,
                        var memory: FabricLastHourMetric,
                        var disk: FabricLastHourMetric,
                        var parameters:  AccessParameters
                      ) extends KryoSerializable {

  override
  def read(kryo: Kryo, input: Input): Unit = {
    ping = kryo.readClassAndObject(input).asInstanceOf[FabricLastHourMetric]
    lav = kryo.readClassAndObject(input).asInstanceOf[FabricLastHourMetric]
    memory = kryo.readClassAndObject(input).asInstanceOf[FabricLastHourMetric]
    disk = kryo.readClassAndObject(input).asInstanceOf[FabricLastHourMetric]
    val sz = input.readInt()
    val m = mutable.Map[String, java.io.Serializable]()
    for (_ <- 0 until sz) {
      val k = input.readString()
      val e = kryo.readClassAndObject(input).asInstanceOf[java.io.Serializable]
      m.put(k, e)
    }
    parameters = m.toMap
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    kryo.writeClassAndObject(output, ping)
    kryo.writeClassAndObject(output, lav)
    kryo.writeClassAndObject(output, memory)
    kryo.writeClassAndObject(output, disk)
    output.writeInt(parameters.size)
    for ((k, e) <- parameters) {
      output.writeString(k)
      kryo.writeClassAndObject(output, e)
    }
  }

}
