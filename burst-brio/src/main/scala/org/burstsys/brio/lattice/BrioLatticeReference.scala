/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.lattice

import org.burstsys.brio.lattice.codec.BrioLongStringMapCodec.{lookupLongStringMap, lookupLongStringMapIsNull, lookupLongStringMapKeys, lookupLongStringMapSize}
import org.burstsys.brio.lattice.codec.BrioStringStringMapCodec.{lookupStringStringMap, lookupStringStringMapIsNull, lookupStringStringMapKeys, lookupStringStringMapSize}
import org.burstsys.brio.model.schema.encoding.BrioSchematic
import org.burstsys.brio.model.schema.types._
import org.burstsys.brio.types.BrioNulls
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.buffer.TeslaBufferReader
import org.burstsys.vitals.errors.VitalsException

/**
 * value class implementation for [[BrioLatticeReference]]
 * seems that you cannot split this into multiple files in any way I know and still have it not allocate objects
 *
 * @param startOffset
 */
final case
class BrioLatticeReference(startOffset: TeslaMemoryOffset = TeslaNullOffset) extends AnyVal {

  /////////////////////////////////////////////////////////////////////////////////////
  // Generic Field Operations
  /////////////////////////////////////////////////////////////////////////////////////

  @inline
  def isBlank: Boolean = startOffset == TeslaNullOffset

  /////////////////////////////////////////////////////////////////////////////////////
  // Access to size in a vector
  /////////////////////////////////////////////////////////////////////////////////////

  /**
   * write the size to just before this instance - only valid if this is in a
   * container such as a vector
   *
   * @param reader
   * @return
   */
  @inline
  def vectorMemberSize(reader: TeslaBufferReader): TeslaMemoryOffset = {
    reader.readOffset(startOffset - SizeOfOffset)
  }

  /////////////////////////////////////////////////////////////////////////////////////
  // VERSION FIELDS
  /////////////////////////////////////////////////////////////////////////////////////

  /**
   * return the version of this instance
   *
   * @param reader
   * @return
   */
  @inline
  def versionKey(reader: TeslaBufferReader): BrioVersionKey = reader.readInteger(startOffset)

