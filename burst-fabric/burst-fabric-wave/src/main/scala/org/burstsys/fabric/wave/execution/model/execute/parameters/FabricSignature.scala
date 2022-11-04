/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.execute.parameters

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}

import scala.reflect.ClassTag

/**
  * metadata associated with a set of parameters
  */
trait FabricSignature[PT <: FabricParameter] extends KryoSerializable {

  /**
    * the set of [[FabricParameter]] types in this [[FabricSignature]]
    *
    * @return
    */
  def parameters: Array[PT]

  /**
    * true if a given parameter name is in this signature
    * @param name
    * @return
    */
  def extantParameter(name: String): Boolean

  /**
    * validate (match) this signature against another [[FabricSignature]] (or [[FabricCall]]
    *
    * @param signature
    * @return
    */
  def validate(signature: FabricSignature[_]): Boolean

}

object FabricSignature {

  def apply(): FabricSignature[FabricParameterType] = new FabricSignatureContext[FabricParameterType](Array.empty) {
    override protected def allocateParameterArray(size: Int): Array[FabricParameterType] = new Array[FabricParameterType](size)
  }

  def apply(parameters: Array[FabricParameterType]): FabricSignature[FabricParameterType] =
    new FabricSignatureContext[FabricParameterType](parameters: Array[FabricParameterType]) {
      override protected def allocateParameterArray(size: Int): Array[FabricParameterType] = new Array[FabricParameterType](size)
    }

  def apply(parameters: FabricParameterType*): FabricSignature[FabricParameterType] =
    new FabricSignatureContext[FabricParameterType](parameters.toArray) {
      override protected def allocateParameterArray(size: Int): Array[FabricParameterType] = new Array[FabricParameterType](size)
    }

}

abstract class FabricSignatureContext[PT <: FabricParameter : ClassTag](final var parameters: Array[PT]) extends FabricSignature[PT]
  with KryoSerializable {

  def this() = this(Array.empty)

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  final override def extantParameter(name: String): Boolean = parameters.exists(_.name == name)

  protected def allocateParameterArray(size: Int): Array[PT]

  final override def validate(signature: FabricSignature[_]): Boolean = {
    if (parameters.length != signature.parameters.length) return false
    found(signature)
  }

  private def found(signature: FabricSignature[_]): Boolean = {
    signature.parameters.foreach {
      case thisP: FabricParameter => this.parameters.foreach {
        thatP: FabricParameter => if (thisP.name == thatP.name) return true
      }
    }
    false
  }

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def write(kryo: Kryo, output: Output): Unit = {
    output.writeInt(parameters.size)
    parameters.foreach(kryo.writeClassAndObject(output, _))
  }

  override
  def read(kryo: Kryo, input: Input): Unit = {
    val size = input.readInt
    val tmp = allocateParameterArray(size)
    var i = 0
    while (i < size) {
      val pt = kryo.readClassAndObject(input).asInstanceOf[PT]
      tmp(i) = pt
      i += 1
    }
    parameters = tmp
  }

}
