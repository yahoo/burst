/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2.algorithms

import org.burstsys.brio.dictionary.flex.BrioFlexDictionary
import org.burstsys.brio.types.BrioTypes.BrioStringKey
import org.burstsys.zap.log
import org.burstsys.vitals.text.VitalsTextCodec
import org.burstsys.zap.cube2.state.{EmptyBucket, EmptyLink, ZapCube2State}
import org.burstsys.zap.cube2.{ZapCube2, ZapCube2Builder}

/**
 * algorithm that given another cube, rewrites all its rows so that it shared the same dictionary
 * and all its string->key mappings so the two cubes are '''normalized''' to the same dictionary.
 * This prepares the given cube to be able to participate in operations that require them to use the
 * same dictionary.
 */
trait ZapCube2Normalize extends Any with ZapCube2State with ZapCube2Merge {

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def normalizeThatCubeToThis(thatCube: ZapCube2, builder: ZapCube2Builder, text: VitalsTextCodec): ZapCube2 = {
    try {
      // if dictionaries are the same then nothing to do
      if (this.dictionary == thatCube.dictionary) {
        return thatCube
      }

      // if we have overflow, then bail (this should not happen since all dictionaries are flex)
      if (this.dictionary.overflowed || thatCube.dictionary.overflowed) {
        log warn s"unexpected dictionary overflow in cube"
        return thatCube
      }

      /**
       * we make the top level decision about what is required for this normalization based
       * on the existence of string dimensions and/or aggregations
       */
      if (!builder.hasStringDimensions && !builder.hasStringAggregations) {
        // simple assign our dictionary to them
        // (I guess we do this to prevent later functions from complaining about
        // the dictionaries in two cube being different?)
        thatCube.dictionary = this.dictionary
        thatCube
      } else {
        thatCube.rowNormalize(builder, this.dictionary, text)
        thatCube
      }
    } finally {
      if (!rowsLimited) {
        resizeCount = 0 // made it all the way through without a resize
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // IMPLEMENTATION
  //////////////////////////////////////////////////////////////////////////////////////////////////

  @inline private[cube2]
  def rowNormalize(thatBuilder: ZapCube2Builder, thatDictionary: BrioFlexDictionary, text: VitalsTextCodec): Unit = {
    if (this.isEmpty)
      return // if the incoming cube is empty - nothing to do

    // we will be remapping the dimensions so rebucket on the fly
    if (thatBuilder.hasStringDimensions)
      resetBuckets()

    var rc = 0
    while (rc < rowsCount) {
      val currentRow = row(rc)

      // dimensions
      if (thatBuilder.hasStringDimensions) {
        // unlink it
        currentRow.link = EmptyLink
        var d = 0
        while (d < dimCount) {
          if (thatBuilder.dimensionFieldTypes(d) == BrioStringKey && !currentRow.dimIsNull(d)) {
            val oldValue = currentRow.dimRead(d)
            // remap the dictionary key
            val oldString = dictionary.stringLookup(oldValue.toShort)(text)
            val newValue = thatDictionary.keyLookupWithAdd(oldString)(text).toLong
            currentRow.dimWrite(d, newValue)
          }
          d += 1
        }

        // put the row in the bucket
        setCursorFrom(currentRow)
        val index = cursor.bucketIndex(bucketsCount)

        // and see whats in the associated bucket list
        bucketRead(index) match {
          // no rows in bucket yet - we create new matching one, add to bucket and return
          case EmptyBucket =>
            // update bucket with a valid first row in list
            bucketWrite(index, rowOffset(rc))
          case firstRowOffset =>
            currentRow.link = firstRowOffset
            bucketWrite(index, rowOffset(rc))
        }

      }

      // aggregations
      if (thatBuilder.hasStringAggregations) {
        var a = 0
        while (a < aggCount) {
          if (thatBuilder.aggregationFieldTypes(a) == BrioStringKey && !currentRow.aggIsNull(a)) {
            val oldValue = currentRow.aggRead(a)
            val oldString = dictionary.stringLookup(oldValue.toShort)(text)
            val newValue = thatDictionary.keyLookupWithAdd(oldString)(text).toLong
            currentRow.aggWrite(a, newValue)
          }
          a += 1
        }
      }
      rc += 1
    }

    this.dictionary = thatDictionary
  }
}
