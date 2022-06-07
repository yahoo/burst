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
    * mark the relation null
    */
  def markRelationNull(): Unit

  /**
    * the relations value data type
    *
    * @return
    */
  def relationValueDataType: BrioDataType

  /**
    *
    * @return
    */
  def dictionaryEntry(word: String): BrioDictionaryKey

}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Value Scalars
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

trait BrioValueScalarPressCapture extends BrioPressCapture {

  def booleanValue(value: Boolean): Unit

  def byteValue(value: Byte): Unit

  def shortValue(value: Short): Unit

  def integerValue(value: Int): Unit

  def longValue(value: Long): Unit

  def doubleValue(value: Double): Unit

  def stringValue(value: BrioDictionaryKey): Unit

}

trait BrioValueMapPressCapture extends BrioValueScalarPressCapture {

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

  def unsortedStringMapKeySet: Array[BrioDictionaryKey]

  def unsortedStringMapValueSet: Array[BrioDictionaryKey]

}

trait BrioValueVectorPressCapture extends BrioPressCapture {

  /**
    * presser source sets the size of the vector here...
    *
    * @param size
    */
  def valueVectorEntries(size: Long): Unit

  /**
    * a boolean value vector to be updated
    *
    * @return
    */
  def booleanVector: Array[Boolean]

  /**
    * a byte value vector to be updated
    *
    * @return
    */
  def byteVector: Array[Byte]

  /**
    * a short value vector to be updated
    *
    * @return
    */
  def shortVector: Array[Short]

  /**
    * a integer value vector to be updated
    *
    * @return
    */
  def integerVector: Array[Int]

  /**
    * a long value vector to be updated
    *
    * @return
    */
  def longVector: Array[Long]

  /**
    * a double value vector to be updated
    *
    * @return
    */
  def doubleVector: Array[Double]

