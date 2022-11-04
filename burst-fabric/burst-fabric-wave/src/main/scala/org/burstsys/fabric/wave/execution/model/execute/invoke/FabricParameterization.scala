/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.execute.invoke

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.brio.types.BrioTypes
import org.burstsys.brio.types.BrioTypes.{BrioDataType, BrioTypeKey}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

/**
  * The set of parameters passed to a particular felt invocation
  */
trait FabricParameterization {
  def hasScalarWithType(name: String, key: BrioTypeKey): Boolean

  def hasVectorWithType(name: String, key: BrioTypeKey): Boolean

  def setBoolean(name: String, value: Boolean): Unit

  def setBooleans(name: String, value: Array[Boolean]): Unit

  def setByte(name: String, value: Byte): Unit

  def setBytes(name: String, value: Array[Byte]): Unit

  def setShort(name: String, value: Short): Unit

  def setShorts(name: String, value: Array[Short]): Unit

  def setInt(name: String, value: Int): Unit

  def setInts(name: String, value: Array[Int]): Unit

  def setLong(name: String, value: Long): Unit

  def setLongs(name: String, value: Array[Long]): Unit

  def setDouble(name: String, value: Double): Unit

  def setDoubles(name: String, value: Array[Double]): Unit

  def setString(name: String, value: String): Unit

  def setStrings(name: String, value: Array[String]): Unit

  def getBoolean(name: String): Boolean

  def getBooleans(name: String): Array[Boolean]

  def getByte(name: String): Byte

  def getBytes(name: String): Array[Byte]

  def getShort(name: String): Short

  def getShorts(name: String): Array[Short]

  def getInt(name: String): Int

  def getInts(name: String): Array[Int]

  def getLong(name: String): Long

  def getLongs(name: String): Array[Long]

  def getDouble(name: String): Double

  def getDoubles(name: String): Array[Double]

  def getString(name: String): String

  def getStrings(name: String): Array[String]

}

object FabricParameterization {

  def apply(): FabricParameterization =
    FabricParameterizationContext()

}

final case
class FabricParameterizationContext() extends KryoSerializable with FabricParameterization {


  ///////////////////////////////////////////////////////////////////
  // state
  ///////////////////////////////////////////////////////////////////

  override
  def toString: String =
    s"""FabricParameterization(
       |
     |)""".stripMargin

