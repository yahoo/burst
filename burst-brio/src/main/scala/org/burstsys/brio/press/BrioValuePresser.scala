/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.press

import org.burstsys.brio.types.BrioTypes.BrioDictionaryKey

/**
 * A utility trait to press various types of values
 * Note some of these routines are not super efficient especially where the underlying data is already sorted...
 */
trait BrioValuePresser {

  ////////////////////////////////////////////////////////////////////////////////////
  // String Scalars
  ////////////////////////////////////////////////////////////////////////////////////

  protected final
  def extractStringValueScalar(capture: BrioValueScalarPressCapture, stringValue: String): Unit = {
    if (stringValue == null) {
      capture.markRelationNull()
    } else capture.stringValue(capture.dictionaryEntry(stringValue))
  }

  def extractStringValueWithPossibleNull(capture: BrioValueScalarPressCapture, value: String): Unit = {
    if (value == null) capture.markRelationNull()
    else capture.stringValue(capture.dictionaryEntry(value))
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // Value Vectors
  ////////////////////////////////////////////////////////////////////////////////////

  protected final
  def extractStringValueVector(capture: BrioValueVectorPressCapture, array: Array[String]): Unit = {
    val keys = array.map(capture.dictionaryEntry)
    // this may look odd but it avoids object creation - harder than it looks
    // copy your data into the provided array
    Array.copy(keys, 0, capture.stringVector, 0, array.length)
    // tell datasource the size of this array
    capture.valueVectorEntries(array.length)
  }

  protected final
  def extractBooleanValueVector(capture: BrioValueVectorPressCapture, array: Array[Boolean]): Unit = {
    capture.valueVectorEntries(array.length)
    Array.copy(array, 0, capture.booleanVector, 0, array.length)
  }

  protected final
  def extractByteValueVector(capture: BrioValueVectorPressCapture, array: Array[Byte]): Unit = {
    capture.valueVectorEntries(array.length)
    Array.copy(array, 0, capture.byteVector, 0, array.length)
  }

  protected final
  def extractShortValueVector(capture: BrioValueVectorPressCapture, array: Array[Short]): Unit = {
    capture.valueVectorEntries(array.length)
    Array.copy(array, 0, capture.shortVector, 0, array.length)
  }

  protected final
  def extractIntegerValueVector(capture: BrioValueVectorPressCapture, array: Array[Integer]): Unit = {
    capture.valueVectorEntries(array.length)
    Array.copy(array, 0, capture.integerVector, 0, array.length)
  }

  protected final
  def extractLongValueVector(capture: BrioValueVectorPressCapture, array: Array[Long]): Unit = {
    capture.valueVectorEntries(array.length)
    val sortedValueSet = array.sorted // make sure values are sorted to match keys
    Array.copy(sortedValueSet, 0, capture.longVector, 0, array.length)
  }

  protected final
  def extractDoubleValueVector(capture: BrioValueVectorPressCapture, array: Array[Double]): Unit = {
    Array.copy(array, 0, capture.doubleVector, 0, array.length)
    // tell datasource the size of this array
    capture.valueVectorEntries(array.length)
  }

  ////////////////////////////////////////////////////////////////////////////////////
  // Maps
  ////////////////////////////////////////////////////////////////////////////////////

  protected final
  def extractStringStringMap(capture: BrioValueMapPressCapture, map: Map[String, String]): Unit = {
    extractStringStringMap(capture, map.iterator)
  }

  protected final
  def extractStringStringMap(capture: BrioValueMapPressCapture, iterator: Iterator[(String, String)]): Unit = {
    var i = 0
    val unsortedKeySet = capture.unsortedStringMapKeySet
    val unsortedValueSet = capture.unsortedStringMapValueSet
    while (iterator.hasNext) {
      val e = iterator.next()
      unsortedKeySet(i) = capture.dictionaryEntry(e._1)
      unsortedValueSet(i) = capture.dictionaryEntry(e._2)
      i += 1
    }
    extractStringStringMap(capture, unsortedKeySet, unsortedValueSet, i)
  }

  // The calling function must use capture.dictionaryEntry to populate the Input Sets
  protected final
  def extractStringStringMap(capture: BrioValueMapPressCapture,
                             inputKeySet: Array[BrioDictionaryKey],
                             inputValueSet: Array[BrioDictionaryKey],
                             size: Int): Unit = {
    capture.valueMapEntries(size)
    if (size > 0) {
      val keySet = capture.stringMapKeySet
      val valueSet = capture.stringMapValueSet

      inplaceMultiQuickSort(inputKeySet, inputValueSet, size)((a1: BrioDictionaryKey, a2: BrioDictionaryKey) => a1 <= a2)

      Array.copy(inputKeySet, 0, keySet, 0, size)
      Array.copy(inputValueSet, 0, valueSet, 0, size)
    } else {
      capture.stringMapKeySet
      capture.stringMapValueSet
    }
  }

  protected final
  def extractLongStringMap(capture: BrioValueMapPressCapture, map: Map[Long, String]): Unit = {
    val entries = map.size
    capture.valueMapEntries(entries)
    if (map.nonEmpty) {
      val sortedKeySet = map.keySet.take(entries).toList.sorted.toArray // make sure keys are sorted
      val sortedValueSet = sortedKeySet.map(s => capture.dictionaryEntry(map(s)))
      // make sure values are sorted to match keys
      // copy your data into the provided array
      Array.copy(sortedKeySet, 0, capture.longMapKeySet, 0, entries)
      Array.copy(sortedValueSet, 0, capture.stringMapValueSet, 0, entries)
    } else {
      capture.longMapKeySet
      capture.stringMapValueSet
    }
  }
}
