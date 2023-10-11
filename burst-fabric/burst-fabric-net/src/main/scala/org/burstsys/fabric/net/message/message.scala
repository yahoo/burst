/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net

import io.netty.handler.codec.{LengthFieldBasedFrameDecoder, LengthFieldPrepender}
import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties.VitalsPropertyKey
import org.burstsys.vitals.reporter.instrument._

import java.util.concurrent.ConcurrentHashMap

package object message extends VitalsLogger {

  // what is maximum frame length - for error checking...
  private val maxFrameLength: Long = 500 * MB

  assert(maxFrameLength < Int.MaxValue)

  // how far into header is length field
  private val lengthFieldOffset: Int = 0

  // how many bytes to encode frame length
  private val lengthFieldLength: Int = 4

  val FabricAccessMonikerParameter = "burstsys.fabric.moniker"

  type AccessParameters = scala.collection.Map[VitalsPropertyKey, java.io.Serializable]


  /**
    * on the way in, decode a length prefix frame
    */
  final case
  class FabricNetInboundFrameDecoder()
    extends LengthFieldBasedFrameDecoder(maxFrameLength.toInt, lengthFieldOffset, lengthFieldLength)

  /**
    * on the way out - encode a length prefix frame
    */
  final case
  class FabricNetOutboundFrameEncoder() extends LengthFieldPrepender(lengthFieldLength)

  /**
   * An identifier for the message type.
   * 1-100 are reserved for burst-fabric-net
   * 100-500 are reserved for burst-fabric-net-wave
   * 500-600 are reserved for burst-samplesource
   * @param code the int sent over the wire, used to dispatch messages to the correct recipient
   */
  case class FabricNetMsgType(code: Int, name: String) {
    msgMap.putIfAbsent(code, this)

    override def equals(obj: Any): Boolean = {
      obj match {
        case that: FabricNetMsgType =>
          code == that.code
        case _ =>
          false
      }
    }
  }

  val msgMap: ConcurrentHashMap[Int, FabricNetMsgType] = new ConcurrentHashMap[Int, FabricNetMsgType]()

  def codeToMsgType(code: Int): FabricNetMsgType = {
    msgMap.get(code) match {
      case null =>
        FabricNetMsgType(code, s"message code=$code")
      case msgType =>
        msgType
    }
  }

  object FabricNetHeartbeatMsgType extends FabricNetMsgType(1, "Heartbeat")

  object FabricNetAssessReqMsgType extends FabricNetMsgType(2, "Assess Request")

  object FabricNetAssessRespMsgType extends FabricNetMsgType(3, "Assess Response")

  object FabricNetShutdownMsgType extends FabricNetMsgType(4, "Shutdown")

}
