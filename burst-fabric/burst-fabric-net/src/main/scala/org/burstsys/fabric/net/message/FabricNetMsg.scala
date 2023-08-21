/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.message

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import io.netty.buffer.ByteBuf
import org.burstsys.fabric.net.{FabricNetMessageId, fabricKryoOutputBufferInitialSize, fabricKryoOutputBufferMaxSize}
import org.burstsys.fabric.topology.model.node.FabricNode
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.kryo.{acquireKryo, releaseKryo}

import scala.collection.mutable

/**
 * base trait for all fabric protocol messages
 */
trait FabricNetMsg extends AnyRef {

  /**
   * @return the id of this message
   */
  def messageId: FabricNetMessageId

  /**
   * @return Node key for the sending node
   */
  def senderKey: FabricNode

  /**
   * @return Node key for the receiving node
   */
  def receiverKey: FabricNode

  /**
   * Translate between Netty and Kryo Worlds
   *
   * @param buffer the buffer to decode from
   * @return this
   */
  def decode(buffer: Array[Byte]): this.type

  /**
   * Translate between Netty and Kryo Worlds
   *
   * @param buffer the buffer to encode into
   * @return this
   */
  def encode(buffer: ByteBuf): this.type

  /**
   * Show that this message is linked to another message
   *
   * @param msg the message to link to
   * @return this
   */
  def link(msg: FabricNetMsg): this.type

}


/**
 * An abstract message within the FabricNet Protocol
 *
 * @group FabricNetMsg
 */
abstract class FabricNetMsgContext(val messageType: FabricNetMsgType)
  extends AnyRef with FabricNetMsg with KryoSerializable {

  override def toString: String = s"${getClass.getSimpleName.stripSuffix("$")} messageId=$messageId"

  ////////////////////////////////////////////////////////////////////////////////////
  // State
  ////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _messageId: FabricNetMessageId = _

  private[this]
  var _senderKey: FabricNode = _

  private[this]
  var _receiverKey: FabricNode = _

  ////////////////////////////////////////////////////////////////////////////////////
  // Accessors
  ////////////////////////////////////////////////////////////////////////////////////

  /**
   * connection unique request id
   *
   * @return
   */
  final def messageId: FabricNetMessageId = _messageId

  /**
   * connection unique id
   */
  final def messageId_=(id: FabricNetMessageId): Unit = _messageId = id

  /**
   * Msg sender key
   *
   * @return
   */
  final def senderKey: FabricNode = _senderKey

  final def senderKey_=(key: FabricNode): Unit = _senderKey = key

  /**
   * Msg receiver key
   *
   * @return
   */
  final def receiverKey: FabricNode = _receiverKey

  final def receiverKey_=(key: FabricNode): Unit = _receiverKey = key

  ////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  ////////////////////////////////////////////////////////////////////////////////////

  override
  def link(msg: FabricNetMsg): this.type = {
    _messageId = msg.messageId
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    _messageId = input.readLong
    _senderKey = kryo.readClassAndObject(input).asInstanceOf[FabricNode]
    _receiverKey = kryo.readClassAndObject(input).asInstanceOf[FabricNode]
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    output.writeLong(_messageId)
    kryo.writeClassAndObject(output, _senderKey)
    kryo.writeClassAndObject(output, _receiverKey)
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // CODEC
  ////////////////////////////////////////////////////////////////////////////////////

  protected def readMap[T](kryo: Kryo, input: Input): Map[String, T] = {
    val sz = input.readInt()
    val m = mutable.Map[String, T]()
    for (_ <- 0 until sz) {
      val k = input.readString()
      val e = kryo.readClassAndObject(input).asInstanceOf[T]
      m.put(k, e)
    }
    m.toMap
  }

  protected def writeMap[T](kryo: Kryo, output: Output, map: Map[String, T]): Unit = {
    output.writeInt(map.size)
    for ((k, e) <- map) {
      output.writeString(k)
      kryo.writeClassAndObject(output, e)
    }
  }

  /**
   * Read this message's contents from the netty buffer
   *
   * @param buffer the netty buffer contents
   * @return this message
   */
  final def decode(buffer: Array[Byte]): this.type = {
    // the message type is already read
    val k = acquireKryo
    try {
      try {
        val input = new Input(buffer)
        this.read(k, input)
      } finally
        releaseKryo(k)
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
    this
  }

  /**
   * Write this message into a netty buffer
   *
   * @param buffer the netty buffer to encode into
   * @return this message
   */
  final def encode(buffer: ByteBuf): this.type = {
    try {
      buffer.writeInt(messageType.code) // write the message type
      val k = acquireKryo
      try {
        val output = {
          new Output(fabricKryoOutputBufferInitialSize.toInt, fabricKryoOutputBufferMaxSize.toInt)
        }
        this.write(k, output)
        buffer.writeBytes(output.toBytes)
      } finally
        releaseKryo(k)
    } catch safely {
      case t: Throwable =>
        throw VitalsException(t)
    }
    this
  }

}
