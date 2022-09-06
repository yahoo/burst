/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.kryo

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}

/**
  * A kryo serializable for stateless types
  */
trait VitalsKryoStatelessSerializable extends KryoSerializable {

  final override
  def read(kryo: Kryo, input: Input): Unit = {
  }

  final override
  def write(kryo: Kryo, output: Output): Unit = {
  }
}

/**
  * A pure interface version of the Kryo serializable interface
  * (be careful how you use this! Its not a real[[com.esotericsoftware.kryo.KryoSerializable]])
  */
trait VitalsKryoPureSerializable extends Any {
  def write(kryo: Kryo, output: Output): Unit

  def read(kryo: Kryo, input: Input): Unit
}
