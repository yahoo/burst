/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

package object stats {

  /**
   * our standard way to measure skew
   * @param min
   * @param max
   * @return
   */
  def stdSkewStat(min: Long, max: Long): Double = {
    if (min == Long.MaxValue) 0.0
    else if (min == 0) 1.0
    else (max - min) / min.toDouble
  }


}
