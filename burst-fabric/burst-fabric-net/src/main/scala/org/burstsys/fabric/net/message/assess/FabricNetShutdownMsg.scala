/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.message.assess

import org.burstsys.fabric.net.message.{FabricNetMsgContext, FabricNetShutdownMsgType}

object FabricNetShutdownMsg {
  def apply(buffer: Array[Byte]): Unit = {
    FabricNetShutdownMsg().decode(buffer)
  }
}

case class FabricNetShutdownMsg() extends
  FabricNetMsgContext(FabricNetShutdownMsgType)