  /**
    * a string value vector to be updated
    *
    * @return
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
  var booleanKey: Boolean = _

  var byteValue: Byte = _
  val byteValueVector: Array[Byte] = new Array[Byte](MapCaptureArraySize)
  val byteKeyVector: Array[Byte] = new Array[Byte](MapCaptureArraySize)
  var byteKey: Byte = _

  var shortValue: Short = _
  val shortValueVector: Array[Short] = new Array[Short](MapCaptureArraySize)
  val shortKeyVector: Array[Short] = new Array[Short](MapCaptureArraySize)
  var shortKey: Short = _

  var integerValue: Int = _
  val integerValueVector: Array[Int] = new Array[Int](MapCaptureArraySize)
  val integerKeyVector: Array[Int] = new Array[Int](MapCaptureArraySize)
  var integerKey: Int = _

  var longValue: Long = _
  val longValueVector: Array[Long] = new Array[Long](MapCaptureArraySize)
  val longKeyVector: Array[Long] = new Array[Long](MapCaptureArraySize)
  var longKey: Long = _

  var doubleValue: Double = _
  val doubleValueVector: Array[Double] = new Array[Double](MapCaptureArraySize)
  val doubleKeyVector: Array[Double] = new Array[Double](MapCaptureArraySize)
  var doubleKey: Double = _

  var stringValue: BrioDictionaryKey = _
  val stringValueVector: Array[BrioDictionaryKey] = new Array[BrioDictionaryKey](MapCaptureArraySize)
  val stringKeyVector: Array[BrioDictionaryKey] = new Array[BrioDictionaryKey](MapCaptureArraySize)
  var stringKey: BrioDictionaryKey = _

  val unsortedStringKeyVector: Array[BrioDictionaryKey] = new Array[BrioDictionaryKey](MapCaptureArraySize)
  val unsortedStringValueVector: Array[BrioDictionaryKey] = new Array[BrioDictionaryKey](MapCaptureArraySize)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Lifecycle
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  final val excessiveCaution = false

  /**
    * clear out and make ready for another usage
    *
    * @return
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
    if (excessiveCaution)
      for (i <- booleanValueVector.indices) booleanValueVector(i) = false
    if (excessiveCaution)
      for (i <- booleanKeyVector.indices) booleanKeyVector(i) = false
    booleanKey = false
    byteValue = 0
    if (excessiveCaution)
      for (i <- byteValueVector.indices) byteValueVector(i) = 0
    if (excessiveCaution)
      for (i <- byteKeyVector.indices) byteKeyVector(i) = 0
    byteKey = 0
    shortValue = 0
    if (excessiveCaution)
      for (i <- shortValueVector.indices) shortValueVector(i) = 0
    if (excessiveCaution)
      for (i <- shortKeyVector.indices) shortKeyVector(i) = 0
    shortKey = 0
    integerValue = 0
    if (excessiveCaution)
      for (i <- integerValueVector.indices) integerValueVector(i) = 0
    if (excessiveCaution)
      for (i <- integerKeyVector.indices) integerKeyVector(i) = 0
    integerKey = 0
    longValue = 0
    if (excessiveCaution)
      for (i <- longValueVector.indices) longValueVector(i) = 0L
    if (excessiveCaution)
      for (i <- longKeyVector.indices) longKeyVector(i) = 0L
    longKey = 0
    doubleValue = 0.0
    if (excessiveCaution)
      for (i <- doubleValueVector.indices) doubleValueVector(i) = 0.0
    if (excessiveCaution)
      for (i <- doubleKeyVector.indices) doubleKeyVector(i) = 0.0
    doubleKey = 0.0
    stringValue = BrioDictionaryNotFound
    if (excessiveCaution)
      for (i <- stringValueVector.indices) stringValueVector(i) = BrioDictionaryNotFound
    if (excessiveCaution)
      for (i <- stringKeyVector.indices) stringKeyVector(i) = BrioDictionaryNotFound
    stringKey = BrioDictionaryNotFound
    if (excessiveCaution)
      for (i <- unsortedStringValueVector.indices) unsortedStringValueVector(i) = BrioDictionaryNotFound
    if (excessiveCaution)
      for (i <- unsortedStringKeyVector.indices) unsortedStringKeyVector(i) = BrioDictionaryNotFound
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Capture State Validation
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def validateMapCapture: Boolean = {
    if (isNullCaptured) return true
    if (isKeyCaptured && isValueCaptured && isSizeCaptured) return true
    throw VitalsException(s"invalid value map capture ($isKeyCaptured, $isValueCaptured, $isSizeCaptured)")
  }

  def validateVectorCapture: Boolean = {
    if (isNullCaptured) return true
    if (!isKeyCaptured && isValueCaptured && isSizeCaptured) return true
    throw VitalsException(s"invalid value vector capture ($isKeyCaptured, $isValueCaptured, $isSizeCaptured)")
  }

  def validateScalarCapture: Boolean = {
    if (isNullCaptured) return true
    if (!isKeyCaptured && isValueCaptured && !isSizeCaptured) return true
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
    validateNonNull()
    validateValueType(BrioBooleanKey)
    booleanValue = value
    markValueCaptured()
  }

  override
  def byteValue(value: Byte): Unit = {
    validateNonNull()
    validateValueType(BrioByteKey)
    byteValue = value
    markValueCaptured()
  }

  override
  def shortValue(value: Short): Unit = {
    validateNonNull()
    validateValueType(BrioShortKey)
    shortValue = value
    markValueCaptured()
  }

  override
  def integerValue(value: Int): Unit = {
    validateNonNull()
    validateValueType(BrioIntegerKey)
    integerValue = value
    markValueCaptured()
  }

  override
  def longValue(value: Long): Unit = {
    validateNonNull()
    validateValueType(BrioLongKey)
    longValue = value
    markValueCaptured()
  }

  override
  def doubleValue(value: Double): Unit = {
    validateNonNull()
    validateValueType(BrioDoubleKey)
    doubleValue = value
    markValueCaptured()
  }

  override
  def stringValue(value: BrioDictionaryKey): Unit = {
    validateNonNull()
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
    validateNonNull()
    validateKeyType(BrioBooleanKey)
    markKeyCaptured()
    booleanKeyVector
  }

  override
  def booleanMapValueSet: Array[Boolean] = {
    validateNonNull()
    validateValueType(BrioBooleanKey)
    markValueCaptured()
    booleanValueVector
  }

  override
  def byteMapKeySet: Array[Byte] = {
    validateNonNull()
    validateKeyType(BrioByteKey)
    markKeyCaptured()
    byteKeyVector
  }

  override
  def byteMapValueSet: Array[Byte] = {
    validateNonNull()
    validateValueType(BrioByteKey)
    markValueCaptured()
    byteValueVector
  }

  override
  def shortMapKeySet: Array[Short] = {
    validateNonNull()
    validateKeyType(BrioShortKey)
    markKeyCaptured()
    shortKeyVector
  }

  override
  def shortMapValueSet: Array[Short] = {
    validateNonNull()
    validateValueType(BrioShortKey)
    markValueCaptured()
    shortValueVector
  }

  override
  def integerMapKeySet: Array[Int] = {
    validateNonNull()
    validateKeyType(BrioIntegerKey)
    markKeyCaptured()
    integerKeyVector
  }

  override
  def integerMapValueSet: Array[Int] = {
    validateNonNull()
    validateValueType(BrioIntegerKey)
    markValueCaptured()
    integerValueVector
  }

  override
  def longMapKeySet: Array[Long] = {
    validateNonNull()
    validateKeyType(BrioLongKey)
    markKeyCaptured()
    longKeyVector
  }

  override
  def longMapValueSet: Array[Long] = {
    validateNonNull()
    validateValueType(BrioLongKey)
    markValueCaptured()
    longValueVector
  }

  override
  def doubleMapKeySet: Array[Double] = {
    validateNonNull()
    validateKeyType(BrioDoubleKey)
    markKeyCaptured()
    doubleKeyVector
  }

  override
  def doubleMapValueSet: Array[Double] = {
    validateNonNull()
    validateValueType(BrioDoubleKey)
    markValueCaptured()
    doubleValueVector
  }

  override
  def stringMapKeySet: Array[BrioDictionaryKey] = {
    validateNonNull()
    validateKeyType(BrioStringKey)
    markKeyCaptured()
    stringKeyVector
  }

  override
  def stringMapValueSet: Array[BrioDictionaryKey] = {
    validateNonNull()
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
    validateNonNull()
    validateValueType(BrioBooleanKey)
    markValueCaptured()
    booleanValueVector
  }

  override
  def byteVector: Array[Byte] = {
    validateNonNull()
    validateValueType(BrioByteKey)
    markValueCaptured()
    byteValueVector
  }

  override
  def shortVector: Array[Short] = {
    validateNonNull()
    validateValueType(BrioShortKey)
    markValueCaptured()
    shortValueVector
  }

  override
  def integerVector: Array[Int] = {
    validateNonNull()
    validateValueType(BrioIntegerKey)
    markValueCaptured()
    integerValueVector
  }

  override
  def longVector: Array[Long] = {
    validateNonNull()
    validateValueType(BrioLongKey)
    markValueCaptured()
    longValueVector
  }

  override
  def doubleVector: Array[Double] = {
    validateNonNull()
    validateValueType(BrioDoubleKey)
    markValueCaptured()
    doubleValueVector
  }

  override
  def stringVector: Array[BrioDictionaryKey] = {
    validateNonNull()
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

  private
  def validateValueType(bType: BrioTypeKey): Unit = {
    if (relationValueDataType != bType)
      throw VitalsException(s"'${brioDataTypeNameFromKey(bType)}' asserted where valueDataType='${brioDataTypeNameFromKey(relationValueDataType)}'")
  }

  private
  def validateKeyType(bType: BrioTypeKey): Unit = {
    if (relationKeyDataType != bType)
      throw VitalsException(s"'${brioDataTypeNameFromKey(bType)}' asserted where keyDataType='${brioDataTypeNameFromKey(relationKeyDataType)}'")
  }

}
