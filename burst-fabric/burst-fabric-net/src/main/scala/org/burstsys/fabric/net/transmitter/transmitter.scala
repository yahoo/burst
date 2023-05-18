/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net

import io.netty.channel.Channel
import io.netty.util.concurrent.{Future => NettyFuture}
import org.burstsys.fabric.net.message.FabricNetMsg
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._

import scala.concurrent.{Future, Promise}

package object transmitter extends VitalsLogger {

  class NettyMessageSendRunnable(channel: Channel, msg: FabricNetMsg, tag: String) extends Runnable {

    private val promise = Promise[Unit]()

    def completion: Future[Unit] = promise.future

    override def run(): Unit = {
      try {
        val encodeStart = System.nanoTime
        val buffer = channel.alloc().buffer()
        msg.encode(buffer)
        val buffSize = buffer.capacity
        val encodeDuration = System.nanoTime - encodeStart
        val transmitEnqueued = System.nanoTime
        channel.writeAndFlush(buffer).addListener((future: NettyFuture[_ >: Void]) => {
          val transmitDuration = System.nanoTime - transmitEnqueued
          log trace burstStdMsg(s"$tag encodeNanos=$encodeDuration transmitNanos=$transmitDuration")
          if (future.isSuccess) {
            FabricNetReporter.onMessageXmit(buffSize)
            promise.success(())
          } else {
            log warn burstStdMsg(s"$tag FAIL  ${future.cause}", future.cause)
            if (!promise.isCompleted)
              promise.failure(future.cause())
          }
        })
      } catch safely {
        case t: Throwable =>
          log error burstStdMsg(s"XMIT_FAIL $t $tag", t)
          promise.failure(t)
      }
    }
  }
}
