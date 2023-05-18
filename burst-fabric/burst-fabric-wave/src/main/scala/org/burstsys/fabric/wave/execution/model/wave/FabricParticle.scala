/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.wave

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.wave.data.model.slice.FabricSlice
import org.burstsys.fabric.wave.execution.model.scanner.FabricScanner
import org.burstsys.vitals.uid.VitalsUid

/**
 * a single distributed unit of a scatter/gather  [[org.burstsys.fabric.execution.model.wave.FabricWave]] destined for
 * a single [[org.burstsys.fabric.topology.model.node.worker.FabricWorker]]. It consists of a partition of
 * data called a [[FabricSlice]] and a [[org.burstsys.fabric.execution.model.scanner.FabricScanner]] to execute against it.
 */
trait FabricParticle extends Any {

  /**
   * @return the guid for the wave this particle belongs to
   */
   def guid: VitalsUid

  /**
   * @return the data ''slice'' needed for this operation
   */
  def slice: FabricSlice

  /**
   * @return the scan that processes blobs and returns gathers that can be merged together
   */
  def scanner: FabricScanner

  /**
   * @return do we add useful instrumentation to this particle's execution (adds overhead)
   */
  def instrumented: Boolean = false

}

object FabricParticle {

  def apply(guid: VitalsUid, slice: FabricSlice, scanner: FabricScanner): FabricParticle =
    FabricParticleContext(guid, slice, scanner)

}

final case
class FabricParticleContext(var guid: VitalsUid, var slice: FabricSlice, var scanner: FabricScanner)
  extends KryoSerializable with FabricParticle {

  override def toString: String =
    s"""|
        |FabricParticle(
        |   slice=$slice
        |)""".stripMargin

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////


  override
  def write(kryo: Kryo, output: Output): Unit = {
    output.writeString(guid)
    kryo.writeClassAndObject(output, slice)
    kryo.writeClassAndObject(output, scanner)
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    guid = input.readString()
    slice = kryo.readClassAndObject(input).asInstanceOf[FabricSlice]
    scanner = kryo.readClassAndObject(input).asInstanceOf[FabricScanner]
  }


}
