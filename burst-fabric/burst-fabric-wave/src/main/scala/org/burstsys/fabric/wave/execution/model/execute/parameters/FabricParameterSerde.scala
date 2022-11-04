/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.execute.parameters

import com.esotericsoftware.kryo.KryoSerializable
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.types.BrioTypes
import org.burstsys.brio.types.BrioTypes.{BrioDataType, BrioTypeKey}
import org.burstsys.vitals.errors.VitalsException

import scala.collection.mutable
import scala.reflect.ClassTag

trait FabricParameterSerde extends KryoSerializable {

  self: FabricParameterValueContext =>

  ///////////////////////////////////////////////////////////////////
  // scalars
  ///////////////////////////////////////////////////////////////////

  protected def writeScalar(output: Output): Unit = {
    valueType match {
      case BrioTypes.BrioBooleanKey => output.writeBoolean(data.asInstanceOf[Boolean])
      case BrioTypes.BrioByteKey => output.writeByte(data.asInstanceOf[Byte])
      case BrioTypes.BrioShortKey => output.writeShort(data.asInstanceOf[Short])
      case BrioTypes.BrioIntegerKey => output.writeInt(data.asInstanceOf[BrioTypeKey])
      case BrioTypes.BrioLongKey => output.writeLong(data.asInstanceOf[Long])
      case BrioTypes.BrioDoubleKey => output.writeDouble(data.asInstanceOf[Double])
      case BrioTypes.BrioStringKey => output.writeString(data.asInstanceOf[String])
      case _ => ???
    }
  }

  protected def readScalar(input: Input): Unit = {
    valueType match {
      case BrioTypes.BrioBooleanKey => data = input.readBoolean
      case BrioTypes.BrioByteKey => data = input.readByte()
      case BrioTypes.BrioShortKey => data = input.readShort()
      case BrioTypes.BrioIntegerKey => data = input.readInt()
      case BrioTypes.BrioLongKey => data = input.readLong()
      case BrioTypes.BrioDoubleKey => data = input.readDouble()
      case BrioTypes.BrioStringKey => data = input.readString()
      case _ => ???
    }
  }

  ///////////////////////////////////////////////////////////////////
  // vectors
  ///////////////////////////////////////////////////////////////////

  protected def readVector(input: Input): Unit = {
    val size = input.readInt
    valueType match {
      case BrioTypes.BrioBooleanKey =>
        val tmp = new Array[Boolean](size)
        for (i <- 0 until size) tmp(i) = input.readBoolean
        data = tmp
      case BrioTypes.BrioByteKey =>
        val tmp = new Array[Boolean](size)
        for (i <- 0 until size) tmp(i) = input.readBoolean()
        data = tmp
      case BrioTypes.BrioShortKey =>
        val tmp = new Array[Short](size)
        for (i <- 0 until size) tmp(i) = input.readShort()
        data = tmp
      case BrioTypes.BrioIntegerKey =>
        val tmp = new Array[Int](size)
        for (i <- 0 until size) tmp(i) = input.readInt()
        data = tmp
      case BrioTypes.BrioLongKey =>
        val tmp = new Array[Long](size)
        for (i <- 0 until size) tmp(i) = input.readLong()
        data = tmp
      case BrioTypes.BrioDoubleKey =>
        val tmp = new Array[Double](size)
        for (i <- 0 until size) tmp(i) = input.readDouble()
        data = tmp
      case BrioTypes.BrioStringKey =>
        val tmp = new Array[String](size)
        for (i <- 0 until size) tmp(i) = input.readString()
        data = tmp
      case _ => ???
    }
  }

  protected def writeVector(output: Output): Unit = {
    valueType match {
      case BrioTypes.BrioBooleanKey =>
        val array = data.asInstanceOf[Array[Boolean]]
        output.writeInt(array.length)
        array.foreach(output.writeBoolean)
      case BrioTypes.BrioByteKey =>
        val array = data.asInstanceOf[Array[Byte]]
        output.writeInt(array.length)
        array.foreach(output.writeByte)
      case BrioTypes.BrioShortKey =>
        val array = data.asInstanceOf[Array[Short]]
        output.writeInt(array.length)
        array.foreach(s => output.writeShort(s))
      case BrioTypes.BrioIntegerKey =>
        val array = data.asInstanceOf[Array[Int]]
        output.writeInt(array.length)
        array.foreach(output.writeInt)
      case BrioTypes.BrioLongKey =>
        val array = data.asInstanceOf[Array[Long]]
        output.writeInt(array.length)
        array.foreach(output.writeLong)
      case BrioTypes.BrioDoubleKey =>
        val array = data.asInstanceOf[Array[Double]]
        output.writeInt(array.length)
        array.foreach(output.writeDouble)
      case BrioTypes.BrioStringKey =>
        val array = data.asInstanceOf[Array[String]]
        output.writeInt(array.length)
        array.foreach(output.writeString)
      case _ => ???
    }
  }

  ///////////////////////////////////////////////////////////////////
  // maps
  ///////////////////////////////////////////////////////////////////

  protected def readMap(input: Input): Unit = {
    (keyType, valueType) match {
      case (BrioTypes.BrioStringKey, BrioTypes.BrioStringKey) => data = MapReader[String, String](input, _.readString, _.readString).read
      // TODO flesh out rest
      case _ => throw VitalsException(s"key=${BrioTypes.brioDataTypeNameFromKey(keyType)}, value=${BrioTypes.brioDataTypeNameFromKey(valueType)} not implemented")
    }
  }

  protected def writeMap(output: Output): Unit = {
    (keyType, valueType) match {
      case (BrioTypes.BrioStringKey, BrioTypes.BrioStringKey) => MapWriter[String, String](data, output, output.writeString, output.writeString).write
      // TODO flesh out rest
      case _ => throw VitalsException(s"key=${BrioTypes.brioDataTypeNameFromKey(keyType)}, value=${BrioTypes.brioDataTypeNameFromKey(valueType)} not implemented")
    }
  }

}

private final case
class MapReader[KT <: BrioDataType : ClassTag, VT <: BrioDataType : ClassTag](input: Input, key: Input => KT, value: Input => VT) {
  def read: Map[KT, VT] = {
    val size = input.readInt
    val tmp = new mutable.HashMap[KT, VT]
    for (i <- 0 until size) {
      tmp += key(input) -> value(input)
    }
    tmp.toMap
  }
}

private final case
class MapWriter[KT <: BrioDataType : ClassTag, VT <: BrioDataType : ClassTag](data: Any, output: Output, key: KT => Unit, value: VT => Unit) {
  def write: this.type = {
    val map = data.asInstanceOf[Map[KT, VT]]
    output.writeInt(map.size)
    map.foreach {
      case (k, v) =>
        key(k)
        value(v)
    }
    this
  }
}

