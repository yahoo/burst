/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.wave

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.vitals.uid._

/**
 * a wave is the ''scatter'' part of the ''scatter/gather'' ''supervisor/worker'' algorithm.
 * It consists of a set of [[FabricParticle]] instances each of which is sent to a worker for execution
 */
trait FabricWave extends Any {

  /**
   *
   * @return
   */
  def guid: VitalsUid

  /**
   * the set of divisions to execute the scatter on
   *
   * @return
   */
  def particles: Array[FabricParticle]

}

/**
 *
 */
object FabricWave {

  def apply(guid: VitalsUid, particles: Array[FabricParticle] = Array.empty): FabricWave =
    FabricWaveContext(guid, particles)

}

final case
class FabricWaveContext(var guid: VitalsUid, var particles: Array[FabricParticle]) extends KryoSerializable with FabricWave {

  override def toString: VitalsUid = s"FabricWave(guid=$guid)"

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    particles = new Array[FabricParticle](input.readInt)
    guid = input.readString
    var i = 0
    while (i < particles.length) {
      particles(i) = kryo.readClassAndObject(input).asInstanceOf[FabricParticle]
      i += 1
    }
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    output writeInt particles.length
    output.writeString(guid)
    var i = 0
    while (i < particles.length) {
      kryo.writeClassAndObject(output, particles(i))
      i += 1
    }
  }
}
