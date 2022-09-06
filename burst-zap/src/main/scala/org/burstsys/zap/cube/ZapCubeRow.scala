/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube

import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.brio.types.BrioPrimitives.BrioPrimitive
import org.burstsys.brio.types.BrioTypes.BrioStringKey
import org.burstsys.fabric.execution.model.result.row.FabricDataKeyAnyVal
import org.burstsys.felt.model.collectors.cube.runtime.FeltCubeRow
import org.burstsys.tesla.TeslaTypes.TeslaMemoryPtr
import org.burstsys.vitals.text.VitalsTextCodec

/**
 * This is a scala 'Value' class that encapsulates the start offset for a row and all of the behavior associated
 * with an individual row.
 * <p> __NOTE:__ currently there is a hard limit to 64 aggregation fields and 64 dimension fields </p>
 * {{{
 *   ROW[ DIMENSION NULLMAP | AGGREGATION NULLMAP | DIMENSION FIELDS | AGGREGATION FIELDS ]
 * }}}
 * For null maps: <pre>   0 == NULL, 1 == NOT NULL</pre>
 *
 */
final case
class ZapCubeRow(rowStartOffset: TeslaMemoryPtr = ZapCubeEmptyLink) extends AnyVal with FeltCubeRow {

  @inline def validRow: Boolean = rowStartOffset != ZapCubeEmptyLink

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Key Matching
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * determine if a dimensional key matches
   *
   * @param key
   * @return
   */
  @inline
  def matchesDimensionKey(builder: ZapCubeBuilder, thisCube: ZapCubeContext, key: FabricDataKeyAnyVal): Boolean = {

    ////////////////////////////////////////////////////////////////////////////////////////////
    // first check the dimension null map
    val dimensionNullMap = readDimensionNullMap(builder, thisCube)
    val keyNullMap = key.nullMap

    // if both are all nulls then we have a match
    if (keyNullMap == 0L && dimensionNullMap == 0L)
      return true

    // if the nulls are not the same we do not have a match
    if (keyNullMap != dimensionNullMap)
      return false

    ////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * we got the nulls sorted out, lets now check the non null dimension values
     * The nulls are the same, so we can use either key or this row to test
     * for non null-ness.
     */
    var d = 0 // start at first dimension
    while (d < builder.dimensionCount) {
      // only check non null fields
      if (!key.readKeyDimensionIsNull(d)) {
        val dValue = readRowDimensionPrimitive(builder, thisCube, d)
        if (key.readKeyDimension(d) != dValue)
          return false
      }
      d += 1
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // its a match
    true
  }

  /**
   * Does this row match a row from another map.
   *
   * @param thatCube
   * @param thatRow
   * @return
   */
  @inline
  def matchesRowDimensionKeyInAnotherMap(builder: ZapCubeBuilder, thisCube: ZapCubeContext, thatCube: ZapCubeContext, thatRow: ZapCubeRow): Boolean = {

    ////////////////////////////////////////////////////////////////////////////////////////////
    // check dimension nulls map for these rows...
    val thisDimensionNullsMap = readDimensionNullMap(builder, thisCube)
    val thatDimensionNullsMap = thatRow.readDimensionNullMap(builder, thatCube)

    // if both are all null then we have a match
    if (thisDimensionNullsMap == 0L && thatDimensionNullsMap == 0L)
      return true

    // if null are not equal then we do not have a match
    if (thisDimensionNullsMap != thatDimensionNullsMap)
      return false

    ////////////////////////////////////////////////////////////////////////////////////////////
    // now check each non null dimension
    var d = 0
    while (d < builder.dimensionCount) {
      if (!readRowDimensionIsNull(builder, thisCube, d)) {
        val thisD = readRowDimensionPrimitive(builder, thisCube, d)
        val thatD = thatRow.readRowDimensionPrimitive(builder, thatCube, d)
        if (thisD != thatD)
          return false
      }
      d += 1
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // we have a match
    true
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Initialize/Copy
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Initialize this row to all default pristine values but with the same dimensional __key__
   *
   * @return
   */
  @inline
  def initializeToDimensionKey(builder: ZapCubeBuilder, thisCube: ZapCubeContext, key: FabricDataKeyAnyVal): ZapCubeRow = {

    ////////////////////////////////////////////////////////////////////////////////////////////
    // set the dimensions from the key
    clearDimensionNullMap(builder, thisCube)
    var d = 0
    while (d < builder.dimensionCount) {
      if (key.readKeyDimensionIsNull(d))
        initRowDimension(builder, thisCube, d)
      else
        writeRowDimensionPrimitive(builder, thisCube, d, key.readKeyDimension(d))
      d += 1
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // clear the aggregations
    clearAggregationNullMap(builder, thisCube)
    var a = 0
    while (a < builder.aggregationCount) {
      initRowAggregation(builder, thisCube, a)
      a += 1
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // now clear the next row pointer to -1
    setLinkField(builder, thisCube, ZapCubeEmptyLink)

    ////////////////////////////////////////////////////////////////////////////////////////////
    // init the dimension nulls map to original value
    writeDimensionNullMap(builder, thisCube, key.nullMap)

    ////////////////////////////////////////////////////////////////////////////////////////////
    // init the aggregation nulls map to null
    clearAggregationNullMap(builder, thisCube)

    this
  }

  /**
   * Copy in all data from external row other than the start offset and the link column
   *
   * @param thatCube
   * @param thatRow
   * @return
   */
  @inline
  def copyDimensionAndAggregationValuesFrom(builder: ZapCubeBuilder, thisCube: ZapCubeContext,
                                            thatCube: ZapCubeContext, thatRow: ZapCubeRow): ZapCubeRow = {
    ////////////////////////////////////////////////////////////////////////////////////////////
    // copy over dimensions
    clearDimensionNullMap(builder, thisCube)
    var d = 0
    while (d < builder.dimensionCount) {
      if (thatRow.readRowDimensionIsNull(builder, thatCube, d)) {
        initRowDimension(builder, thisCube, d)
      } else {
        val newValue = thatRow.readRowDimensionPrimitive(builder, thatCube, d)
        writeRowDimensionPrimitive(builder, thisCube, d, newValue)
      }
      d += 1
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // copy over aggregations
    clearAggregationNullMap(builder, thisCube)
    var a = 0
    while (a < builder.aggregationCount) {
      if (thatRow.readRowAggregationIsNull(builder, thatCube, a)) {
        initRowAggregation(builder, thisCube, a)
      } else {
        val newValue = thatRow.readRowAggregationPrimitive(builder, thatCube, a)
        writeRowAggregationPrimitive(builder, thisCube, a, newValue)
      }
      a += 1
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // clear link column
    setLinkField(builder, thisCube, ZapCubeEmptyLink)

    ////////////////////////////////////////////////////////////////////////////////////////////
    // write dimension null map to clear all inappropriate writes to dimensions
    val dimensionsNullMap = thatRow.readDimensionNullMap(builder, thatCube)
    writeDimensionNullMap(builder, thisCube, dimensionsNullMap)

    ////////////////////////////////////////////////////////////////////////////////////////////
    // write aggregation null map to clear all inappropriate writes to aggregations
    val aggregationsNullMap = thatRow.readAggregationNullMap(builder, thatCube)
    writeAggregationNullMap(builder, thisCube, aggregationsNullMap)

    this
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // DIMENSION NULL MAP
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline private
  def dimensionNullMapOffset: ZapMemoryOffset = rowStartOffset

  @inline
  def readDimensionNullMap(builder: ZapCubeBuilder, thisCube: ZapCubeContext): Long =
    thisCube.getLong(dimensionNullMapOffset)

  @inline
  def writeDimensionNullMap(builder: ZapCubeBuilder, thisCube: ZapCubeContext, value: Long): Unit =
    thisCube.putLong(dimensionNullMapOffset, value)

  @inline
  def clearDimensionNullMap(builder: ZapCubeBuilder, thisCube: ZapCubeContext): Unit =
    thisCube.putLong(dimensionNullMapOffset, 0L)

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // AGGREGATION NULL MAP
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline private
  def aggregationNullMapOffset: ZapMemoryOffset = dimensionNullMapOffset + DimensionNullMapSize

  @inline
  def readAggregationNullMap(builder: ZapCubeBuilder, thisCube: ZapCubeContext): Long = thisCube.getLong(aggregationNullMapOffset)

  @inline
  def writeAggregationNullMap(builder: ZapCubeBuilder, thisCube: ZapCubeContext, value: Long): Unit = thisCube.putLong(aggregationNullMapOffset, value)

  @inline
  def clearAggregationNullMap(builder: ZapCubeBuilder, thisCube: ZapCubeContext): Unit = thisCube.putLong(aggregationNullMapOffset, 0L)

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // DIMENSION FIELDS
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline private
  def dimensionFieldOffset: Long =
    aggregationNullMapOffset + AggregationNullMapSize

  @inline private
  def dimensionFieldOffset(dimension: Long): Long =
    dimensionFieldOffset + (dimension * DimensionColumnSize)

  /**
   * set a dimension field in the row to a not null state
   * <pre>0 == NULL, 1 == NOT NULL</pre>
   *
   * @param dimension
   * @param thisCube
   */
  @inline
  def writeRowDimensionNotNull(builder: ZapCubeBuilder, thisCube: ZapCubeContext, dimension: ZapCubeDimensionKey): Unit = {
    val bit: Long = 1L << dimension.toLong
    val oldNulls: Long = readDimensionNullMap(builder, thisCube)
    val newNulls: Long = oldNulls | bit // set the bit
    writeDimensionNullMap(builder, thisCube, newNulls)
  }

  /**
   * set a dimension field in the row to a null state
   * <pre>0 == NULL, 1 == NOT NULL</pre>
   *
   * @param dimension
   * @param thisCube
   */
  @inline
  def writeRowDimensionIsNull(builder: ZapCubeBuilder, thisCube: ZapCubeContext, dimension: ZapCubeDimensionKey): Unit = {
    val bit: Long = 1L << dimension.toLong
    val oldNulls: Long = readDimensionNullMap(builder, thisCube)
    val newNulls: Long = oldNulls & ~bit // reset the bit
    writeDimensionNullMap(builder, thisCube, newNulls)
  }

  /**
   * return the nullity state for a dimension field in the row
   * <pre>0 == NULL, 1 == NOT NULL</pre>
   *
   * @param dimension
   * @param thisCube
   * @return
   */
  @inline
  def readRowDimensionIsNull(builder: ZapCubeBuilder, thisCube: ZapCubeContext, dimension: ZapCubeDimensionKey): Boolean = {
    val bit: Long = 1L << dimension.toLong
    val oldNulls: Long = readDimensionNullMap(builder, thisCube)
    val newNulls: Long = oldNulls & bit
    newNulls != bit
  }

  /**
   * return the primitive value for a dimension field in the row
   *
   * @param dimension
   * @param thisCube
   * @return
   */
  @inline
  def readRowDimensionPrimitive(builder: ZapCubeBuilder, thisCube: ZapCubeContext, dimension: ZapCubeDimensionKey): BrioPrimitive =
    thisCube.getLong(dimensionFieldOffset(dimension))

  /**
   * write the primitive value for a dimension field in the row
   *
   * @param dimension
   * @param value
   * @param thisCube
   */
  @inline
  def writeRowDimensionPrimitive(builder: ZapCubeBuilder, thisCube: ZapCubeContext, dimension: ZapCubeDimensionKey, value: BrioPrimitive): Unit = {
    thisCube.putLong(dimensionFieldOffset(dimension), value)
    writeRowDimensionNotNull(builder, thisCube, dimension)
  }

  /**
   * write the default value for a dimension field in the row and set it to null
   *
   * @param dimension
   * @param thisCube
   */
  @inline
  def initRowDimension(builder: ZapCubeBuilder, thisCube: ZapCubeContext, dimension: ZapCubeDimensionKey): Unit = {
    thisCube.putLong(dimensionFieldOffset(dimension), zapCubeDefaultAggregation)
    writeRowDimensionIsNull(builder, thisCube, dimension)
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // AGGREGATION FIELDS
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline private
  def aggregationFieldOffset(builder: ZapCubeBuilder, thisCube: ZapCubeContext): ZapMemoryOffset = {
    rowStartOffset + builder.firstAggregationPointerOffset
  }

  @inline private
  def aggregationFieldOffset(builder: ZapCubeBuilder, thisCube: ZapCubeContext, aggregation: ZapCubeAggregationKey): ZapMemoryOffset = {
    aggregationFieldOffset(builder, thisCube) + (aggregation * AggregationColumnSize)
  }

  /**
   * set a aggregation field in the row to a not null state
   * <pre>0 == NULL, 1 == NOT NULL</pre>
   *
   * @param aggregation
   * @param thisCube
   */
  @inline private
  def writeRowAggregationNotNull(builder: ZapCubeBuilder, thisCube: ZapCubeContext, aggregation: ZapCubeAggregationKey): Unit = {
    val bit: Long = 1L << aggregation.toLong
    val oldNulls: Long = readAggregationNullMap(builder, thisCube)
    val newNulls: Long = oldNulls | bit // set the bit
    writeAggregationNullMap(builder, thisCube, newNulls)
  }

  /**
   * set a aggregation field in the row to a null state
   * <pre>0 == NULL, 1 == NOT NULL</pre>
   *
   * @param aggregation
   * @param thisCube
   */
  @inline
  def writeRowAggregationIsNull(builder: ZapCubeBuilder, thisCube: ZapCubeContext, aggregation: ZapCubeAggregationKey): Unit = {
    val bit: Long = 1L << aggregation.toLong
    val oldNulls: Long = readAggregationNullMap(builder, thisCube)
    val newNulls: Long = oldNulls & ~bit // reset the bit
    writeAggregationNullMap(builder, thisCube, newNulls)
  }

  /**
   * return the nullity state for a aggregation field in the row
   * <pre>0 == NULL, 1 == NOT NULL</pre>
   *
   * @param aggregation
   * @param thisCube
   * @return
   */
  @inline
  def readRowAggregationIsNull(builder: ZapCubeBuilder, thisCube: ZapCubeContext, aggregation: ZapCubeAggregationKey): Boolean = {
    val bit: Long = 1L << aggregation.toLong
    val oldNulls: Long = readAggregationNullMap(builder, thisCube)
    val newNulls: Long = oldNulls & bit
    newNulls != bit
  }

  /**
   * return the primitive value for a aggregation field in the row
   *
   * @param aggregation
   * @param thisCube
   * @return
   */
  @inline
  def readRowAggregationPrimitive(builder: ZapCubeBuilder, thisCube: ZapCubeContext, aggregation: ZapCubeAggregationKey): BrioPrimitive =
    thisCube.getLong(aggregationFieldOffset(builder, thisCube, aggregation))

  /**
   * write the primitive value for a aggregation field in the row
   *
   * @param aggregation
   * @param value
   * @param thisCube
   */
  @inline
  def writeRowAggregationPrimitive(builder: ZapCubeBuilder, thisCube: ZapCubeContext, aggregation: ZapCubeAggregationKey, value: BrioPrimitive): Unit = {
    thisCube.putLong(aggregationFieldOffset(builder, thisCube, aggregation), value)
    writeRowAggregationNotNull(builder, thisCube, aggregation)
  }

  /**
   * write the default value for a aggregation field in the row and set it to null
   *
   * @param aggregation
   * @param thisCube
   */
  @inline
  def initRowAggregation(builder: ZapCubeBuilder, thisCube: ZapCubeContext, aggregation: ZapCubeAggregationKey): Unit = {
    thisCube.putLong(aggregationFieldOffset(builder, thisCube, aggregation), zapCubeDefaultAggregation)
    writeRowAggregationIsNull(builder, thisCube, aggregation)
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Next Row Link Management
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @inline private
  def linkOffset(builder: ZapCubeBuilder, thisCube: ZapCubeContext): ZapMemoryOffset = {
    rowStartOffset + builder.linkOffset
  }

  @inline
  def linkField(builder: ZapCubeBuilder, thisCube: ZapCubeContext): ZapMemoryOffset = {
    thisCube.getLong(linkOffset(builder, thisCube))
  }

  @inline private[zap]
  def setLinkField(builder: ZapCubeBuilder, thisCube: ZapCubeContext, value: ZapMemoryOffset): Unit =
    thisCube.putLong(linkOffset(builder, thisCube), value)

  /**
   * return true if this row as a link to a 'next' row in the linked list of rows for this bucket
   *
   * @param builder
   * @param thisCube
   * @return
   */
  @inline
  def hasLinkRow(builder: ZapCubeBuilder, thisCube: ZapCubeContext): Boolean = linkField(builder, thisCube) != ZapCubeEmptyLink

  /**
   * return the offset for the next row in the linked list
   *
   * @return
   */
  @inline
  def linkRow(builder: ZapCubeBuilder, thisCube: ZapCubeContext): ZapCubeRow =
    ZapCubeRow(linkField(builder, thisCube))

  /**
   * Link this row to a new 'next' row in the linked list of rows for this bucket
   *
   * @param linkedRow
   */
  @inline
  def setLinkRow(builder: ZapCubeBuilder, thisCube: ZapCubeContext, linkedRow: ZapCubeRow): Unit =
    thisCube.putLong(linkOffset(builder, thisCube), linkedRow.rowStartOffset)

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Debug
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * print out row contents - for debugging
   *
   * @return
   */
  def printRow(builder: ZapCubeBuilder, thisCube: ZapCubeContext, thisDictionary: BrioMutableDictionary): String = {
    implicit val codec: VitalsTextCodec = VitalsTextCodec()
    var sb = new StringBuilder
    sb ++= s"[offset=$rowStartOffset, dim_nulls(${readDimensionNullMap(builder, thisCube)}), agg_nulls(${readAggregationNullMap(builder, thisCube)}), dims("

    {
      var d = 0
      while (d < builder.dimensionCount) {
        val dimensionValue = readRowDimensionPrimitive(builder, thisCube, d)
        val dimensionNull = readRowDimensionIsNull(builder, thisCube, d)
        val dimensionName = builder.dimensionName(d)
        if (builder.dimensionFieldTypes(d) == BrioStringKey) {
          val dimensionAsKey = dimensionValue.toShort
          val dimensionAsString = if (dimensionNull) "(NULL)" else s"'${thisDictionary.stringLookup(dimensionAsKey)}'"
          sb ++= s"$dimensionName=[key=$dimensionAsKey]$dimensionAsString "
        } else {
          val dimensionAsString = if (dimensionNull) "(NULL)" else dimensionValue
          sb ++= s"$dimensionName=$dimensionAsString "
        }
        d += 1
      }
    }

    sb ++= s") aggs("

    {
      var a = 0
      while (a < builder.aggregationCount) {
        val aggregationValue = readRowAggregationPrimitive(builder, thisCube, a)
        val aggregationNull = readRowAggregationIsNull(builder, thisCube, a)
        val aggregationName = builder.aggregationName(a)
        if (builder.aggregationFieldTypes(a) == BrioStringKey) {
          val aggregationAsKey = aggregationValue.toShort
          val aggregationAsString = if (aggregationNull) "(NULL)" else s"'${thisDictionary.stringLookup(aggregationAsKey)}'"
          sb ++= s"$aggregationName=[key=$aggregationAsKey]$aggregationAsString "
        } else {
          val aggregationAsString = if (aggregationNull) "(NULL)" else aggregationValue.toString
          sb ++= s"$aggregationName=$aggregationAsString "
        }
        a += 1
      }
    }

    sb ++= s") "
    sb ++= s"link(${linkField(builder, thisCube)})]"
    val s = sb.toString()
    s
  }


}
