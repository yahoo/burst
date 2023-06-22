/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore

import org.burstsys.tesla.block
import org.burstsys.tesla.block.factory.TeslaBlockSizes
import org.burstsys.tesla.buffer.mutable

import java.util.concurrent.atomic.AtomicLong
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reporter.instrument.{MB, prettyByteSizeString}

import scala.concurrent.duration._
import scala.language.postfixOps

package object pipeline extends VitalsLogger with PressPipeline {

  private[pipeline]
  final val jobId = new AtomicLong

  final val pressBufferSize = {
    val sSz = TeslaBlockSizes.findBlockSize(20 * MB.toInt)
    this.log info s"Buffer start max size: $sSz (${prettyByteSizeString(sSz)})"
    val sz = sSz - mutable.SizeofMutableBufferHeader - 2 * block.SizeofBlockHeader
    this.log info s"Buffer adjusted max size: $sz (${prettyByteSizeString(sz)})"
    this.log info s"Buffer adjusted max size after block check: ${TeslaBlockSizes.findBlockSize(sz)} (${prettyByteSizeString(TeslaBlockSizes.findBlockSize(sz))})"
    sz

  }

  final val slowPressDuration = 60 seconds // how long is suspiciously long for a press

  final val brioPressDefaultDictionarySize: TeslaPoolId = 10e6.toInt

}
