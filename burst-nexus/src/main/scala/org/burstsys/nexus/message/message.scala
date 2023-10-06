/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.{LengthFieldBasedFrameDecoder, LengthFieldPrepender}
import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties.VitalsPropertyMap
import org.burstsys.vitals.reporter.instrument._

import java.util
import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

package object message extends VitalsLogger {

  // what is maximum frame length - for error checking...
  val maxFrameLength: Long = 500 * MB

  assert(maxFrameLength < Int.MaxValue)

  // how far into header is length field
  private val lengthFieldOffset: Int = 0

  // how many bytes to encode frame length
  private val lengthFieldLength: Int = 4

  /**
    * on the way in, decode a length prefix frame
    */
  final case
  class NexusInboundFrameDecoder()
    extends LengthFieldBasedFrameDecoder(maxFrameLength.toInt, lengthFieldOffset, lengthFieldLength)

  /**
    * on the way out - encode a length prefix frame
    */
  final case
  class NexusOutboundFrameEncoder() extends LengthFieldPrepender(lengthFieldLength)

  def msgIds(msg: NexusMsg): String = s"message(msg_guid=${msg.guid} msg_suid=${msg.suid})"

  private val msgMap = new ConcurrentHashMap[Int, NexusMsgType]()
  sealed case class NexusMsgType(code: Int, name: String) {
    msgMap.put(code, this)
  }

  def codeToMsg(code: Int): NexusMsgType = {
    msgMap.get(code) match {
      case null =>
        log warn burstLocMsg(s"unknown message type $code")
        NexusMsgType(code, s"unknown message type $code")
      case msgType =>
        msgType
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  // stream request/response
  ////////////////////////////////////////////////////////////////////////////////

  /**
    * sent from the client to the server to request the start of a stream
    * The response to this is [[NexusStreamInitiatedMsgType]]
    */
  object NexusStreamInitiateMsgType extends NexusMsgType(1, "Initiate Stream Request")

  /**
    * send from the server back to the client to acknowledge the stream request
    * This is the response to a [[NexusStreamInitiateMsgType]]
    */
  object NexusStreamInitiatedMsgType extends NexusMsgType(2, "Initiate Stream Response")

  ////////////////////////////////////////////////////////////////////////////////
  // stream chunks
  ////////////////////////////////////////////////////////////////////////////////

  /**
    * sent from the server to the client as parcels in the stream
    */
  object NexusStreamParcelMsgType extends NexusMsgType(3, "Stream Parcel")

  /**
    * sent from the server to the client when the stream is complete
    */
  object NexusStreamCompleteMsgType extends NexusMsgType(4, "Stream Complete")

  /**
    * sent from the client to the server when the stream is no longer desired
    */
  object NexusStreamAbortMsgType extends NexusMsgType(5,  "Stream Abort")

  /**
    * sent from server to client to indicate the server is still active
    */
  object NexusStreamHeartbeatMsgType extends NexusMsgType(6, "Stream Heartbeat")


  ////////////////////////////////////////////////////////////////////////////////
  // decode helpers
  ////////////////////////////////////////////////////////////////////////////////

  def decodeAsciiStringFromByteBuf(buffer: ByteBuf): String = {
    val length = buffer.readByte
    val bytes = new Array[Byte](length)
    buffer.readBytes(bytes)
    new String(bytes)
  }

  def encodeAsciiStringToByteBuf(s: String, buffer: ByteBuf): Unit = {
    val bytes = s.getBytes()
    buffer.writeByte(bytes.length)
    buffer.writeBytes(bytes)
  }

  def decodeUtf8StringFromByteBuf(buffer: ByteBuf): String = {
    val length = buffer.readInt
    val bytes = new Array[Byte](length)
    buffer.readBytes(bytes)
    new String(bytes, "UTF8")
  }

  def encodeUtf8StringToByteBuf(s: String, buffer: ByteBuf): Unit = {
    val bytes = s.getBytes("UTF8")
    buffer.writeInt(bytes.length)
    buffer.writeBytes(bytes)
  }

  def decodePropertyMapToByteBuf(buffer: ByteBuf): VitalsPropertyMap = {
    val result = new util.HashMap[String, String]
    val length = buffer.readInt
    var i = 0
    while (i < length) {
      result put (decodeUtf8StringFromByteBuf(buffer) , decodeUtf8StringFromByteBuf(buffer))
      i += 1
    }
    result.asScala.toMap
  }

  def encodePropertyMapFromByteBuf(properties: VitalsPropertyMap, buffer: ByteBuf): Unit = {
    buffer.writeInt(properties.size)
    properties foreach {
      case (k, v) =>
        encodeUtf8StringToByteBuf(k, buffer)
        encodeUtf8StringToByteBuf(v, buffer)
    }
  }

  def decodeOptionalUtf8StringFromByteBuf(buffer: ByteBuf): Option[String] = {
    val length = buffer.readInt
    length match {
      case 0 => None
      case _ =>
        val bytes = new Array[Byte](length)
        buffer.readBytes(bytes)
        Some(new String(bytes, "UTF8"))
    }
  }

  def encodeOptionalUtf8StringToByteBuf(s: Option[String], buffer: ByteBuf): Unit = {
    val bytes = s match {
      case Some(str) => str.getBytes("UTF8")
      case _ => "".getBytes("UTF8")
    }
    buffer.writeInt(bytes.length)
    buffer.writeBytes(bytes)
  }

}
