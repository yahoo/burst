/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.text

import java.nio.charset.{CharsetDecoder, CharsetEncoder, StandardCharsets}
import java.nio.{ByteBuffer, CharBuffer}

import scala.collection.mutable

/**
  * This allows for reuse of expensive structures within a single thread.
  * i.e. this is ''not'' thread safe.
  * This is __UTF8__ only
  *
  * TODO write an version of this that grabs characters from off heap memory
  * to create a CharBuffer.
  */
final case class VitalsTextCodec() {

  private val encoder: CharsetEncoder = StandardCharsets.UTF_8.newEncoder
  private val decoder: CharsetDecoder = StandardCharsets.UTF_8.newDecoder
  private val builder = new mutable.ArrayBuilder.ofByte

  @inline
  def encode(s: String): Array[Byte] = { // YOYO
    val buffer = encoder.encode(CharBuffer.wrap(s))
    val array = buffer.array
    var c = 0
    while (c < buffer.limit()) {
      builder += array(c)
      c += 1
    }
    val r = builder.result
    builder.clear
    encoder.reset
    r
  }

  @inline
  def decode(bytes: Array[Byte]): String = {
    val r = decoder.decode(ByteBuffer.wrap(bytes)).toString
    decoder.reset
    r
  }
}
