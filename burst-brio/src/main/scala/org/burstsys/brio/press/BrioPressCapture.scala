/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.press

import org.burstsys.brio.dictionary.BrioDictionary
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.text.VitalsTextCodec

object BrioPressCapture {

  final val MapCaptureArraySize: Int = 4096

}

/**
 * this is a place for press sources to place value data to be pressed during rolling. This is a single object
 * within a press and is dynamically typed as necessary at various visit points.
 */
trait BrioPressCapture extends Any {

  /**
   * Mark the relation null
   */
  def markRelationNull(): Unit

  /**
   * Get the data type held by the current relation
   *
   * @return the relation's value's data type
   */
  def relationValueDataType: BrioDataType

  /**
   * Ensure a string exists in the blob's dictionary. If the string does not exist in the dictionary
   * and the string cannot be added, then [[BrioDictionaryNotFound]] is returned instead
   *
   * @return the dictionary key for `word` in the dictionary
   */
  def dictionaryEntry(word: String): BrioDictionaryKey

}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Value Scalars
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Capture scalar data types
 */
trait BrioValueScalarPressCapture extends BrioPressCapture {

  /**
   * Write a boolean into the brio blob
   *
   * @param value the value to write
   */
  def booleanValue(value: Boolean): Unit

  /**
   * If `value` is present, write the boolean contained by `value`, otherwise
   * write a null entry into the brio blob
   *
   * @param value the potentially missing boolean value
   */
  @inline def booleanValue(value: Option[Boolean]): Unit =
    if (value.isEmpty) markRelationNull() else booleanValue(value.get)

  def byteValue(value: Byte): Unit

  /**
   * If `value` is present, write the byte contained by `value`, otherwise
   * write a null entry into the brio blob
   *
   * @param value the potentially missing byte value
   */
  @inline def byteValue(value: Option[Byte]): Unit =
    if (value.isEmpty) markRelationNull() else byteValue(value.get)

  def shortValue(value: Short): Unit

  /**
   * If `value` is present, write the short contained by `value`, otherwise
   * write a null entry into the brio blob
   *
   * @param value the potentially missing short value
   */
  @inline def shortValue(value: Option[Short]): Unit =
    if (value.isEmpty) markRelationNull() else shortValue(value.get)

  def integerValue(value: Int): Unit

  /**
   * If `value` is present, write the integer contained by `value`, otherwise
   * write a null entry into the brio blob
   *
   * @param value the potentially missing integer value
   */
  @inline def integerValue(value: Option[Int]): Unit =
    if (value.isEmpty) markRelationNull() else integerValue(value.get)

  def longValue(value: Long): Unit

  /**
   * If `value` is present, write the long contained by `value`, otherwise
   * write a null entry into the brio blob
   *
   * @param value the potentially missing long value
   */
  @inline def longValue(value: Option[Long]): Unit =
    if (value.isEmpty) markRelationNull() else longValue(value.get)

  def doubleValue(value: Double): Unit

  /**
   * If `value` is present, write the long contained by `value`, otherwise
   * write a null entry into the brio blob
   *
   * @param value the potentially missing long value
   */
  @inline def doubleValue(value: Option[Double]): Unit =
    if (value.isEmpty) markRelationNull() else doubleValue(value.get)

  def stringValue(value: BrioDictionaryKey): Unit

}

trait BrioValueMapPressCapture extends BrioPressCapture {

  /**
   * @return the data type of the maps keys
   */
  def relationKeyDataType: BrioDataType

  def valueMapEntries(size: Long): Unit

  def booleanMapKeySet: Array[Boolean]

  def booleanMapValueSet: Array[Boolean]

  def byteMapKeySet: Array[Byte]

  def byteMapValueSet: Array[Byte]

  def shortMapKeySet: Array[Short]

  def shortMapValueSet: Array[Short]

  def integerMapKeySet: Array[Int]

  def integerMapValueSet: Array[Int]

  def longMapKeySet: Array[Long]

  def longMapValueSet: Array[Long]

  def doubleMapKeySet: Array[Double]

  def doubleMapValueSet: Array[Double]

  def stringMapKeySet: Array[BrioDictionaryKey]

  def stringMapValueSet: Array[BrioDictionaryKey]

  /**
   * Temporary storage for unsorted map[string, _] keys. The contents of this array are not used in the capture
   * You must set the final set of keys in [[stringMapKeySet]], which should be sorted.
   *
   * @return an array that the user can update
   */
  def unsortedStringMapKeySet: Array[BrioDictionaryKey]

