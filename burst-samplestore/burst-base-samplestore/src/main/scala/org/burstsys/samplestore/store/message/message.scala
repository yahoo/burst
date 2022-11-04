/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.store

import org.burstsys.fabric.net.message.FabricNetMsgType
import org.burstsys.vitals.logging._

package object message extends VitalsLogger {

  ////////////////////////////////////////////////////////////////////////////////
  // metadata request/response
  ////////////////////////////////////////////////////////////////////////////////
  object FabricStoreMetadataReqMsgType extends FabricNetMsgType(51)

  object FabricStoreMetadataRespMsgType extends FabricNetMsgType(52)

}
