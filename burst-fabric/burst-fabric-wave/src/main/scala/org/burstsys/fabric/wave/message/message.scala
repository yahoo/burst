/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave

import org.burstsys.fabric.net.message.FabricNetMsgType
import org.burstsys.vitals.logging._

package object message extends VitalsLogger {

  ////////////////////////////////////////////////////////////////////////////////
  // stream request/response
  ////////////////////////////////////////////////////////////////////////////////

  object FabricNetParticleReqMsgType extends FabricNetMsgType(100)

  object FabricNetParticleRespMsgType extends FabricNetMsgType(101)

  object FabricNetProgressMsgType extends FabricNetMsgType(102)

  object FabricNetCacheOperationReqMsgType extends FabricNetMsgType(103)

  object FabricNetCacheOperationRespMsgType extends FabricNetMsgType(104)

  object FabricNetSliceFetchReqMsgType extends FabricNetMsgType(105)

  object FabricNetSliceFetchRespMsgType extends FabricNetMsgType(106)

}