  /**
   * Return the size of this relation
   *
   * @param relationOrdinal
   * @return
   */
  @inline
  def relationSize(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Int = {
    if (relationIsNull(reader, schematic, relationOrdinal))
      return 0
    schematic.form(relationOrdinal) match {
      case BrioValueScalarRelation => 1
      case BrioValueVectorRelation => valueVectorSize(reader, schematic, relationOrdinal)
      case BrioValueMapRelation => valueMapSize(reader, schematic, relationOrdinal)
      case BrioReferenceScalarRelation => 1
      case BrioReferenceVectorRelation => referenceVectorSize(reader, schematic, relationOrdinal)
      case _ => throw VitalsException(s"relationOrdinal=$relationOrdinal unknown relation")
    }
  }

  /**
   * test for nullity in a given relation
   *
   * @param reader
   * @param relationOrdinal
   * @return
   */
  @inline
  def relationIsNull(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Boolean = {
    BrioNulls.relationTestNull(reader, relationOrdinal, startOffset + schematic.nullsMapStart)
  }

  /////////////////////////////////////////////////////////////////////////////////////
  // Reference Scalar Ops
  /////////////////////////////////////////////////////////////////////////////////////

  /**
   * return a lattice type for a singular reference relationship
   *
   * @param reader
   * @param schematic
   * @param relationOrdinal
   * @return
   */
  @inline
  def referenceScalar(
                       reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal
                     ): BrioLatticeReference = {
    val offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    BrioLatticeReference(offset)
  }

  /////////////////////////////////////////////////////////////////////////////////////
  // Reference Vector Ops
  /////////////////////////////////////////////////////////////////////////////////////

  /**
   * return a lattice type for a plural reference relationship
   *
   * @param reader
   * @param schematic
   * @param relationOrdinal
   * @return
   */
  @inline
  def referenceVectorIterator(
                               reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal
                             ): BrioLatticeRefVecIterator = {
    schematic.variableRelationOffsetKeys(relationOrdinal) match {
      case BrioRelationOrdinalNotFound =>
        throw VitalsException(s"lattice reference vector relationOrdinal '$relationOrdinal' not found")
      case variableFieldKey =>
        val variableFieldLocation: TeslaMemoryOffset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
        BrioLatticeRefVecIterator(variableFieldLocation)
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////
  // Value Vector
  /////////////////////////////////////////////////////////////////////////////////////

  @inline
  def valueVectorIterator(
                           reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal
                         ): BrioLatticeValVecIterator = {
    val offset: TeslaMemoryOffset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    BrioLatticeValVecIterator(offset)
  }

  /////////////////////////////////////////////////////////////////////////////////////
  // VALUE VECTOR SIZE
  /////////////////////////////////////////////////////////////////////////////////////

  @inline
  def valueVectorSize(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Int = {
    val offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    val valueEncoding = schematic.valueEncoding(relationOrdinal)
    reader.readShort(offset)
  }

  /////////////////////////////////////////////////////////////////////////////////////
  // VALUE VECTOR FIELDS
  /////////////////////////////////////////////////////////////////////////////////////

  @inline
  def valueVectorBoolean(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[Boolean] = {
    var offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    val valueEncoding = schematic.valueEncoding(relationOrdinal)
    val count = reader.readShort(offset)
    offset += SizeOfCount
    reader.readBooleans(offset, count)
  }

  @inline
  def valueVectorBooleanContains(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, value: Boolean): Boolean = {
    var offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    val valueEncoding = schematic.valueEncoding(relationOrdinal)
    val count = reader.readShort(offset)
    offset += SizeOfCount
    reader.readBooleanInArray(offset, count, value)
  }

  @inline
  def valueVectorByte(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[Byte] = {
    var offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    val valueEncoding = schematic.valueEncoding(relationOrdinal)
    val count = reader.readShort(offset)
    offset += SizeOfCount
    reader.readBytes(offset, count)
  }

  @inline
  def valueVectorByteContains(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, value: Byte): Boolean = {
    var offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    val valueEncoding = schematic.valueEncoding(relationOrdinal)
    val count = reader.readShort(offset)
    offset += SizeOfCount
    reader.readByteInArray(offset, count, value)
  }

  @inline
  def valueVectorShort(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[Short] = {
    var offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    val valueEncoding = schematic.valueEncoding(relationOrdinal)
    val count = reader.readShort(offset)
    offset += SizeOfCount
    reader.readShorts(offset, count)
  }

  @inline
  def valueVectorShortContains(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, value: Short): Boolean = {
    var offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    val valueEncoding = schematic.valueEncoding(relationOrdinal)
    val count = reader.readShort(offset)
    offset += SizeOfCount
    reader.readShortInArray(offset, count, value)
  }

  @inline
  def valueVectorInt(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[Int] = {
    var offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    val valueEncoding = schematic.valueEncoding(relationOrdinal)
    val count = reader.readShort(offset)
    offset += SizeOfCount
    reader.readIntegers(offset, count)
  }

  @inline
  def valueVectorIntContains(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, value: Int): Boolean = {
    var offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    val valueEncoding = schematic.valueEncoding(relationOrdinal)
    val count = reader.readShort(offset)
    offset += SizeOfCount
    reader.readIntegerInArray(offset, count, value)
  }

  @inline
  def valueVectorLong(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[Long] = {
    var offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    val valueEncoding = schematic.valueEncoding(relationOrdinal)
    val count = reader.readShort(offset)
    offset += SizeOfCount
    reader.readLongs(offset, count)
  }

  @inline
  def valueVectorLongContains(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, value: Long): Boolean = {
    var offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    val valueEncoding = schematic.valueEncoding(relationOrdinal)
    val count = reader.readShort(offset)
    offset += SizeOfCount
    reader.readLongInArray(offset, count, value)
  }

  @inline
  def valueVectorDouble(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[Double] = {
    var offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    val valueEncoding = schematic.valueEncoding(relationOrdinal)
    val count = reader.readShort(offset)
    offset += SizeOfCount
    reader.readDoubles(offset, count)
  }

  @inline
  def valueVectorDoubleContains(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, value: Double): Boolean = {
    var offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    val valueEncoding = schematic.valueEncoding(relationOrdinal)
    val count = reader.readShort(offset)
    offset += SizeOfCount
    reader.readDoubleInArray(offset, count, value)
  }

  @inline
  def valueVectorString(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[BrioDictionaryKey] = {
    var offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    val valueEncoding = schematic.valueEncoding(relationOrdinal)
    val count = reader.readShort(offset)
    offset += SizeOfCount
    reader.readShorts(offset, count)
  }

  @inline
  def valueVectorElastic(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[Long] = {
    var offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    val valueEncoding = schematic.valueEncoding(relationOrdinal)
    val count = reader.readShort(offset)
    offset += SizeOfCount
    // TODO EXTENDED TYPES
    ???
  }

  @inline
  def valueVectorLookupContains(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, value: Long): Boolean = {
    var offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    val valueEncoding = schematic.valueEncoding(relationOrdinal)
    val count = reader.readShort(offset)
    offset += SizeOfCount
    // TODO EXTENDED TYPES
    ???
  }


  /////////////////////////////////////////////////////////////////////////////////////
  // Value Scalar
  /////////////////////////////////////////////////////////////////////////////////////

  @inline
  def referenceVectorSize(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Int = {
    val offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    reader.readShort(offset)
  }

  @inline
  def valueScalarBoolean(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Boolean = {
    reader.readBoolean(startOffset + schematic.fixedRelationsOffsets(relationOrdinal))
  }

  @inline
  def valueScalarByte(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Byte = {
    reader.readByte(startOffset + schematic.fixedRelationsOffsets(relationOrdinal))
  }

  @inline
  def valueScalarShort(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Short = {
    reader.readShort(startOffset + schematic.fixedRelationsOffsets(relationOrdinal))
  }

  @inline
  def valueScalarInteger(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Int = {
    reader.readInteger(startOffset + schematic.fixedRelationsOffsets(relationOrdinal))
  }

  @inline
  def valueScalarLong(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Long = {
    reader.readLong(startOffset + schematic.fixedRelationsOffsets(relationOrdinal))
  }

  @inline
  def valueScalarDouble(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Double = {
    reader.readDouble(startOffset + schematic.fixedRelationsOffsets(relationOrdinal))
  }

  @inline
  def valueScalarString(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): BrioDictionaryKey = {
    reader.readShort(startOffset + schematic.fixedRelationsOffsets(relationOrdinal))
  }

  @inline
  def valueScalarElastic(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Long = {
    // TODO EXTENDED TYPES
    ???
  }

  @inline
  def valueScalarLookup(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Long = {
    // TODO EXTENDED TYPES
    ???
  }


  /////////////////////////////////////////////////////////////////////////////////////
  // Value Map
  /////////////////////////////////////////////////////////////////////////////////////

  @inline
  def valueMapIterator(
                        reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal
                      ): BrioLatticeValMapIterator = {
    val offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    BrioLatticeValMapIterator(offset)
  }

  @inline
  def valueMapSize(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Int = {
    schematic.mapTypeKey(relationOrdinal) match {
      case BrioStringKey =>
        schematic.valueTypeKey(relationOrdinal) match {
          case BrioStringKey => valueMapStringStringSize(reader, schematic, relationOrdinal)
          case _ => throw VitalsException(s"unsupported value map type ${schematic.mapTypeKey(relationOrdinal)}->${schematic.valueTypeKey(relationOrdinal)}")
        }
      case BrioLongKey =>
        schematic.valueTypeKey(relationOrdinal) match {
          case BrioStringKey => valueMapLongStringSize(reader, schematic, relationOrdinal)
          case _ => throw VitalsException(s"unsupported value map type ${schematic.mapTypeKey(relationOrdinal)}->${schematic.valueTypeKey(relationOrdinal)}")
        }
      case _ => throw VitalsException(s"unsupported value map type ${schematic.mapTypeKey(relationOrdinal)}->${schematic.valueTypeKey(relationOrdinal)}")
    }
  }

  /** **************************************************************
   * Boolean-Boolean Maps
   * ************************************************************** */

  @inline
  def valueMapBooleanBooleanKeys(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[Boolean] = ???

  /////////////////////////////////////////////////////////////////////////////////////
  // VALUE MAP FIELDS
  /////////////////////////////////////////////////////////////////////////////////////

  @inline
  def valueMapBooleanBoolean(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Boolean): Boolean = ???

  @inline
  def valueMapBooleanBooleanIsNull(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Boolean): Boolean = ???

  /** **************************************************************
   * Byte-Boolean Maps
   * ************************************************************** */

  @inline
  def valueMapByteBooleanKeys(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[Byte] = ???

  @inline
  def valueMapByteBoolean(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Byte): Boolean = ???

  @inline
  def valueMapByteBooleanIsNull(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Byte): Boolean = ???

  /** **************************************************************
   * Short-Boolean Maps
   * ************************************************************** */

  @inline
  def valueMapShortBooleanKeys(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[Short] = ???

  @inline
  def valueMapShortBoolean(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Short): Boolean =
    ???

  @inline
  def valueMapShortBooleanIsNull(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Short): Boolean =
    ???

  /** **************************************************************
   * Int-Boolean Maps
   * ************************************************************** */

  @inline
  def valueMapIntBooleanKeys(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[Int] =
    ???

  @inline
  def valueMapIntBoolean(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Int): Boolean =
    ???

  @inline
  def valueMapIntBooleanIsNull(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Int): Boolean =
    ???

  /** **************************************************************
   * Long-Boolean Maps
   * ************************************************************** */

  @inline
  def valueMapLongBooleanKeys(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[Long] =
    ???

  @inline
  def valueMapLongBoolean(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Long): Boolean =
    ???

  @inline
  def valueMapLongBooleanIsNull(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Long): Boolean =
    ???

  /** **************************************************************
   * Double-Boolean Maps
   * ************************************************************** */
  @inline
  def valueMapDoubleBooleanKeys(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[Double] =
    ???

  @inline
  def valueMapDoubleBoolean(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Double): Boolean =
    ???

  @inline
  def valueMapDoubleBooleanIsNull(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Double): Boolean =
    ???

  /** **************************************************************
   * String-Boolean Maps
   * ************************************************************** */

  @inline
  def valueMapStringBooleanKeys(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[BrioDictionaryKey] =
    ???

  @inline
  def valueMapStringBoolean(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: BrioDictionaryKey): Boolean =
    ???

  @inline
  def valueMapStringBooleanIsNull(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: BrioDictionaryKey): Boolean =
    ???

  /** **************************************************************
   * Boolean-Byte Maps
   * ************************************************************** */
  @inline
  def valueMapBooleanByteKeys(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[Boolean] =
    ???

  @inline
  def valueMapBooleanByte(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Boolean): Byte =
    ???

  @inline
  def valueMapBooleanByteIsNull(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Boolean): Boolean =
    ???

  /** **************************************************************
   * Byte-Byte Maps
   * ************************************************************** */

  @inline
  def valueMapByteByteKeys(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[Byte] =
    ???

  @inline
  def valueMapByteByte(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Byte): Byte =
    ???

  @inline
  def valueMapByteByteIsNull(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Byte): Boolean =
    ???

  /** **************************************************************
   * Short-Byte Maps
   * ************************************************************** */

  @inline
  def valueMapShortByteKeys(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[Short] =
    ???

  @inline
  def valueMapShortByte(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Short): Byte =
    ???

  @inline
  def valueMapShortByteIsNull(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Short): Boolean =
    ???

  /** **************************************************************
   * Int-Byte Maps
   * ************************************************************** */

  @inline
  def valueMapIntByteKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                          relationOrdinal: BrioRelationOrdinal): Array[Int] =
    ???

  @inline
  def valueMapIntByte(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Int): Byte =
    ???

  @inline
  def valueMapIntByteIsNull(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Int): Boolean =
    ???

  /** **************************************************************
   * Long-Byte Maps
   * ************************************************************** */

  @inline
  def valueMapLongByteKeys(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[Long] =
    ???

  @inline
  def valueMapLongByte(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Long): Byte =
    ???

  @inline
  def valueMapLongByteIsNull(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal, mapKey: Long): Boolean =
    ???

  /** **************************************************************
   * Double-Byte Maps
   * ************************************************************** */

  @inline
  def valueMapDoubleByteKeys(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Array[Double] =
    ???

  @inline
  def valueMapDoubleByte(reader: TeslaBufferReader, schematic: BrioSchematic,
                         relationOrdinal: BrioRelationOrdinal, mapKey: Double): Byte =
    ???

  @inline
  def valueMapDoubleByteIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                               relationOrdinal: BrioRelationOrdinal, mapKey: Double): Boolean =
    ???

  /** **************************************************************
   * String-Byte Maps
   * ************************************************************** */

  @inline
  def valueMapStringByteKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                             relationOrdinal: BrioRelationOrdinal): Array[BrioDictionaryKey] =
    ???

  @inline
  def valueMapStringByte(reader: TeslaBufferReader, schematic: BrioSchematic,
                         relationOrdinal: BrioRelationOrdinal, mapKey: BrioDictionaryKey): Byte =
    ???

  @inline
  def valueMapStringByteIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                               relationOrdinal: BrioRelationOrdinal, mapKey: BrioDictionaryKey): Boolean =
    ???

  /** **************************************************************
   * Boolean-Short Maps
   * ************************************************************** */

  @inline
  def valueMapBooleanShortKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                               relationOrdinal: BrioRelationOrdinal): Array[Boolean] =
    ???

  @inline
  def valueMapBooleanShort(reader: TeslaBufferReader, schematic: BrioSchematic,
                           relationOrdinal: BrioRelationOrdinal, mapKey: Boolean): Short =
    ???

  @inline
  def valueMapBooleanShortIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                                 relationOrdinal: BrioRelationOrdinal, mapKey: Boolean): Boolean =
    ???

  /** **************************************************************
   * Byte-Short Maps
   * ************************************************************** */

  @inline
  def valueMapByteShortKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                            relationOrdinal: BrioRelationOrdinal): Array[Byte] =
    ???

  @inline
  def valueMapByteShort(reader: TeslaBufferReader, schematic: BrioSchematic,
                        relationOrdinal: BrioRelationOrdinal, mapKey: Byte): Short =
    ???

  @inline
  def valueMapByteShortIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                              relationOrdinal: BrioRelationOrdinal, mapKey: Byte): Boolean =
    ???

  /** **************************************************************
   * Short-Short Maps
   * ************************************************************** */

  @inline
  def valueMapShortShortKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                             relationOrdinal: BrioRelationOrdinal): Array[Short] =
    ???

  @inline
  def valueMapShortShort(reader: TeslaBufferReader, schematic: BrioSchematic,
                         relationOrdinal: BrioRelationOrdinal, mapKey: Short): Short =
    ???

  @inline
  def valueMapShortShortIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                               relationOrdinal: BrioRelationOrdinal, mapKey: Short): Boolean =
    ???

  /** **************************************************************
   * Int-Short Maps
   * ************************************************************** */

  @inline
  def valueMapIntShortKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                           relationOrdinal: BrioRelationOrdinal): Array[Int] =
    ???

  @inline
  def valueMapIntShort(reader: TeslaBufferReader, schematic: BrioSchematic,
                       relationOrdinal: BrioRelationOrdinal, mapKey: Int): Short =
    ???

  @inline
  def valueMapIntShortIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                             relationOrdinal: BrioRelationOrdinal, mapKey: Int): Boolean =
    ???

  /** **************************************************************
   * Long-Short Maps
   * ************************************************************** */

  @inline
  def valueMapLongShortKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                            relationOrdinal: BrioRelationOrdinal): Array[Long] =
    ???

  @inline
  def valueMapLongShort(reader: TeslaBufferReader, schematic: BrioSchematic,
                        relationOrdinal: BrioRelationOrdinal, mapKey: Long): Short =
    ???

  @inline
  def valueMapLongShortIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                              relationOrdinal: BrioRelationOrdinal, mapKey: Long): Boolean =
    ???

  /** **************************************************************
   * Double-Short Maps
   * ************************************************************** */

  @inline
  def valueMapDoubleShortKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                              relationOrdinal: BrioRelationOrdinal): Array[Double] =
    ???

  @inline
  def valueMapDoubleShort(reader: TeslaBufferReader, schematic: BrioSchematic,
                          relationOrdinal: BrioRelationOrdinal, mapKey: Double): Short =
    ???

  @inline
  def valueMapDoubleShortIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                                relationOrdinal: BrioRelationOrdinal, mapKey: Double): Boolean =
    ???

  /** **************************************************************
   * String-Short Maps
   * ************************************************************** */

  @inline
  def valueMapStringShortKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                              relationOrdinal: BrioRelationOrdinal): Array[BrioDictionaryKey] =
    ???

  @inline
  def valueMapStringShort(reader: TeslaBufferReader, schematic: BrioSchematic,
                          relationOrdinal: BrioRelationOrdinal, mapKey: BrioDictionaryKey): Short =
    ???

  @inline
  def valueMapStringShortIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                                relationOrdinal: BrioRelationOrdinal, mapKey: BrioDictionaryKey): Boolean =
    ???

  /** **************************************************************
   * Boolean-Int Maps
   * ************************************************************** */

  @inline
  def valueMapBooleanIntKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                             relationOrdinal: BrioRelationOrdinal): Array[Boolean] =
    ???

  @inline
  def valueMapBooleanInt(reader: TeslaBufferReader, schematic: BrioSchematic,
                         relationOrdinal: BrioRelationOrdinal, mapKey: Boolean): Int =
    ???

  @inline
  def valueMapBooleanIntIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                               relationOrdinal: BrioRelationOrdinal, mapKey: Boolean): Boolean =
    ???

  /** **************************************************************
   * Byte-Int Maps
   * ************************************************************** */

  @inline
  def valueMapByteIntKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                          relationOrdinal: BrioRelationOrdinal): Array[Byte] =
    ???

  @inline
  def valueMapByteInt(reader: TeslaBufferReader, schematic: BrioSchematic,
                      relationOrdinal: BrioRelationOrdinal, mapKey: Byte): Int =
    ???

  @inline
  def valueMapByteIntIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                            relationOrdinal: BrioRelationOrdinal, mapKey: Byte): Boolean =
    ???

  /** **************************************************************
   * Short-Int Maps
   * ************************************************************** */

  @inline
  def valueMapShortIntKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                           relationOrdinal: BrioRelationOrdinal): Array[Short] =
    ???

  @inline
  def valueMapShortInt(reader: TeslaBufferReader, schematic: BrioSchematic,
                       relationOrdinal: BrioRelationOrdinal, mapKey: Short): Int =
    ???

  @inline
  def valueMapShortIntIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                             relationOrdinal: BrioRelationOrdinal, mapKey: Short): Boolean =
    ???

  /** **************************************************************
   * Int-Int Maps
   * ************************************************************** */

  @inline
  def valueMapIntIntKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                         relationOrdinal: BrioRelationOrdinal): Array[Int] =
    ???

  @inline
  def valueMapIntInt(reader: TeslaBufferReader, schematic: BrioSchematic,
                     relationOrdinal: BrioRelationOrdinal, mapKey: Int): Int =
    ???

  @inline
  def valueMapIntIntIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                           relationOrdinal: BrioRelationOrdinal, mapKey: Int): Boolean =
    ???

  /** **************************************************************
   * Long-Int Maps
   * ************************************************************** */

  @inline
  def valueMapLongIntKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                          relationOrdinal: BrioRelationOrdinal): Array[Long] =
    ???

  @inline
  def valueMapLongInt(reader: TeslaBufferReader, schematic: BrioSchematic,
                      relationOrdinal: BrioRelationOrdinal, mapKey: Long): Int =
    ???

  @inline
  def valueMapLongIntIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                            relationOrdinal: BrioRelationOrdinal, mapKey: Long): Boolean =
    ???

  /** **************************************************************
   * Double-Int Maps
   * ************************************************************** */

  @inline
  def valueMapDoubleIntKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                            relationOrdinal: BrioRelationOrdinal): Array[Double] =
    ???


  @inline
  def valueMapDoubleInt(reader: TeslaBufferReader, schematic: BrioSchematic,
                        relationOrdinal: BrioRelationOrdinal, mapKey: Double): Int =
    ???

  @inline
  def valueMapDoubleIntIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                              relationOrdinal: BrioRelationOrdinal, mapKey: Double): Boolean =
    ???

  /** **************************************************************
   * String-Int Maps
   * ************************************************************** */

  @inline
  def valueMapStringIntKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                            relationOrdinal: BrioRelationOrdinal): Array[BrioDictionaryKey] =
    ???

  @inline
  def valueMapStringInt(reader: TeslaBufferReader, schematic: BrioSchematic,
                        relationOrdinal: BrioRelationOrdinal, mapKey: BrioDictionaryKey): Int =
    ???

  @inline
  def valueMapStringIntIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                              relationOrdinal: BrioRelationOrdinal, mapKey: BrioDictionaryKey): Boolean =
    ???

  /** **************************************************************
   * Boolean-Long Int Maps
   * ************************************************************** */

  @inline
  def valueMapBooleanLongKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                              relationOrdinal: BrioRelationOrdinal): Array[Boolean] =
    ???

  @inline
  def valueMapBooleanLong(reader: TeslaBufferReader, schematic: BrioSchematic,
                          relationOrdinal: BrioRelationOrdinal, mapKey: Boolean): Long =
    ???

  @inline
  def valueMapBooleanLongIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                                relationOrdinal: BrioRelationOrdinal, mapKey: Boolean): Boolean =
    ???

  /** **************************************************************
   * Byte-Long Int Maps
   * ************************************************************** */

  @inline
  def valueMapByteLongKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                           relationOrdinal: BrioRelationOrdinal): Array[Byte] =
    ???

