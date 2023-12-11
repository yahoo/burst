/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave

import org.burstsys.fabric.net.message.FabricNetMsgType
import org.burstsys.vitals.logging._

package object message extends VitalsLogger {

  ////////////////////////////////////////////////////////////////////////////////
  // stream request/response
  ////////////////////////////////////////////////////////////////////////////////

  object FabricNetParticleReqMsgType extends FabricNetMsgType(100, "Particle Request")

  object FabricNetParticleRespMsgType extends FabricNetMsgType(101, "Particle Response")

  object FabricNetProgressMsgType extends FabricNetMsgType(102, "Progress")

  object FabricNetCacheOperationReqMsgType extends FabricNetMsgType(103, "Cache Operation Request")

  object FabricNetCacheOperationRespMsgType extends FabricNetMsgType(104, "Cache Operation Response")

  object FabricNetSliceFetchReqMsgType extends FabricNetMsgType(105, "Slice Fetch Request")

  object FabricNetSliceFetchRespMsgType extends FabricNetMsgType(106, "Slice Fetch Response")

}
