/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net

import io.netty.channel.Channel
import io.netty.util.concurrent.{Future => NettyFuture}
import io.opentelemetry.api.common.AttributeKey
import org.burstsys.fabric.net.message.FabricNetMsg
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._
import org.burstsys.vitals.trek.context.injectContext

import scala.concurrent.{Future, Promise}

package object transceiver extends VitalsLogger {

  val fabricMessageTypeKey: AttributeKey[java.lang.Long] = AttributeKey.longKey("burst.fabric.message.type")

  /**
    * handle message events associated with a fabric network client
    */
  trait FabricNetClientMsgListener extends Any {

  }

  /**
    * handle message events associated with a fabric network server
    */
  trait FabricNetServerMsgListener extends Any {

  }

  class NettyMessageSendRunnable(channel: Channel, msg: FabricNetMsg) extends Runnable {

    private val promise = Promise[Unit]()

    def completion: Future[Unit] = promise.future

    override def run(): Unit = {
      try {
        val encodeStart = System.nanoTime
        val buffer = channel.alloc().buffer()
        injectContext(this, buffer)
        msg.encode(buffer)
        val buffSize = buffer.capacity
        val encodeDuration = System.nanoTime - encodeStart
        val transmitEnqueued = System.nanoTime
        channel.writeAndFlush(buffer).addListener((future: NettyFuture[_ >: Void]) => {
          val transmitDuration = System.nanoTime - transmitEnqueued
          log trace burstLocMsg(s"encodeNanos=$encodeDuration transmitNanos=$transmitDuration")
          if (future.isSuccess) {
            FabricNetReporter.onMessageXmit(buffSize)
            promise.success(())
          } else {
            log warn burstLocMsg(s"FAIL  ${future.cause}", future.cause)
            if (!promise.isCompleted)
              promise.failure(future.cause())
          }
        })
      } catch safely {
        case t: Throwable =>
          log error burstLocMsg(s"FAB_TX_FAIL $t", t)
          promise.failure(t)
      }
    }
  }

}
