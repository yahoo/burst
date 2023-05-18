/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.ops

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}

/**
 * please comment
 *
 * @param name
 * @param relation
 * @param lVal
 * @param dVal
 * @param bVal
 */
final case
class FabricCacheOpParameter(
                              var name: FabricCacheOpParameterName,
                              var relation: FabricCacheOpRelation,
                              var lVal: Long = 0,
                              var dVal: Double = 0.0,
                              var bVal: Boolean = false
                            ) extends KryoSerializable {

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    name = kryo.readClassAndObject(input).asInstanceOf[FabricCacheOpParameterName]
    relation = kryo.readClassAndObject(input).asInstanceOf[FabricCacheOpRelation]
    lVal = input.readLong()
    dVal = input.readDouble()
    bVal = input.readBoolean()
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    kryo.writeClassAndObject(output, name)
    kryo.writeClassAndObject(output, relation)
    output.writeLong(lVal)
    output.writeDouble(dVal)
    output.writeBoolean(bVal)
  }

}