  /**
   * Temporary storage for unsorted map[_, string] values. The contents of this array are not used in the capture
   * You must set the final set of values in [[stringMapValueSet]], which should be in the order to correspond to.
   *
   * @return
   */
  def unsortedStringMapValueSet: Array[BrioDictionaryKey]

}

trait BrioValueVectorPressCapture extends BrioPressCapture {

  /**
   * Tell the capture how many entries are expected in the vector being captured
   *
   * @param size the number of entries in the vector
   */
  def valueVectorEntries(size: Long): Unit

  /**
   * @return a boolean value vector to be updated
   */
  def booleanVector: Array[Boolean]

  /**
   * @return a byte value vector to be updated
   */
  def byteVector: Array[Byte]

  /**
   * @return a short value vector to be updated
   */
  def shortVector: Array[Short]

  /**
   * @return an integer value vector to be updated
   */
  def integerVector: Array[Int]

  /**
   * @return a long value vector to be updated
   */
  def longVector: Array[Long]

  /**
   * @return a double value vector to be updated
   */
  def doubleVector: Array[Double]

  /**
   * @return a dictionary key vector to be updated
   */
  def stringVector: Array[BrioDictionaryKey]

}


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Unified Context
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

final case
class BrioPressCaptureContext(dictionary: BrioDictionary) extends AnyRef with BrioValueScalarPressCapture
  with BrioValueMapPressCapture with BrioValueVectorPressCapture {
  import BrioPressCapture._

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Shared State
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  implicit val text: VitalsTextCodec = VitalsTextCodec()

  var isNullCaptured: Boolean = false
  var isValueCaptured: Boolean = false
  var isKeyCaptured: Boolean = false
  var isSizeCaptured: Boolean = false

  var isNull: Boolean = false

  var relationKeyDataType: BrioTypeKey = _

  var relationValueDataType: BrioTypeKey = _

  var valueVectorEntries: Long = 0

  var valueMapEntries: Long = 0

  var booleanValue: Boolean = _
  val booleanValueVector: Array[Boolean] = new Array[Boolean](MapCaptureArraySize)
  val booleanKeyVector: Array[Boolean] = new Array[Boolean](MapCaptureArraySize)

  var byteValue: Byte = _
  val byteValueVector: Array[Byte] = new Array[Byte](MapCaptureArraySize)
  val byteKeyVector: Array[Byte] = new Array[Byte](MapCaptureArraySize)

  var shortValue: Short = _
  val shortValueVector: Array[Short] = new Array[Short](MapCaptureArraySize)
  val shortKeyVector: Array[Short] = new Array[Short](MapCaptureArraySize)

  var integerValue: Int = _
  val integerValueVector: Array[Int] = new Array[Int](MapCaptureArraySize)
  val integerKeyVector: Array[Int] = new Array[Int](MapCaptureArraySize)

  var longValue: Long = _
  val longValueVector: Array[Long] = new Array[Long](MapCaptureArraySize)
  val longKeyVector: Array[Long] = new Array[Long](MapCaptureArraySize)

  var doubleValue: Double = _
  val doubleValueVector: Array[Double] = new Array[Double](MapCaptureArraySize)
  val doubleKeyVector: Array[Double] = new Array[Double](MapCaptureArraySize)

  var stringValue: BrioDictionaryKey = _
  val stringValueVector: Array[BrioDictionaryKey] = new Array[BrioDictionaryKey](MapCaptureArraySize)
  val stringKeyVector: Array[BrioDictionaryKey] = new Array[BrioDictionaryKey](MapCaptureArraySize)

  val unsortedStringKeyVector: Array[BrioDictionaryKey] = new Array[BrioDictionaryKey](MapCaptureArraySize)
  val unsortedStringValueVector: Array[BrioDictionaryKey] = new Array[BrioDictionaryKey](MapCaptureArraySize)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  final val excessiveCaution = false

  /**
   * clear out and make ready for another usage
   */
  def reset: this.type = {
    isNullCaptured = false
    isValueCaptured = false
    isKeyCaptured = false
    isSizeCaptured = false
    isNull = false
    relationKeyDataType = BrioAnyTypeKey
    relationValueDataType = BrioAnyTypeKey
    valueVectorEntries = 0
    valueMapEntries = 0
    booleanValue = false
    byteValue = 0
    shortValue = 0
    integerValue = 0
    longValue = 0
    doubleValue = 0.0
    stringValue = BrioDictionaryNotFound

    if (excessiveCaution)
      for (i <- 0 to MapCaptureArraySize) {
        booleanValueVector(i) = false
        booleanKeyVector(i) = false
        byteValueVector(i) = 0
        byteKeyVector(i) = 0
        shortValueVector(i) = 0
        shortKeyVector(i) = 0
        integerValueVector(i) = 0
        integerKeyVector(i) = 0
        longValueVector(i) = 0L
        longKeyVector(i) = 0L
        doubleValueVector(i) = 0.0
        doubleKeyVector(i) = 0.0
        stringValueVector(i) = BrioDictionaryNotFound
        stringKeyVector(i) = BrioDictionaryNotFound
        unsortedStringValueVector(i) = BrioDictionaryNotFound
        unsortedStringKeyVector(i) = BrioDictionaryNotFound
      }
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Capture State Validation
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def validateMapCapture: Boolean = {
    if (isNullCaptured)
      true
    else if (isSizeCaptured && valueMapEntries == 0)
      true
    else if (isKeyCaptured && isValueCaptured && isSizeCaptured)
      true
    else
      throw VitalsException(s"invalid value map capture ($isKeyCaptured, $isValueCaptured, $isSizeCaptured)")
  }

  def validateVectorCapture: Boolean = {
    if (isNullCaptured)
      true
    else if (isSizeCaptured && valueVectorEntries == 0)
      true
    else if (!isKeyCaptured && isValueCaptured && isSizeCaptured)
      true
    else
      throw VitalsException(s"invalid value vector capture ($isKeyCaptured, $isValueCaptured, $isSizeCaptured)")
  }

  def validateScalarCapture: Boolean = {
    if (isNullCaptured)
      true
    else if (!isKeyCaptured && isValueCaptured && !isSizeCaptured)
      true
    else
      throw VitalsException(s"invalid value scalar capture ($isKeyCaptured, $isValueCaptured, $isSizeCaptured)")
  }

  def markNullCaptured(): Unit = {
    if (isNullCaptured)
      throw VitalsException(s"null already captured!!")
    isNullCaptured = true
  }

  def markValueCaptured(): Unit = {
    if (isNullCaptured)
      throw VitalsException(s"null already captured!!")
    if (isValueCaptured)
      throw VitalsException(s"value already captured!!")
    isValueCaptured = true
  }

  def markKeyCaptured(): Unit = {
    if (isNullCaptured)
      throw VitalsException(s"null already captured!!")
    if (isKeyCaptured)
      throw VitalsException(s"key already captured!!")
    isKeyCaptured = true
  }

  def markSizeCaptured(): Unit = {
    if (isNullCaptured)
      throw VitalsException(s"null already captured!!")
    if (isSizeCaptured)
      throw VitalsException(s"size already captured!!")
    isSizeCaptured = true
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Nulls
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def dictionaryEntry(word: String): BrioDictionaryKey = dictionary.keyLookupWithAdd(word)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Nulls
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def markRelationNull(): Unit = {
    isNull = true
    markNullCaptured()
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Value Scalars
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def booleanValue(value: Boolean): Unit = {
    validateValueType(BrioBooleanKey)
    booleanValue = value
    markValueCaptured()
  }

  override
  def byteValue(value: Byte): Unit = {
    validateValueType(BrioByteKey)
    byteValue = value
    markValueCaptured()
  }

  override
  def shortValue(value: Short): Unit = {
    validateValueType(BrioShortKey)
    shortValue = value
    markValueCaptured()
  }

  override
  def integerValue(value: Int): Unit = {
    validateValueType(BrioIntegerKey)
    integerValue = value
    markValueCaptured()
  }

  override
  def longValue(value: Long): Unit = {
    validateValueType(BrioLongKey)
    longValue = value
    markValueCaptured()
  }

  override
  def doubleValue(value: Double): Unit = {
    validateValueType(BrioDoubleKey)
    doubleValue = value
    markValueCaptured()
  }

  override
  def stringValue(value: BrioDictionaryKey): Unit = {
    validateValueType(BrioStringKey)
    stringValue = value
    markValueCaptured()
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Value Map Keys
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////


  override
  def valueMapEntries(size: Long): Unit = {
    validateNonNull()
    valueMapEntries = size
    markSizeCaptured()
  }

  override
  def booleanMapKeySet: Array[Boolean] = {
    validateKeyType(BrioBooleanKey)
    markKeyCaptured()
    booleanKeyVector
  }

  override
  def booleanMapValueSet: Array[Boolean] = {
    validateValueType(BrioBooleanKey)
    markValueCaptured()
    booleanValueVector
  }

  override
  def byteMapKeySet: Array[Byte] = {
    validateKeyType(BrioByteKey)
    markKeyCaptured()
    byteKeyVector
  }

  override
  def byteMapValueSet: Array[Byte] = {
    validateValueType(BrioByteKey)
    markValueCaptured()
    byteValueVector
  }

  override
  def shortMapKeySet: Array[Short] = {
    validateKeyType(BrioShortKey)
    markKeyCaptured()
    shortKeyVector
  }

  override
  def shortMapValueSet: Array[Short] = {
    validateValueType(BrioShortKey)
    markValueCaptured()
    shortValueVector
  }

  override
  def integerMapKeySet: Array[Int] = {
    validateKeyType(BrioIntegerKey)
    markKeyCaptured()
    integerKeyVector
  }

  override
  def integerMapValueSet: Array[Int] = {
    validateValueType(BrioIntegerKey)
    markValueCaptured()
    integerValueVector
  }

  override
  def longMapKeySet: Array[Long] = {
    validateKeyType(BrioLongKey)
    markKeyCaptured()
    longKeyVector
  }

  override
  def longMapValueSet: Array[Long] = {
    validateValueType(BrioLongKey)
    markValueCaptured()
    longValueVector
  }

  override
  def doubleMapKeySet: Array[Double] = {
    validateKeyType(BrioDoubleKey)
    markKeyCaptured()
    doubleKeyVector
  }

  override
  def doubleMapValueSet: Array[Double] = {
    validateValueType(BrioDoubleKey)
    markValueCaptured()
    doubleValueVector
  }

  override
  def stringMapKeySet: Array[BrioDictionaryKey] = {
    validateKeyType(BrioStringKey)
    markKeyCaptured()
    stringKeyVector
  }

  override
  def stringMapValueSet: Array[BrioDictionaryKey] = {
    validateValueType(BrioStringKey)
    markValueCaptured()
    stringValueVector
  }

  override
  def unsortedStringMapKeySet: Array[BrioDictionaryKey] = {
    // All the other checks performed on the sorted set
    unsortedStringKeyVector
  }

  override
  def unsortedStringMapValueSet: Array[BrioDictionaryKey] = {
    // All the other checks performed on the sorted set
    unsortedStringValueVector
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Value Vectors
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def valueVectorEntries(size: Long): Unit = {
    if (size > MapCaptureArraySize)
      throw VitalsException(s"value vector size asserted ($size) is greater than MaxValueVector=$MapCaptureArraySize")
    valueVectorEntries = size
    markSizeCaptured()
  }

  override
  def booleanVector: Array[Boolean] = {
    validateValueType(BrioBooleanKey)
    markValueCaptured()
    booleanValueVector
  }

  override
  def byteVector: Array[Byte] = {
    validateValueType(BrioByteKey)
    markValueCaptured()
    byteValueVector
  }

  override
  def shortVector: Array[Short] = {
    validateValueType(BrioShortKey)
    markValueCaptured()
    shortValueVector
  }

  override
  def integerVector: Array[Int] = {
    validateValueType(BrioIntegerKey)
    markValueCaptured()
    integerValueVector
  }

  override
  def longVector: Array[Long] = {
    validateValueType(BrioLongKey)
    markValueCaptured()
    longValueVector
  }

  override
  def doubleVector: Array[Double] = {
    validateValueType(BrioDoubleKey)
    markValueCaptured()
    doubleValueVector
  }

  override
  def stringVector: Array[BrioDictionaryKey] = {
    validateValueType(BrioStringKey)
    markValueCaptured()
    stringValueVector
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Utility Methods 
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private
  def validateNonNull(): Unit = {
    if (isNull)
      throw VitalsException(s"value asserted where isNull=$isNull")
  }

  private def validateValueType(bType: BrioTypeKey): Unit = {
    validateNonNull()
    if (relationValueDataType != bType)
      throw VitalsException(s"'${brioDataTypeNameFromKey(bType)}' asserted where valueDataType='${brioDataTypeNameFromKey(relationValueDataType)}'")
  }

  private def validateKeyType(bType: BrioTypeKey): Unit = {
    validateNonNull()
    if (relationKeyDataType != bType)
      throw VitalsException(s"'${brioDataTypeNameFromKey(bType)}' asserted where keyDataType='${brioDataTypeNameFromKey(relationKeyDataType)}'")
  }

}
