/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net

import io.netty.channel.Channel
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort}

import java.net.InetSocketAddress

/**
  * useful stuff for netty channels
  */
trait FabricNetLink extends Any {

  /**
    * The netty channel associated with this connection
    */
  def channel: Channel

  /**
    * Is the netty channel usable
    */
  final def isConnected: Boolean = {
    channel != null && channel.isRegistered && channel.isActive
  }

  /**
    * remote address for this connection
    */
  final def remoteAddress: VitalsHostAddress = {
    if (channel.remoteAddress == null)
      "unknown"
    else
      channel.remoteAddress.asInstanceOf[InetSocketAddress].getAddress.getHostAddress
  }

  /**
    * remote port for this connection
    */
  final def remotePort: VitalsHostPort = {
    if (channel.remoteAddress == null)
      -1
    else
      channel.remoteAddress.asInstanceOf[InetSocketAddress].getPort
  }

  /**
    * local address for this connection
    */
  final def localAddress: VitalsHostAddress = {
    if (channel == null || channel.remoteAddress == null)
      "unknown"
    else
      channel.localAddress.asInstanceOf[InetSocketAddress].getAddress.getHostAddress
  }

  /**
    * local address for this connection
    */
  private final
  def localPort: VitalsHostPort = {
    if (channel == null || channel.remoteAddress == null)
      -1
    else
      channel.localAddress.asInstanceOf[InetSocketAddress].getPort
  }

  /**
    * A string useful for debugging
    */
  final def link: String = {
    s"local=$localAddress:$localPort, remote=$remoteAddress:$remotePort"
  }

}
