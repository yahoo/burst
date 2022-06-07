/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio

import org.burstsys.vitals.logging._

import scala.language.postfixOps

/**
  * zero object allocation presser framework for Brio data models
  */
package object press extends VitalsLogger {

  /**
    * Does a traditional quickSort on the primary Array and replicates the swap
    * step on the secondary array
    *
    * @param primaryArray
    * @param secondaryArray
    * @param size
    * @param leqCompare
    * @tparam A
    * @tparam B
    */
  def inplaceMultiQuickSort[A, B](primaryArray: Array[A],
                                  secondaryArray: Array[B],
                                  size: Int)(leqCompare: (A, A) => Boolean): Unit = {
    multiQuickSort(primaryArray, secondaryArray, 0, size - 1)(leqCompare)
  }

  /**
    * This function takes last element as pivot, places the pivot element at its correct
    * position in sorted array, and places all smaller (smaller than pivot) to left of
    * pivot and all greater elements to right of pivot
    */
  private
  def partition[A, B](parr: Array[A],
                      sarr: Array[B],
                      low: Int,
                      high: Int)(leqCompare: (A, A) => Boolean): Int = {
    val pivot = parr(high)
    var i = low - 1
    // index of smaller element
    var j = low
    while (j < high) {
      if (leqCompare(parr(j),pivot)) {
        i += 1
        multiSwap(parr, sarr, i, j)
      }
      j += 1
    }
    multiSwap(parr, sarr, i+1, high)
    i + 1
  }

  /**
    * Swap positions index1 and index2 in both parr and sarr
    * @param parr
    * @param sarr
    * @param index1
    * @param index2
    * @tparam A
    * @tparam B
    */
  private
  def multiSwap[A, B](parr: Array[A], sarr: Array[B], index1: Int, index2: Int): Unit = {
    val tempA = parr(index1)
    val tempB = sarr(index1)

    parr(index1) = parr(index2)
    sarr(index1) = sarr(index2)

    parr(index2) = tempA
    sarr(index2) = tempB
  }


  /**
    * The main function that implements QuickSort()
    * parr[] --> Array to be sorted, low  --> Starting index, high  --> Ending index.
    * sarr[] elements are also swapped correspondingly
    */
  private
  def multiQuickSort[A, B](parr: Array[A],
                 sarr: Array[B],
                 low: Int,
                 high: Int)(leqCompare: (A, A) => Boolean): Unit = {
    if (low < high) {
      // pi is partitioning index, parr[pi] is now at right place
      val pi = partition(parr, sarr, low, high)(leqCompare)
      // Recursively sort elements before partition and after partition
      multiQuickSort(parr, sarr, low, pi - 1)(leqCompare)
      multiQuickSort(parr, sarr, pi + 1, high)(leqCompare)
    }
  }
}
