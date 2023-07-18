package org.burstsys.vitals.trek

import io.netty.buffer.{ByteBuf, Unpooled}
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.{TextMapGetter, TextMapSetter}

import scala.collection.mutable
import scala.jdk.CollectionConverters.IterableHasAsJava

object context {

  private final val LAST_TAG:Byte = -1
  private final val PROPERTY_TAG:Byte = -2

  def injectContext(msg: ByteBuf): Unit = {
    val prop = GlobalOpenTelemetry.getPropagators.getTextMapPropagator
    prop.inject(Context.current(), msg, msgSetter)
    msg.writeByte(LAST_TAG)
  }

  def extractContext(msg: ByteBuf): Unit = {
    GlobalOpenTelemetry.getPropagators.getTextMapPropagator.extract(Context.current(), msg, msgGetter)
    skipContext(msg)
  }

  def extractContext(msg: Array[Byte]): Array[Byte] = {
    val buffer = Unpooled.wrappedBuffer(msg)
    this.extractContext(buffer)
    val ri = buffer.readerIndex()
    msg.slice(ri, msg.length)
  }

  private def skipContext(msg: ByteBuf): Unit = {
    var tag = msg.readByte()

    while (tag != LAST_TAG) {
      if (tag != PROPERTY_TAG)
        throw new RuntimeException(s"Expected PROPERTY_TAG instead got $tag")
      skipAsciiStringFromByteBuf(msg)
      skipAsciiStringFromByteBuf(msg)
      tag = msg.readByte()
    }
  }

  def skipAsciiStringFromByteBuf(buffer: ByteBuf): Unit = {
    val length = buffer.readByte
    buffer.readerIndex(buffer.readerIndex() + length)
  }

  def decodeAsciiStringFromByteBuf(buffer: ByteBuf): String = {
    val length = buffer.readByte
    val bytes = new Array[Byte](length)
    buffer.readBytes(bytes)
    new String(bytes)
  }

  def encodeAsciiStringToByteBuf(s: String, buffer: ByteBuf): Unit = {
    val bytes = s.getBytes()
    buffer.writeByte(bytes.length)
    buffer.writeBytes(bytes)
  }

  private val msgGetter = new MsgGetter()
  private class MsgGetter extends TextMapGetter[ByteBuf] {
    override def keys(msg: ByteBuf): java.lang.Iterable[String] = {
      // assume we are at the beginning of the context
      val ri = msg.readerIndex()
      var tag = msg.readByte()
      val keys = mutable.Set[String]()

      while (tag != LAST_TAG) {
        if (tag != PROPERTY_TAG)
          throw new RuntimeException(s"Expected PROPERTY_TAG instead got $tag")
        val msgKey = decodeAsciiStringFromByteBuf(msg)
        keys.add(msgKey)
        skipAsciiStringFromByteBuf(msg)
        tag = msg.readByte()
      }
      msg.readerIndex(ri) // return to where we started
      keys.iterator.to(Iterable).asJava
    }

    override def get(msg: ByteBuf, key: String): String = {
      val ri = msg.readerIndex()
      var getValue:String = null
      var tag = msg.readByte()

      while (tag != LAST_TAG) {
        if (tag != PROPERTY_TAG)
          throw new RuntimeException(s"Expected PROPERTY_TAG instead got $tag")
        val msgKey = decodeAsciiStringFromByteBuf(msg)
        if (msgKey == key) {
          getValue = decodeAsciiStringFromByteBuf(msg)
        } else {
          skipAsciiStringFromByteBuf(msg)
        }
        tag = msg.readByte()
      }
      msg.readerIndex(ri) // return to where we started
      getValue
    }
  }

  private val msgSetter = new MsgSetter()
  private class MsgSetter extends TextMapSetter[ByteBuf] {
    override def set(carrier: ByteBuf, key: String, value: String): Unit = {
      carrier.writeByte(PROPERTY_TAG)
      encodeAsciiStringToByteBuf(key, carrier)
      encodeAsciiStringToByteBuf(value, carrier)
    }
  }


}
