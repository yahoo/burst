/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave

import org.burstsys.fabric.net.message.FabricNetMsgType
import org.burstsys.vitals.logging._

package object message extends VitalsLogger {

  ////////////////////////////////////////////////////////////////////////////////
  // stream request/response
  ////////////////////////////////////////////////////////////////////////////////

  object FabricNetParticleReqMsgType extends FabricNetMsgType(12)

  object FabricNetParticleRespMsgType extends FabricNetMsgType(13)

  object FabricNetProgressMsgType extends FabricNetMsgType(15)

  object FabricNetCacheOperationReqMsgType extends FabricNetMsgType(16)

  object FabricNetCacheOperationRespMsgType extends FabricNetMsgType(17)

  object FabricNetSliceFetchReqMsgType extends FabricNetMsgType(18)

  object FabricNetSliceFetchRespMsgType extends FabricNetMsgType(19)

}
