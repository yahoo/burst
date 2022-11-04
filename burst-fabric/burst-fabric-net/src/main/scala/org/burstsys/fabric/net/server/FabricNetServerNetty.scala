/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.server

import org.burstsys.fabric.net.FabricNetworkConfig
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel._

/**
 * Did you notice option() and childOption()? option() is for the NioServerSocketChannel that accepts
 * incoming connections. childOption() is for the Channels accepted by the parent ServerChannel,
 * which is NioServerSocketChannel in this case.
 */
trait FabricNetServerNetty extends Any {

  self: FabricNetServerContext =>

  final
  def setNettyOptions(bootstrap: ServerBootstrap, netConfig: FabricNetworkConfig): ServerBootstrap = {

    // child option means for each client connection...
    bootstrap

      /**
       * off heap pooled
       */
      .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)

      .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)

      /**
       * Sets or gets the maximum socket send buffer in bytes.  The
       * kernel doubles this value (to allow space for bookkeeping
       * overhead) when it is set using setsockopt(2), and this doubled
       * value is returned by getsockopt(2).  The default value is set
       * by the /proc/sys/net/core/wmem_default file and the maximum
       * allowed value is set by the /proc/sys/net/core/wmem_max file.
       * The minimum (doubled) value for this option is 2048.
       */
      //      .childOption[java.lang.Integer](ChannelOption.SO_SNDBUF, 100e6.toInt)

      /**
       * Sets or gets the maximum socket receive buffer in bytes.  The
       * kernel doubles this value (to allow space for bookkeeping
       * overhead) when it is set using setsockopt(2), and this doubled
       * value is returned by getsockopt(2).  The default value is set
       * by the /proc/sys/net/core/rmem_default file, and the maximum
       * allowed value is set by the /proc/sys/net/core/rmem_max file.
       * The minimum (doubled) value for this option is 256.
       */
      //      .childOption(ChannelOption.SO_RCVBUF, 65536)

      /**
       * The backlog argument defines the maximum length to which the queue of
       * pending connections for sockfd may grow.  If a connection request
       * arrives when the queue is full, the client may receive an error with
       * an indication of ECONNREFUSED or, if the underlying protocol supports
       * retransmission, the request may be ignored so that a later reattempt
       * at connection succeeds.
       */
      //      .option[java.lang.Integer](ChannelOption.SO_BACKLOG, netConfig.connectionBacklog)
      //      .option[java.lang.Integer](ChannelOption.SO_BACKLOG, 500)

      /**
       * Indicates that the rules used in validating addresses supplied
       * in a bind(2) call should allow reuse of local addresses.  For
       * AF_INET sockets this means that a socket may bind, except when
       * there is an active listening socket bound to the address.
       * When the listening socket is bound to INADDR_ANY with a spe‐
       * cific port then it is not possible to bind to this port for
       * any local address.  Argument is an integer boolean flag.
       */
      //.childOption(ChannelOption.SO_REUSEADDR, true)

      /**
       * https://github.com/netty/netty/issues/939
       */
      .childOption[java.lang.Boolean](ChannelOption.TCP_NODELAY, true)

      /**
       * when writable condition is toggled
       */
      //      .childOption[WriteBufferWaterMark](ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(netConfig.lowWaterMark, netConfig.highWaterMark))

      /**
       * The SO_KEEPALIVE option causes a packet (called a 'keepalive probe')
       * to be sent to the remote system if a long time (by default, more than
       * 2 hours) passes with no other data being sent or received. This packet
       * is designed to provoke an ACK response from the peer. This enables
       * detection of a peer which has become unreachable (e.g. powered off or
       * disconnected from the net).
       **/
      .childOption[java.lang.Boolean](ChannelOption.SO_KEEPALIVE, true)

    bootstrap

  }
}
