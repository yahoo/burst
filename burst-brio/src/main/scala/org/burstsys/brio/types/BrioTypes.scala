/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.types

import java.util.concurrent.atomic.AtomicInteger

import org.burstsys.brio.model.schema.types.BrioRelation
import org.burstsys.brio.types.BrioCourse._
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.tesla.TeslaTypes._

import scala.language.existentials
import scala.reflect.{ClassTag, classTag}

object BrioTypes {

  type BrioRelationOrdinal = Int

  final val BrioRootRelationOrdinal: BrioRelationOrdinal = -1

  type BrioRelationName = String

  type BrioRelationNameMap = Map[BrioRelationName, BrioRelation]

  type BrioRelationOrdinalMap = Map[BrioRelationOrdinal, BrioRelation]

  /////////////////////////////////////////////////////////////////////////////////////////////
  // Brio Schema Types
  /////////////////////////////////////////////////////////////////////////////////////////////

  type BrioSchemaName = String

  type BrioFieldNameKeyMap = gnu.trove.map.hash.TObjectByteHashMap[BrioRelationName]

  /////////////////////////////////////////////////////////////////////////////////////////////
  // Brio Dictionary Types
  /////////////////////////////////////////////////////////////////////////////////////////////

  type BrioDictionaryKey = Short

  type BrioWordSize = Short

  final val BrioDictionaryNotFound: BrioDictionaryKey = (-1).toShort

  /////////////////////////////////////////////////////////////////////////////////////////////
  // Brio Sizes
  /////////////////////////////////////////////////////////////////////////////////////////////

  // blobs are no bigger than 2,147,483,647 bytes
  type BrioCount = Short // collections/maps are no bigger than 32,767 members

  /**
   * Versions are encoded with an Int in Brio
   */
  final val SizeOfVersion = SizeOfInteger
  /**
   * Counts are encoded with a Short in Brio
   */
  final val SizeOfCount = SizeOfShort
  /**
   * Offsets are encoded with an Int in brio
   */
  final val SizeOfOffset = SizeOfInteger
  /**
   * Strings are encoded with a Short in Brio
   */
  final val SizeOfString = SizeOfShort
  // we use a dictionary and strings are stored as short keys max 32,767 words

  /////////////////////////////////////////////////////////////////////////////////////////////
  // Brio Types
  /////////////////////////////////////////////////////////////////////////////////////////////

  /**
    * We limit the domain and item keys to all subtypes of AnyVal and Strings
    * This is that new fangled existential typing mechanism. It even sometimes allows
    * you to cast an int to a null! This is one for Paul Phillips...
    */
  type BrioDataType = T forSome {type T >: AnyVal; type String}

  final val BrioRelationOrdinalNotFound: BrioRelationOrdinal = -1

  type BrioTypeName = String
  type BrioTypeKey = Int
  type BrioVersionKey = Int
  type BrioRelationCount = Int

  private[this] final
  val typeIndex = new AtomicInteger(0)

  /////////////////////////////////////////////////////////////////////////////////////////////
  // Value Scalars
  /////////////////////////////////////////////////////////////////////////////////////////////

  // no type yet defined - or any type value or reference (nuanced)
  final val BrioAnyTypeName: BrioTypeName = "Any"
  final val BrioAnyTypeKey: BrioTypeKey = -1

  // explicitly defined to be 'no type'
  final val BrioUnitTypeName: BrioTypeName = "Unit"
  final val BrioUnitTypeKey: BrioTypeKey = typeIndex.incrementAndGet

  // AnyVal types
  final val BrioBooleanName: BrioTypeName = "Boolean"
  final val BrioBooleanKey: BrioTypeKey = typeIndex.incrementAndGet

  final val BrioByteName: BrioTypeName = "Byte"
  final val BrioByteKey: BrioTypeKey = typeIndex.incrementAndGet

  final val BrioShortName: BrioTypeName = "Short"
  final val BrioShortKey: BrioTypeKey = typeIndex.incrementAndGet

  final val BrioIntegerName: BrioTypeName = "Integer"
  final val BrioIntegerKey: BrioTypeKey = typeIndex.incrementAndGet

  final val BrioLongName: BrioTypeName = "Long"
  final val BrioLongKey: BrioTypeKey = typeIndex.incrementAndGet

  final val BrioDoubleName: BrioTypeName = "Double"
  final val BrioDoubleKey: BrioTypeKey = typeIndex.incrementAndGet

