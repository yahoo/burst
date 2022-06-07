/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net

import org.burstsys.vitals.instrument._
import org.burstsys.vitals.logging._
import io.netty.handler.codec.{LengthFieldBasedFrameDecoder, LengthFieldPrepender}

package object message extends VitalsLogger {

  // what is maximum frame length - for error checking...
  val maxFrameLength: Long = 500 * MB

  assert(maxFrameLength < Int.MaxValue)

  // how far into header is length field
  val lengthFieldOffset: Int = 0

  // how many bytes to encode frame length
  val lengthFieldLength: Int = 4

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

  sealed case class FabricNetMsgType(code: Int)

  ////////////////////////////////////////////////////////////////////////////////
  // stream request/response
  ////////////////////////////////////////////////////////////////////////////////

  object FabricNetParticleReqMsgType extends FabricNetMsgType(2)

  object FabricNetParticleRespMsgType extends FabricNetMsgType(3)

  object FabricNetAssessReqMsgType extends FabricNetMsgType(4)

  object FabricNetAssessRespMsgType extends FabricNetMsgType(5)

  object FabricNetTetherMsgType extends FabricNetMsgType(8)

  object FabricNetProgressMsgType extends FabricNetMsgType(15)

  object FabricNetCacheOperationReqMsgType extends FabricNetMsgType(16)

  object FabricNetCacheOperationRespMsgType extends FabricNetMsgType(17)

  object FabricNetSliceFetchReqMsgType extends FabricNetMsgType(18)

  object FabricNetSliceFetchRespMsgType extends FabricNetMsgType(19)

}