  @inline
  def valueMapByteLong(reader: TeslaBufferReader, schematic: BrioSchematic,
                       relationOrdinal: BrioRelationOrdinal, mapKey: Byte): Long =
    ???

  @inline
  def valueMapByteLongIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                             relationOrdinal: BrioRelationOrdinal, mapKey: Byte): Boolean =
    ???

  /** **************************************************************
   * Short-Long Int Maps
   * ************************************************************** */

  @inline
  def valueMapShortLongKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                            relationOrdinal: BrioRelationOrdinal): Array[Short] =
    ???

  @inline
  def valueMapShortLong(reader: TeslaBufferReader, schematic: BrioSchematic,
                        relationOrdinal: BrioRelationOrdinal, mapKey: Short): Long =
    ???

  @inline
  def valueMapShortLongIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                              relationOrdinal: BrioRelationOrdinal, mapKey: Short): Boolean =
    ???

  /** **************************************************************
   * Int-Long Int Maps
   * ************************************************************** */

  @inline
  def valueMapIntLongKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                          relationOrdinal: BrioRelationOrdinal): Array[Int] =
    ???

  @inline
  def valueMapIntLong(reader: TeslaBufferReader, schematic: BrioSchematic,
                      relationOrdinal: BrioRelationOrdinal, mapKey: Int): Long =
    ???

  @inline
  def valueMapIntLongIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                            relationOrdinal: BrioRelationOrdinal, mapKey: Int): Boolean =
    ???

  /** **************************************************************
   * Long-Long Int Maps
   * ************************************************************** */

  @inline
  def valueMapLongLongKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                           relationOrdinal: BrioRelationOrdinal): Array[Long] =
    ???

  @inline
  def valueMapLongLong(reader: TeslaBufferReader, schematic: BrioSchematic,
                       relationOrdinal: BrioRelationOrdinal, mapKey: Long): Long =
    ???

  @inline
  def valueMapLongLongIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                             relationOrdinal: BrioRelationOrdinal, mapKey: Long): Boolean =
    ???

  /** **************************************************************
   * Double-Long Int Maps
   * ************************************************************** */

  @inline
  def valueMapDoubleLongKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                             relationOrdinal: BrioRelationOrdinal): Array[Double] =
    ???

  @inline
  def valueMapDoubleLong(reader: TeslaBufferReader, schematic: BrioSchematic,
                         relationOrdinal: BrioRelationOrdinal, mapKey: Double): Long =
    ???

  @inline
  def valueMapDoubleLongIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                               relationOrdinal: BrioRelationOrdinal, mapKey: Double): Boolean =
    ???

  /** **************************************************************
   * String-Long Int Maps
   * ************************************************************** */

  @inline
  def valueMapStringLongKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                             relationOrdinal: BrioRelationOrdinal): Array[BrioDictionaryKey] =
    ???

  @inline
  def valueMapStringLong(reader: TeslaBufferReader, schematic: BrioSchematic,
                         relationOrdinal: BrioRelationOrdinal, mapKey: BrioDictionaryKey): Long =
    ???

  @inline
  def valueMapStringLongIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                               relationOrdinal: BrioRelationOrdinal, mapKey: BrioDictionaryKey): Boolean =
    ???

  /** **************************************************************
   * Boolean-Double Int Maps
   * ************************************************************** */

  @inline
  def valueMapBooleanDoubleKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                                relationOrdinal: BrioRelationOrdinal): Array[Boolean] =
    ???

  @inline
  def valueMapBooleanDouble(reader: TeslaBufferReader, schematic: BrioSchematic,
                            relationOrdinal: BrioRelationOrdinal, mapKey: Boolean): Double =
    ???

  @inline
  def valueMapBooleanDoubleIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                                  relationOrdinal: BrioRelationOrdinal, mapKey: Boolean): Boolean =
    ???

  /** **************************************************************
   * Byte-Double Int Maps
   * ************************************************************** */

  @inline
  def valueMapByteDoubleKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                             relationOrdinal: BrioRelationOrdinal): Array[Byte] =
    ???

  @inline
  def valueMapByteDouble(reader: TeslaBufferReader, schematic: BrioSchematic,
                         relationOrdinal: BrioRelationOrdinal, mapKey: Byte): Double =
    ???

  @inline
  def valueMapByteDoubleIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                               relationOrdinal: BrioRelationOrdinal, mapKey: Byte): Boolean =
    ???

  /** **************************************************************
   * Short-Double Int Maps
   * ************************************************************** */

  @inline
  def valueMapShortDoubleKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                              relationOrdinal: BrioRelationOrdinal): Array[Short] =
    ???

  @inline
  def valueMapShortDouble(reader: TeslaBufferReader, schematic: BrioSchematic,
                          relationOrdinal: BrioRelationOrdinal, mapKey: Short): Double =
    ???

  @inline
  def valueMapShortDoubleIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                                relationOrdinal: BrioRelationOrdinal, mapKey: Short): Boolean =
    ???

  /** **************************************************************
   * Int-Double Int Maps
   * ************************************************************** */

  @inline
  def valueMapIntDoubleKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                            relationOrdinal: BrioRelationOrdinal): Array[Int] =
    ???

  @inline
  def valueMapIntDouble(reader: TeslaBufferReader, schematic: BrioSchematic,
                        relationOrdinal: BrioRelationOrdinal, mapKey: Int): Double =
    ???

  @inline
  def valueMapIntDoubleIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                              relationOrdinal: BrioRelationOrdinal, mapKey: Int): Boolean =
    ???

  /** **************************************************************
   * Long-Double Int Maps
   * ************************************************************** */

  @inline
  def valueMapLongDoubleKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                             relationOrdinal: BrioRelationOrdinal): Array[Long] =
    ???

  @inline
  def valueMapLongDouble(reader: TeslaBufferReader, schematic: BrioSchematic,
                         relationOrdinal: BrioRelationOrdinal, mapKey: Long): Double =
    ???

  @inline
  def valueMapLongDoubleIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                               relationOrdinal: BrioRelationOrdinal, mapKey: Long): Boolean =
    ???

  /** **************************************************************
   * Double-Double Int Maps
   * ************************************************************** */

  @inline
  def valueMapDoubleDoubleKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                               relationOrdinal: BrioRelationOrdinal): Array[Double] =
    ???

  @inline
  def valueMapDoubleDouble(reader: TeslaBufferReader, schematic: BrioSchematic,
                           relationOrdinal: BrioRelationOrdinal, mapKey: Double): Double =
    ???

  @inline
  def valueMapDoubleDoubleIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                                 relationOrdinal: BrioRelationOrdinal, mapKey: Double): Boolean =
    ???

  /** **************************************************************
   * String-Double Int Maps
   * ************************************************************** */

  @inline
  def valueMapStringDoubleKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                               relationOrdinal: BrioRelationOrdinal): Array[BrioDictionaryKey] =
    ???

  @inline
  def valueMapStringDouble(reader: TeslaBufferReader, schematic: BrioSchematic,
                           relationOrdinal: BrioRelationOrdinal, mapKey: BrioDictionaryKey): Double =
    ???

  @inline
  def valueMapStringDoubleIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                                 relationOrdinal: BrioRelationOrdinal, mapKey: BrioDictionaryKey): Boolean =
    ???

  /** **************************************************************
   * Boolean-String Int Maps
   * ************************************************************** */

  @inline
  def valueMapBooleanStringKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                                relationOrdinal: BrioRelationOrdinal): Array[Boolean] =
    ???

  @inline
  def valueMapBooleanString(reader: TeslaBufferReader, schematic: BrioSchematic,
                            relationOrdinal: BrioRelationOrdinal, mapKey: Boolean): BrioDictionaryKey =
    ???

  @inline
  def valueMapBooleanStringIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                                  relationOrdinal: BrioRelationOrdinal, mapKey: Boolean): Boolean =
    ???

  /** **************************************************************
   * Byte-String Int Maps
   * ************************************************************** */
  @inline
  def valueMapByteStringKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                             relationOrdinal: BrioRelationOrdinal): Array[Byte] =
    ???

  @inline
  def valueMapByteString(reader: TeslaBufferReader, schematic: BrioSchematic,
                         relationOrdinal: BrioRelationOrdinal, mapKey: Byte): BrioDictionaryKey =
    ???

  @inline
  def valueMapByteStringIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                               relationOrdinal: BrioRelationOrdinal, mapKey: Byte): Boolean =
    ???

  /** **************************************************************
   * Short-String Int Maps
   * ************************************************************** */
  @inline
  def valueMapShortStringKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                              relationOrdinal: BrioRelationOrdinal): Array[Short] =
    ???

  @inline
  def valueMapShortString(reader: TeslaBufferReader, schematic: BrioSchematic,
                          relationOrdinal: BrioRelationOrdinal, mapKey: Short): BrioDictionaryKey =
    ???

  @inline
  def valueMapShortStringIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                                relationOrdinal: BrioRelationOrdinal, mapKey: Short): Boolean =
    ???

  /** **************************************************************
   * Int-String Int Maps
   * ************************************************************** */
  @inline
  def valueMapIntStringKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                            relationOrdinal: BrioRelationOrdinal): Array[Int] =
    ???

  @inline
  def valueMapIntString(reader: TeslaBufferReader, schematic: BrioSchematic,
                        relationOrdinal: BrioRelationOrdinal, mapKey: Int): BrioDictionaryKey =
    ???

  @inline
  def valueMapIntStringIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                              relationOrdinal: BrioRelationOrdinal, mapKey: Int): Boolean =
    ???

  /** **************************************************************
   * Long-String Maps
   * ************************************************************** */
  @inline
  def valueMapLongStringKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                             relationOrdinal: BrioRelationOrdinal): Array[Long] = {
    val offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    lookupLongStringMapKeys(reader, offset)
  }

  @inline
  def valueMapLongString(reader: TeslaBufferReader, schematic: BrioSchematic,
                         relationOrdinal: BrioRelationOrdinal, mapKey: Long): BrioDictionaryKey = {
    val offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    lookupLongStringMap(mapKey, reader, offset)
  }

  @inline
  def valueMapLongStringIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                               relationOrdinal: BrioRelationOrdinal, mapKey: Long): Boolean = {
    val offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    lookupLongStringMapIsNull(mapKey, reader, offset)
  }

  @inline
  def valueMapLongStringSize(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Int = {
    val offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    lookupLongStringMapSize(reader, offset)
  }

  /** **************************************************************
   * Double-String Int Maps
   * ************************************************************** */

  @inline
  def valueMapDoubleStringKeys(reader: TeslaBufferReader, schematic: BrioSchematic,
                               relationOrdinal: BrioRelationOrdinal): Array[Double] =
    ???

  @inline
  def valueMapDoubleString(reader: TeslaBufferReader, schematic: BrioSchematic,
                           relationOrdinal: BrioRelationOrdinal, mapKey: Double): BrioDictionaryKey =
    ???

  @inline
  def valueMapDoubleStringIsNull(reader: TeslaBufferReader, schematic: BrioSchematic,
                                 relationOrdinal: BrioRelationOrdinal, mapKey: Double): Boolean =
    ???

  /** **************************************************************
   * String-String Maps
   * ************************************************************** */

  @inline
  def valueMapStringStringKeys(
                                reader: TeslaBufferReader, schematic: BrioSchematic,
                                relationOrdinal: BrioRelationOrdinal
                              ): Array[BrioDictionaryKey] = {
    val offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    lookupStringStringMapKeys(reader, offset)
  }

  @inline
  def valueMapStringString(
                            reader: TeslaBufferReader, schematic: BrioSchematic,
                            relationOrdinal: BrioRelationOrdinal, mapKey: BrioDictionaryKey
                          ): BrioDictionaryKey = {
    val offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    lookupStringStringMap(mapKey, reader, offset)
  }

  @inline
  def valueMapStringStringIsNull(
                                  reader: TeslaBufferReader, schematic: BrioSchematic,
                                  relationOrdinal: BrioRelationOrdinal, mapKey: BrioDictionaryKey
                                ): Boolean = {
    val offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    lookupStringStringMapIsNull(mapKey, reader, offset)
  }

  @inline
  def valueMapStringStringSize(reader: TeslaBufferReader, schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal): Int = {
    val offset = readVariableSizeRelationOffset(schematic, relationOrdinal, reader, startOffset)
    lookupStringStringMapSize(reader, offset)
  }

  /////////////////////////////////////////////////////////////////////////////////////
  // Generic Field Operations
  /////////////////////////////////////////////////////////////////////////////////////
  /**
   * read the offset into the variable size data area
   * for a given variable relation (scalar references, vector values, vector references)
   *
   * @param schematic       the active schematic
   * @param relationOrdinal the zero based relation ordinal
   * @param reader
   * @param startOffset
   * @return
   */
  @inline private
  def readVariableSizeRelationOffset(
                                      schematic: BrioSchematic, relationOrdinal: BrioRelationOrdinal,
                                      reader: TeslaBufferReader, startOffset: TeslaMemoryOffset
                                    ): TeslaMemoryOffset = {
    val variableFieldKey = schematic.variableRelationOffsetKeys(relationOrdinal)
    val vRelationOffset = schematic.variableRelationOffsetsStart
    val offsetIntoOffsets = variableFieldKey * SizeOfOffset
    val position = startOffset + vRelationOffset + offsetIntoOffsets
    val readOffset = reader.readOffset(position)
    readOffset + startOffset
  }

}