  // neither fish nor fowl.  Strings are weird - they act like, but are not AnyVal.
  // and they are variable length. They are handled specially (dictionaries)
  final val BrioStringName: BrioTypeName = "String"
  final val BrioStringKey: BrioTypeKey = typeIndex.incrementAndGet

  /////////////////////////////////////////////////////////////////////////////////////////////
  // Courses
  /////////////////////////////////////////////////////////////////////////////////////////////
  final val SizeOfCourse = SizeOfLong

  final val BrioCourse32Name: BrioTypeName = "Course32"
  final val BrioCourse32Key: BrioTypeKey = typeIndex.incrementAndGet
  type Course32 = BrioCourse32

  final val BrioCourse16Name: BrioTypeName = "Course16"
  final val BrioCourse16Key: BrioTypeKey = typeIndex.incrementAndGet
  type Course16 = BrioCourse16

  final val BrioCourse8Name: BrioTypeName = "Course8"
  final val BrioCourse8Key: BrioTypeKey = typeIndex.incrementAndGet
  type Course8 = BrioCourse8

  final val BrioCourse4Name: BrioTypeName = "Course4"
  final val BrioCourse4Key: BrioTypeKey = typeIndex.incrementAndGet
  type Course4 = BrioCourse4

  /////////////////////////////////////////////////////////////////////////////////////////////
  // Extended Types
  /////////////////////////////////////////////////////////////////////////////////////////////

  // TODO EXTENDED TYPES
  type BrioExtensionSlotOrdinal = Int
  final val BrioExtensionSlotNoOrdinal: BrioExtensionSlotOrdinal = -1
  type BrioExtensionSlotName = String
  final val BrioExtensionSlotNoName: BrioExtensionSlotName = "NoSlotName"
  type BrioExtensionBlur = Int
  final val BrioExtensionNoBlur: BrioExtensionBlur = -1

  final val SizeOfElastic = SizeOfLong
  final val BrioElasticName: BrioTypeName = "Elastic"
  final val BrioElasticKey: BrioTypeKey = typeIndex.incrementAndGet
  type Elastic = Long

  final val SizeOfLookup = SizeOfLong
  final val BrioLookupName: BrioTypeName = "Lookup"
  final val BrioLookupKey: BrioTypeKey = typeIndex.incrementAndGet
  type Lookup = Long

  /////////////////////////////////////////////////////////////////////////////////////////////

  typeIndex.addAndGet(40) // leave room for more types

  /**
    * this is the first structure id after value 'built-in' types
    */
  final val FirstStructureType: BrioTypeKey = typeIndex.incrementAndGet

  /////////////////////////////////////////////////////////////////////////////////////////////
  // Brio Conversions
  /////////////////////////////////////////////////////////////////////////////////////////////

  /**
    * The size in bytes for underlying storage of a given Brio Datatype
    *
    * @tparam T
    * @return
    */
  final
  def brioDataTypeByteSize[T <: BrioDataType : ClassTag]: TeslaMemorySize = {
    brioDataTypeKey match {
      case BrioBooleanKey => SizeOfBoolean
      case BrioByteKey => SizeOfByte
      case BrioShortKey => SizeOfShort
      case BrioIntegerKey => SizeOfInteger
      case BrioLongKey => SizeOfLong
      case BrioDoubleKey => SizeOfDouble
      case BrioStringKey => SizeOfString
      case BrioCourse32Key => SizeOfLong
      case BrioCourse16Key => SizeOfLong
      case BrioCourse8Key => SizeOfLong
      case BrioCourse4Key => SizeOfLong
      case BrioElasticKey => SizeOfElastic
      case BrioLookupKey => SizeOfElastic
    }
  }

  final
  def brioDataTypeByteSize(bType: BrioTypeKey): TeslaMemorySize = {
    bType match {
      case BrioBooleanKey => SizeOfBoolean
      case BrioByteKey => SizeOfByte
      case BrioShortKey => SizeOfShort
      case BrioIntegerKey => SizeOfInteger
      case BrioLongKey => SizeOfLong
      case BrioDoubleKey => SizeOfDouble
      case BrioStringKey => SizeOfString
      case BrioCourse32Key => SizeOfLong
      case BrioCourse16Key => SizeOfLong
      case BrioCourse8Key => SizeOfLong
      case BrioCourse4Key => SizeOfLong
      case BrioLookupKey => SizeOfLookup
      case BrioElasticKey => SizeOfElastic
    }
  }

  /**
    * Used to distinguish between value and reference types
    *
    * @param id
    * @return
    */
  final
  def isBrioReferenceStructureType(id: BrioTypeKey): Boolean = id > FirstStructureType // TODO >= ???

