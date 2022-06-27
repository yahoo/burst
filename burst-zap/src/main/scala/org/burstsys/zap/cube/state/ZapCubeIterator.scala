/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.state

import org.burstsys.zap.cube
import org.burstsys.zap.cube._

/**
 * supports basic iteration through rows
 */
trait ZapCubeIterator extends Any with ZapCube {


  /**
   * Iterate over all rows in all buckets
   *
   * @param body
   */
  @inline final private[zap]
  def foreachRow(builder: ZapCubeBuilder, thisCube: ZapCubeContext, body: ZapCubeRow => Unit): Unit = {
    var i = 0
    while (i < thisCube.rowCount) {
      body(thisCube.row(builder, thisCube, i))
      i += 1
    }
  }


  @inline final private[zap]
  def foreachRowByBucket(builder: ZapCubeBuilder, thisCube: ZapCubeContext, body: ZapCubeRow => Unit): Unit = {
    var b = 0
    while (b < bucketCount) {
      thisCube.bucket(builder, thisCube, b) match {
        case ZapCubeEmptyBucket =>
        case p =>
          var row = cube.ZapCubeRow(p)
          do {
            body(row)
            row = row.linkRow(builder, thisCube)
          } while (row.validRow)
      }
      b += 1
    }
  }
}
