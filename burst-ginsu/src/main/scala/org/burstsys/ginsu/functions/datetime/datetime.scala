/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu.functions

package object datetime {

  final val Q1: Array[Int] = Array(1, 2, 3)
  final val Q2: Array[Int] = Array(4, 5, 6)
  final val Q3: Array[Int] = Array(7, 8, 9)
  final val Q4: Array[Int] = Array(10, 11, 12)
  final val H1: Array[Int] = Array(1, 2, 3, 4, 5, 6)
  final val H2: Array[Int] = Array(7, 8, 9, 10, 11, 12)

  trait GinsuDatetimeFunctions extends Any with GinsuDurationFunctions with GinsuGrainFunctions
    with GinsuOrdinalFunctions with GinsuTickFunctions

}
