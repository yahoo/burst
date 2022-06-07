/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.kryo

import java.io._

import com.esotericsoftware.kryo.KryoSerializable
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.vitals.kryo._

/**
  * Used to support a hand off from java serialization to Kryo serialization.
  * Use this on a top level object that is serialized using java serialization
  * but you really want to use kryo (really simple way to circumvent the maze
  * of spark local/distributed and in/out serialization mess...
 * @deprecated spark no more
  */
trait VitalsKryoExternalizable extends KryoSerializable with Externalizable with Serializable {

  /**
    * @see java.io.Externalizable
    * @param in
    */
  final override
  def readExternal(in: ObjectInput): Unit = {
    val is = in.asInstanceOf[ObjectInput]
    readKryoExternal(is)
  }

  /**
    * @see java.io.Externalizable
    * @param out
    */
  final override
  def writeExternal(out: ObjectOutput): Unit = {
    val os = out.asInstanceOf[ObjectOutput]
    writeKryoExternal(os)
  }

  /**
    * Delegate from Java Serialization to Kryo
    *
    * @param os
    */
  final private def writeKryoExternal(os: ObjectOutput): Unit = {
    val kryo = acquireKryo
    try {
      val bos = new ByteArrayOutputStream()
      val out = new Output(bos)
      write(kryo, out)
      out.close()
      val buff = bos.toByteArray
      os writeInt buff.length
      os.write(buff)
    } finally releaseKryo(kryo)
  }

  /**
    * Delegate from Java De-serialization to Kryo
    *
    * @param in
    * @throws java.io.IOException
    */
  @throws[IOException]
  final private def readKryoExternal(in: ObjectInput): Unit = {
    val size = in.readInt
    val buffer = new Array[Byte](size)
    in.readFully(buffer)
    val kryo = acquireKryo
    try {
      read(kryo, new Input(buffer))
    } finally releaseKryo(kryo)
  }

}
