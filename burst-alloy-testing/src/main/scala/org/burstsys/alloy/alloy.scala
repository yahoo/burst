/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys

package object alloy {


  case class BurstUnitRepeatingValue[T](data: T*) {
    var index: Int = 0

    def next: T = {
      index += 1
      if (index >= data.length)
        index = 0
      value
    }

    def value: T = {
      data(index)
    }

  }


}
