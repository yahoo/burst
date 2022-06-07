/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.model.schema.encoding

import org.burstsys.brio.types.BrioTypes._
import org.burstsys.tesla.TeslaTypes._

/**
  * This defines how an atomic value scalar relation is encoded/decoded in a brio blob
  */
trait BrioValueEncoding {

  /**
    * the value datatype key
    *
    * @return
    */
  def typeKey: BrioTypeKey

  /**
    * The value datatype name
    *
    * @return
    */
  def typeName: BrioTypeName

  /**
    * optional count of LSBs which are ignored, thus reducing numeric resolution without sacrificing number range
    *
    * @return
    */
  def bytes: Int

  /**
    * extension slot name (if used) This can be a lookup value or a offset value
    *
    * @return
    */
  def blur: BrioExtensionBlur = BrioExtensionNoBlur

  /**
    * a name that the schema will map to an extensionSlotOrdinal
    *
    * @return
    */
  def extensionSlotName: BrioExtensionSlotName = BrioExtensionSlotNoName

  /**
    * optional offset into the blob's extension table that contains a long value used by the extended type
    *
    * @return
    */
  def extensionSlotOrdinal: Int = BrioExtensionSlotNoOrdinal

}

object BrioNoValueEncoding extends BrioValueEncoding {
  val typeKey: BrioTypeKey = BrioAnyTypeKey
  val typeName: BrioTypeName = BrioAnyTypeName
  val bytes: Int = 0
}

object BrioBooleanValueEncoding extends BrioValueEncoding {
  val typeKey: BrioTypeKey = BrioBooleanKey
  val typeName: BrioTypeName = BrioBooleanName
  val bytes: Int = SizeOfBoolean
}

object BrioByteValueEncoding extends BrioValueEncoding {
  val typeKey: BrioTypeKey = BrioByteKey
  val typeName: BrioTypeName = BrioByteName
  val bytes: Int = SizeOfByte
}

object BrioShortValueEncoding extends BrioValueEncoding {
  val typeKey: BrioTypeKey = BrioShortKey
  val typeName: BrioTypeName = BrioShortName
  val bytes: Int = SizeOfShort
}

object BrioIntegerValueEncoding extends BrioValueEncoding {
  val typeKey: BrioTypeKey = BrioIntegerKey
  val typeName: BrioTypeName = BrioIntegerName
  val bytes: Int = SizeOfInteger
}

object BrioLongValueEncoding extends BrioValueEncoding {
  val typeKey: BrioTypeKey = BrioLongKey
  val typeName: BrioTypeName = BrioLongName
  val bytes: Int = SizeOfLong
}

object BrioDoubleValueEncoding extends BrioValueEncoding {
  val typeKey: BrioTypeKey = BrioDoubleKey
  val typeName: BrioTypeName = BrioDoubleName
  val bytes: Int = SizeOfDouble
}

object BrioStringValueEncoding extends BrioValueEncoding {
  val typeKey: BrioTypeKey = BrioStringKey
  val typeName: BrioTypeName = BrioStringName
  val bytes: Int = SizeOfString
}

// TODO EXTENDED TYPES
/**
  * Elastic values store in a variable size encoding in a specified number of bytes a LONG value (once decoded).
  * This value has an optional 'blur' offset added (shifted left) and an optional LONG 'offset' value added.
  * The result is presented as a LONG. The offset is stored in the [[org.burstsys.brio.blob.BrioBlob]]
  */
final case
class BrioElasticValueEncoding(bytes: Int, override val blur: BrioExtensionBlur = BrioExtensionNoBlur,
                               override val extensionSlotName: BrioExtensionSlotName = BrioExtensionSlotNoName,
                               override val extensionSlotOrdinal: BrioExtensionSlotOrdinal) extends BrioValueEncoding {
  val typeKey: BrioTypeKey = BrioElasticKey
  val typeName: BrioTypeName = BrioElasticName
}

// TODO EXTENDED TYPES
/**
  * Lookup values take a value stored in a variable number of bytes which is then mapped to an offset value
  * in the extensions table. The result is presented as a LONG. The lookup values are stored in the [[org.burstsys.brio.blob.BrioBlob]]
  */
final case
class BrioLookupValueEncoding(bytes: Int,
                              override val extensionSlotName: BrioExtensionSlotName = BrioExtensionSlotNoName,
                              override val extensionSlotOrdinal: BrioExtensionSlotOrdinal) extends BrioValueEncoding {
  val typeKey: BrioTypeKey = BrioElasticKey
  val typeName: BrioTypeName = BrioElasticName
}
