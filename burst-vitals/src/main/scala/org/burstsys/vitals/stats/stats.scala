/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import scala.language.implicitConversions

package object stats {

  final val KB: Long = math.pow(2, 10).toLong
  final val MB: Long = math.pow(2, 20).toLong
  final val GB: Long = math.pow(2, 30).toLong
  final val TB: Long = math.pow(2, 40).toLong
  final val PB: Long = math.pow(2, 50).toLong

  implicit class ByteSize(val data: Long) extends AnyVal {

    def inB: Long = data

    def b: Long = data

    def mb: Long = data * MB

    def gb: Long = data * GB

    /**
     * lower and upper bound are inclusive
     */
    def between(low: ByteSize, high: ByteSize): Boolean = {
      (data >= low.inB) && (data <= high.inB)
    }


    override def toString: String = reporter.instrument.prettyByteSizeString(data)
  }

  implicit def intToBytesSize(size: Int): ByteSize = new ByteSize(size)

  /**
   * our standard way to measure skew
   */
  def stdSkewStat(min: Long, max: Long): Double = {
    if (min == Long.MaxValue) 0.0
    else if (min == 0) 1.0
    else (max - min) / min.toDouble
  }


}
