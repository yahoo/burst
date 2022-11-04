/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net

import org.burstsys.vitals.instrument._
import org.burstsys.vitals.logging._
import io.netty.handler.codec.{LengthFieldBasedFrameDecoder, LengthFieldPrepender}
import org.burstsys.vitals.properties.VitalsPropertyKey

package object message extends VitalsLogger {

  // what is maximum frame length - for error checking...
  val maxFrameLength: Long = 500 * MB

  assert(maxFrameLength < Int.MaxValue)

  // how far into header is length field
  val lengthFieldOffset: Int = 0

  // how many bytes to encode frame length
  val lengthFieldLength: Int = 4

  // A way to pass info in an acess info, but not too much
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

  case class FabricNetMsgType(code: Int)

  object FabricNetTetherMsgType extends FabricNetMsgType(1)

  object FabricNetAssessReqMsgType extends FabricNetMsgType(2)

  object FabricNetAssessRespMsgType extends FabricNetMsgType(3)

}