  /**
    * The Brio Type key for a Java/Scala runtime primitive from an implicit
    * classtag
    *
    * @tparam T
    * @return
    */
  final
  def brioDataTypeFromClassTag[T <: BrioDataType : ClassTag]: BrioTypeKey = {
    val e = classTag[T].runtimeClass
    if (e == classOf[Boolean]) return BrioBooleanKey
    if (e == classOf[Byte]) return BrioByteKey
    if (e == classOf[Short]) return BrioShortKey
    if (e == classOf[Int]) return BrioIntegerKey
    if (e == classOf[Integer]) return BrioIntegerKey
    if (e == classOf[Long]) return BrioLongKey
    if (e == classOf[Double]) return BrioDoubleKey
    if (e == classOf[String]) return BrioStringKey
    if (e == classOf[BrioCourse32]) return BrioCourse32Key
    if (e == classOf[BrioCourse16]) return BrioCourse16Key
    if (e == classOf[BrioCourse8]) return BrioCourse8Key
    if (e == classOf[BrioCourse4]) return BrioCourse4Key
    // TODO EXTENDED TYPES
    throw VitalsException(s"unknown type '$e'")
  }

  /**
    * return a type id from a classtag
    *
    * @param t
    * @return
    */
  final
  def brioDataTypeKey(implicit t: ClassTag[_]): BrioTypeKey = {
    val e = t.runtimeClass
    keyIdentifier(e)
  }

  /**
    * return a type id from a runtime object
    *
    * @param value
    * @return
    */
  final
  def brioDataTypeKey(value: Any): BrioTypeKey = {
    val e = value.getClass
    keyIdentifier(e)
  }

  private
  def keyIdentifier(e: Any): BrioTypeKey = {
    if (e == classOf[java.lang.Boolean]) return BrioBooleanKey
    if (e == classOf[Boolean]) return BrioBooleanKey
    if (e == classOf[java.lang.Byte]) return BrioByteKey
    if (e == classOf[Byte]) return BrioByteKey
    if (e == classOf[java.lang.Short]) return BrioShortKey
    if (e == classOf[Short]) return BrioShortKey
    if (e == classOf[java.lang.Integer]) return BrioIntegerKey
    if (e == classOf[Int]) return BrioIntegerKey
    if (e == classOf[java.lang.Long]) return BrioLongKey
    if (e == classOf[Long]) return BrioLongKey
    if (e == classOf[java.lang.Double]) return BrioDoubleKey
    if (e == classOf[Double]) return BrioDoubleKey
    if (e == classOf[String]) return BrioStringKey
    if (e == classOf[BrioCourse32]) return BrioCourse32Key
    if (e == classOf[BrioCourse16]) return BrioCourse16Key
    if (e == classOf[BrioCourse8]) return BrioCourse8Key
    if (e == classOf[BrioCourse4]) return BrioCourse4Key
    // TODO EXTENDED TYPES
    throw VitalsException(s"unknown type '$e'")
  }

  /**
    * get a type id from a type name
    *
    * @param name
    * @return
    */
  final
  def brioDataTypeKeyFromName(name: BrioTypeName): BrioTypeKey = {
    name match {
      case BrioAnyTypeName => BrioAnyTypeKey
      case BrioUnitTypeName => BrioUnitTypeKey
      case BrioBooleanName => BrioBooleanKey
      case BrioByteName => BrioByteKey
      case BrioShortName => BrioShortKey
      case BrioIntegerName => BrioIntegerKey
      case BrioLongName => BrioLongKey
      case BrioDoubleName => BrioDoubleKey
      case BrioStringName => BrioStringKey
      case BrioCourse32Name => BrioCourse32Key
      case BrioCourse16Name => BrioCourse16Key
      case BrioCourse8Name => BrioCourse8Key
      case BrioCourse4Name => BrioCourse4Key
      case BrioElasticName => BrioElasticKey
      case BrioLookupName => BrioLookupKey
      case _ => throw VitalsException(s"unknown value type '$name'")
    }
  }

