/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.model.execute.parameters

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.brio.types.BrioTypes
import org.burstsys.brio.types.BrioTypes.BrioTypeKey

/**
  * a single
  */
trait FabricParameter extends Any {

  /**
    * the name of this parameter
    *
    * @return
    */
  def name: String

  /**
    * the ''form'' of this parameter
    *
    * @return
    */
  def form: FabricParameterForm

  /**
    * the value brio type
    *
    * @return
    */
  def valueType: BrioTypeKey

  /**
    * the key brio type
    *
    * @return
    */
  def keyType: BrioTypeKey

}

abstract
class FabricParameterContext(var name: String, var form: FabricParameterForm, var valueType: BrioTypeKey, var keyType: BrioTypeKey)
  extends AnyRef with FabricParameter with KryoSerializable with Equals {

  override def toString: String = s"name=$name, form=$form, valueType=${BrioTypes.brioDataTypeNameFromKey(valueType)}, keyType=${BrioTypes.brioDataTypeNameFromKey(keyType)}"

  ///////////////////////////////////////////////////////////////////
  // equality
  ///////////////////////////////////////////////////////////////////

  final override
  def canEqual(that: Any): Boolean = that.isInstanceOf[FabricParameterContext]

  final override
  def hashCode(): BrioTypeKey = {
    ??? // TODO
  }

  final override
  def equals(obj: Any): Boolean = {
    ??? // TODO
  }

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    output.writeString(name)
    kryo.writeClassAndObject(output, form)
    output.writeInt(valueType)
    output.writeInt(keyType)
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    name = input.readString()
    form = kryo.readClassAndObject(input).asInstanceOf[FabricParameterForm]
    valueType = input.readInt()
    keyType = input.readInt()
  }

}