  private[this]
  var parameters = new mutable.HashMap[String, (BrioTypeKey, Int, Array[_ <: BrioDataType])]()

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////


  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    parameters = new mutable.HashMap[String, (BrioTypeKey, Int, Array[_ <: BrioDataType])]()
    parameters.clear()
    val psz = input.readInt
    for (j <- 0 until psz) {
      val name = input.readString
      val key = input.readInt
      val sz = input.readInt
      val v: Array[BrioDataType] = key match {
        case BrioTypes.BrioBooleanKey => inputVector[Boolean](input, sz, _.readBoolean)
        case BrioTypes.BrioByteKey => inputVector[Byte](input, sz, _.readByte)
        case BrioTypes.BrioShortKey => inputVector[Short](input, sz, _.readShort)
        case BrioTypes.BrioIntegerKey => inputVector[Int](input, sz, _.readInt)
        case BrioTypes.BrioLongKey => inputVector[Long](input, sz, _.readLong)
        case BrioTypes.BrioDoubleKey => inputVector[Double](input, sz, _.readDouble)
        case BrioTypes.BrioStringKey => inputVector[String](input, sz, _.readString)
      }

      parameters.put(name, (key, sz, v))
    }
  }

  def inputVector[T <: BrioDataType](input: Input, sz: Int, f: Input => T): Array[BrioDataType] = {
    val v: ArrayBuffer[T] = ArrayBuffer[T]()

    for (i <- 0 until sz.max(1)) {
      v.append(f(input))
    }
    v.toArray
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    output.writeInt(parameters.size)
    for ((name, (key, sz, v)) <- parameters) {
      output.writeString(name)
      output.writeInt(key)
      output.writeInt(sz)
      for (i <- 0 until sz.max(1)) {
        key match {
          case BrioTypes.BrioBooleanKey => output.writeBoolean(v(i).asInstanceOf[Boolean])
          case BrioTypes.BrioByteKey => output.writeByte(v(i).asInstanceOf[Byte])
          case BrioTypes.BrioShortKey => output.writeShort(v(i).asInstanceOf[Short])
          case BrioTypes.BrioIntegerKey => output.writeInt(v(i).asInstanceOf[Int])
          case BrioTypes.BrioLongKey => output.writeLong(v(i).asInstanceOf[Long])
          case BrioTypes.BrioDoubleKey => output.writeDouble(v(i).asInstanceOf[Double])
          case BrioTypes.BrioStringKey => output.writeString(v(i).asInstanceOf[String])
        }
      }
    }

  }

  override def hasScalarWithType(name:String, key:BrioTypeKey): Boolean = {
    val (tk, sz, _) = parameters.getOrElse(name, (BrioTypes.BrioAnyTypeKey, -1, null))
    tk == key && sz == 0
  }

  override def hasVectorWithType(name:String, key:BrioTypeKey): Boolean = {
    val (tk, sz, _) = parameters.getOrElse(name, (BrioTypes.BrioAnyTypeKey, -1, null))
    tk == key && sz > 0
  }

  override def setBoolean(name: String, value: Boolean): Unit = parameters.put(name, (BrioTypes.BrioBooleanKey, 0, Array(value)))

  override def setBooleans(name: String, value: Array[Boolean]): Unit = parameters.put(name, (BrioTypes.BrioBooleanKey, value.length, value))

  override def setByte(name: String, value: Byte): Unit = parameters.put(name, (BrioTypes.BrioByteKey, 0, Array(value)))

  override def setBytes(name: String, value: Array[Byte]): Unit = parameters.put(name, (BrioTypes.BrioByteKey, value.length, value))

  override def setShort(name: String, value: Short): Unit = parameters.put(name, (BrioTypes.BrioShortKey, 0, Array(value)))

  override def setShorts(name: String, value: Array[Short]): Unit = parameters.put(name, (BrioTypes.BrioShortKey, value.length, value))

  override def setInt(name: String, value: Int): Unit = parameters.put(name, (BrioTypes.BrioIntegerKey, 0, Array(value)))

  override def setInts(name: String, value: Array[Int]): Unit = parameters.put(name, (BrioTypes.BrioIntegerKey, value.length, value))

  override def setLong(name: String, value: Long): Unit = parameters.put(name, (BrioTypes.BrioLongKey, 0, Array(value)))

  override def setLongs(name: String, value: Array[Long]): Unit = parameters.put(name, (BrioTypes.BrioLongKey, value.length, value))

  override def setDouble(name: String, value: Double): Unit = parameters.put(name, (BrioTypes.BrioDoubleKey, 0, Array(value)))

  override def setDoubles(name: String, value: Array[Double]): Unit = parameters.put(name, (BrioTypes.BrioDoubleKey, value.length, value))

  override def setString(name: String, value: String): Unit = parameters.put(name, (BrioTypes.BrioStringKey, 0, Array(value)))

  override def setStrings(name: String, value: Array[String]): Unit = parameters.put(name, (BrioTypes.BrioStringKey, value.length, value))

  override def getBoolean(name: String): Boolean = readScalar(name, BrioTypes.BrioBooleanKey)

  override def getBooleans(name: String): Array[Boolean] = readVector(name, BrioTypes.BrioBooleanKey).toArray

  override def getByte(name: String): Byte = readScalar(name, BrioTypes.BrioByteKey)

  override def getBytes(name: String): Array[Byte] = readVector(name, BrioTypes.BrioByteKey).toArray

  override def getShort(name: String): Short = readScalar(name, BrioTypes.BrioShortKey)

  override def getShorts(name: String): Array[Short] = readVector(name, BrioTypes.BrioShortKey).toArray

  override def getInt(name: String): Int = readScalar(name, BrioTypes.BrioIntegerKey)

  override def getInts(name: String): Array[Int] = readVector(name, BrioTypes.BrioIntegerKey).toArray

  override def getLong(name: String): Long = readScalar(name, BrioTypes.BrioDoubleKey)

  override def getLongs(name: String): Array[Long] = readVector(name, BrioTypes.BrioDoubleKey).toArray

  override def getDouble(name: String): Double = readScalar(name, BrioTypes.BrioDoubleKey)

  override def getDoubles(name: String): Array[Double] = readVector(name, BrioTypes.BrioDoubleKey).toArray

  override def getString(name: String): String = readScalar(name, BrioTypes.BrioStringKey)

  override def getStrings(name: String): Array[String] = readVector(name, BrioTypes.BrioStringKey).toArray

  def readScalar[T:ClassTag](name:String, key: BrioTypeKey): T =
    parameters.get(name) match {
      case None => throw new NoSuchElementException
      case Some((`key`, 0, v: Array[_])) => v.head.asInstanceOf[T]
      case _ => throw new IllegalAccessError
    }

  def readVector[T:ClassTag](name:String, key: BrioTypeKey): mutable.ArraySeq[T] =
    parameters.get(name) match {
      case None => null
      case Some((`key`, sz, va: Array[_])) if sz > 0 =>
        va.map{case t:T => t}
      case _ => throw new IllegalAccessError
    }
}