  /**
    * get a type name from a type key
    *
    * @param key
    * @return
    */
  final
  def brioDataTypeNameFromKey(key: BrioTypeKey): BrioTypeName = {
    key match {
      case BrioAnyTypeKey => BrioAnyTypeName
      case BrioUnitTypeKey => BrioUnitTypeName
      case BrioBooleanKey => BrioBooleanName
      case BrioByteKey => BrioByteName
      case BrioShortKey => BrioShortName
      case BrioIntegerKey => BrioIntegerName
      case BrioLongKey => BrioLongName
      case BrioDoubleKey => BrioDoubleName
      case BrioStringKey => BrioStringName
      case BrioCourse32Key => BrioCourse32Name
      case BrioCourse16Key => BrioCourse16Name
      case BrioCourse8Key => BrioCourse8Name
      case BrioCourse4Key => BrioCourse4Name
      case BrioElasticKey => BrioElasticName
      case BrioLookupKey => BrioLookupName
      case t =>
        if (isBrioReferenceStructureType(t)) "Structure"
        else
          throw VitalsException(s" unknown value type $key")
    }
  }

  /**
    * get a type name from a type key
    *
    * @param key
    * @return
    */
  final
  def classTagFromBrioTypeType(key: BrioTypeKey): ClassTag[_] = {
    key match {
      case BrioAnyTypeKey => ???
      case BrioUnitTypeKey => classTag[Unit]
      case BrioBooleanKey => classTag[Boolean]
      case BrioByteKey => classTag[Byte]
      case BrioShortKey => classTag[Short]
      case BrioIntegerKey => classTag[Int]
      case BrioLongKey => classTag[Long]
      case BrioDoubleKey => classTag[Double]
      case BrioStringKey => classTag[String]
      case t => throw VitalsException(s" unknown typekey=$key")
    }
  }

  final
  def defaultValueForBrioTypeKey(key: BrioTypeKey): Any = {
    key match {
      case BrioAnyTypeKey => null
      case BrioUnitTypeKey => null
      case BrioBooleanKey => false
      case BrioByteKey => 0.toByte
      case BrioShortKey => 0.toShort
      case BrioIntegerKey => 0
      case BrioLongKey => 0L
      case BrioDoubleKey => 0.0
      case BrioStringKey => null
      case t => throw VitalsException(s" unknown type key $key")
    }
  }

  final
  def defaultValueForBrioTypeKeyAsString(key: BrioTypeKey): String = {
    key match {
      case BrioAnyTypeKey => "null"
      case BrioUnitTypeKey => "null"
      case BrioBooleanKey => false.toString
      case BrioByteKey => 0.toByte.toString
      case BrioShortKey => 0.toShort.toString
      case BrioIntegerKey => 0.toString
      case BrioLongKey => 0L.toString
      case BrioDoubleKey => 0.0.toString
      case BrioStringKey => "null"
      case t => throw VitalsException(s" unknown type key $key")
    }
  }

  final
  def scalaTypeFromBrioTypeKey(key: BrioTypeKey): Class[_] = {
    key match {
      case BrioAnyTypeKey => classOf[Any]
      case BrioUnitTypeKey => classOf[Unit]
      case BrioBooleanKey => classOf[scala.Boolean]
      case BrioByteKey => classOf[scala.Byte]
      case BrioShortKey => classOf[scala.Short]
      case BrioIntegerKey => classOf[scala.Int]
      case BrioLongKey => classOf[scala.Long]
      case BrioDoubleKey => classOf[scala.Double]
      case BrioStringKey => classOf[String]
      case t => throw VitalsException(s" unknown type key $key")
    }
  }

  final
  def scalaTypeNameFromBrioTypeKey(key: BrioTypeKey): String = {
    key match {
      case BrioAnyTypeKey => "scala.Any"
      case BrioUnitTypeKey => "scala.Unit"
      case BrioBooleanKey => "scala.Boolean"
      case BrioByteKey => "scala.Byte"
      case BrioShortKey => "scala.Short"
      case BrioIntegerKey => "scala.Int"
      case BrioLongKey => "scala.Long"
      case BrioDoubleKey => "scala.Double"
      case BrioStringKey => "java.lang.String"
      case t => throw VitalsException(s" unknown type key $key")
    }
  }

  final
  def javaTypeFromBrioTypeKey(key: BrioTypeKey): Class[_] = {
    key match {
      case BrioAnyTypeKey => classOf[Any]
      case BrioUnitTypeKey => classOf[Unit]
      case BrioBooleanKey => classOf[java.lang.Boolean]
      case BrioByteKey => classOf[java.lang.Byte]
      case BrioShortKey => classOf[java.lang.Short]
      case BrioIntegerKey => classOf[java.lang.Integer]
      case BrioLongKey => classOf[java.lang.Long]
      case BrioDoubleKey => classOf[java.lang.Double]
      case BrioStringKey => classOf[java.lang.String]
      case t => throw VitalsException(s" unknown type key $key")
    }
  }

}
