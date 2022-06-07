/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net.client

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel._

/**
  *
  */
trait FabricNetClientNetty extends Any {

  self: FabricNetClientContext =>

  final
  def setNettyOptions(bootstrap: Bootstrap): Bootstrap = {
    bootstrap

      /**
        * off heap pooled
        */
      .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)

      /**
        * Sets or gets the maximum socket send buffer in bytes.  The
        * kernel doubles this value (to allow space for bookkeeping
        * overhead) when it is set using setsockopt(2), and this doubled
        * value is returned by getsockopt(2).  The default value is set
        * by the /proc/sys/net/core/wmem_default file and the maximum
        * allowed value is set by the /proc/sys/net/core/wmem_max file.
        * The minimum (doubled) value for this option is 2048.
        */
      //.option(ChannelOption.SO_SNDBUF, 65536)

      /**
        * Sets or gets the maximum socket receive buffer in bytes.  The
        * kernel doubles this value (to allow space for bookkeeping
        * overhead) when it is set using setsockopt(2), and this doubled
        * value is returned by getsockopt(2).  The default value is set
        * by the /proc/sys/net/core/rmem_default file, and the maximum
        * allowed value is set by the /proc/sys/net/core/rmem_max file.
        * The minimum (doubled) value for this option is 256.
        */
//      .option[Integer](ChannelOption.SO_RCVBUF, 500e6.toInt)

      /**
        * The backlog argument defines the maximum length to which the queue of
        * pending connections for sockfd may grow.  If a connection request
        * arrives when the queue is full, the client may receive an error with
        * an indication of ECONNREFUSED or, if the underlying protocol supports
        * retransmission, the request may be ignored so that a later reattempt
        * at connection succeeds.
        */
      //      .option(ChannelOption.SO_BACKLOG, 500)

      /**
        * The SO_KEEPALIVE option causes a packet (called a 'keepalive probe')
        * to be sent to the remote system if a long time (by default, more than
        * 2 hours) passes with no other data being sent or received. This packet
        * is designed to provoke an ACK response from the peer. This enables
        * detection of a peer which has become unreachable (e.g. powered off or
        * disconnected from the net).
        **/
      .option[java.lang.Boolean](ChannelOption.SO_KEEPALIVE, true)

      /**
        * This socket option tells the kernel that even if this port is busy (in
        * the TIME_WAIT state), go ahead and reuse it anyway.  If it is busy,
        * but with another state, you will still get an address already in use
        * error.  It is useful if your server has been shut down, and then
        * restarted right away while sockets are still active on its port.  You
        * should be aware that if any unexpected data comes in, it may confuse
        * your server, but while this is possible, it is not likely.
        */
      //      .option(ChannelOption.SO_REUSEADDR, true)

      /**
        * https://github.com/netty/netty/issues/939
        */
      .option[java.lang.Boolean](ChannelOption.TCP_NODELAY, true)

      /**
        * when writable condition is toggled
        */
//      .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(config.lowWaterMark, config.highWaterMark))

    bootstrap

  }
}
