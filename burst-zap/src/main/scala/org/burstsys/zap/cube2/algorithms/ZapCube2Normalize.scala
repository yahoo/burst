/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2.algorithms

import org.burstsys.brio.types.BrioTypes.BrioStringKey
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.text.VitalsTextCodec
import org.burstsys.zap.cube2
import org.burstsys.zap.cube2.row.ZapCube2Row
import org.burstsys.zap.cube2.state.ZapCube2State
import org.burstsys.zap.cube2.{ZapCube2, ZapCube2Builder}

/**
 * algorithm that given another cube, rewrites all its rows so that it shared the same dictionary
 * and all its string->key mappings so the two cubes are '''normalized''' to the same dictionary.
 * This prepares the given cube to be able to participate in operations that require them to use the
 * same dictionary.
 */
trait ZapCube2Normalize extends Any with ZapCube2State {

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def normalizeThatCubeToThis(thatCube: ZapCube2, builder: ZapCube2Builder, text: VitalsTextCodec): ZapCube2 = {
    // if dictionaries are the same then nothing to do
    try {
      if (this.dictionary == thatCube.dictionary) return thatCube

      // if we have overflow, then bail TODO: do we need to do more here???
      if (this.dictionary.overflowed || thatCube.dictionary.overflowed) return thatCube

      /**
       * we make the top level decision about what is required for this normalization based
       * on the existence of string dimensions and/or aggregations
       */
      if (!builder.hasStringDimensions && !builder.hasStringAggregations) {
        // simple assign our dictionary to them
        thatCube.dictionary = this.dictionary
        thatCube
      } else if (!builder.hasStringDimensions && builder.hasStringAggregations) {
        // ok only need to deal with dimensions
        normalizeJustAggregationsForExistingThatCube(thatCube)(builder, text)
      } else if (builder.hasStringDimensions && !builder.hasStringAggregations) {
        // only need to deal with dimensions
        normalizeJustDimensionsToNewThatCube(thatCube)(builder, text)
      } else {
        // got both
        normalizeDimensionsAndAggregationsToNewThatCube(thatCube)(builder, text)
      }
    } finally if (!rowsLimited) resizeCount = 0 // made it all the way through without a resize
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // IMPLEMENTATION
  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * normalize just the dimensions, not the aggregations. We need a new cube, since
   * we are going to 'rebucket' the rows as the dimensions change (and their bucket
   * hash key changes). The aggregations will be copied over as is.
   *
   * @param thatCube
   * @return
   */
  @inline private
  def normalizeJustDimensionsToNewThatCube(thatCube: ZapCube2)
                                          (implicit builder: ZapCube2Builder, text: VitalsTextCodec): ZapCube2 = {
    // we need to create a new cube and copy all rows over so they re-bucket
    val newThatCube = cube2.flex.grabFlexCube(this.dictionary, builder)
    try {
      // go through all the rows
      var index = 0
      while (index < rowsCount) {
        normalizeJustDimensionsForExistingThatRowToNewThatCube(thatCube, thatCube.row(index), newThatCube)
        index += 1
      }
    } catch safely {
      case t: Throwable =>
        // newThatCube is not set into the merge-source plane if we throw an exception here and we'll leak newThatCube
        // correlary: thatCube will still be free'd by the merge-source plane so we shouldn't free it here
        cube2.flex.releaseFlexCube(newThatCube)
        throw t
    }
    cube2.flex.releaseFlexCube(thatCube) // we are done with the old that cube
    newThatCube
  }

  /**
   * normalize dimensions and aggregations. We need a new cube, since
   * we are going to 'rebucket' the rows as the dimensions change (and their bucket
   * hash key changes). The aggregations will be simply updated to new dictionary keys
   *
   * @param thatCube
   * @return
   */
  @inline private
  def normalizeDimensionsAndAggregationsToNewThatCube(thatCube: ZapCube2)
                                                     (implicit builder: ZapCube2Builder, text: VitalsTextCodec): ZapCube2 = {
    // we need to create a new cube and copy all rows over so they re-bucket
    val newThatCube = cube2.flex.grabFlexCube(this.dictionary, builder)
    try {
      // got through all the rows
      var index = 0
      while (index < rowsCount) {
        normalizeDimensionsAndAggregationsForExistingThatRowToNewThatCube(thatCube, thatCube.row(index), newThatCube)
        index += 1
      }
    } catch safely {
      case t: Throwable =>
        // newThatCube is not set into the merge-source plane if we throw an exception here and we'll leak newThatCube
        // correlary: thatCube will still be free'd by the merge-source plane so we shouldn't free it here
        cube2.flex.releaseFlexCube(newThatCube)
        throw t
    }
    cube2.flex.releaseFlexCube(thatCube) // we are done with the old that cube
    newThatCube
  }

  /**
   *
   * @param oldCube
   * @param oldRow
   * @param newCube
   */
  @inline private
  def normalizeJustDimensionsForExistingThatRowToNewThatCube(oldCube: ZapCube2, oldRow: ZapCube2Row, newCube: ZapCube2)
                                                            (implicit builder: ZapCube2Builder, text: VitalsTextCodec): Unit = {
    val newRow = newNormalizedDimensionsThatRowInNewCube(oldCube, oldRow, newCube)
    copyAggregationsToNewThatRow(oldCube, oldRow, newCube, newRow)
  }

  /**
   *
   * @param thatCube
   * @param thatRow
   * @param newCube
   */
  @inline private
  def normalizeDimensionsAndAggregationsForExistingThatRowToNewThatCube(thatCube: ZapCube2, thatRow: ZapCube2Row, newCube: ZapCube2)
                                                                       (implicit builder: ZapCube2Builder, text: VitalsTextCodec): Unit = {
    val newRow = newNormalizedDimensionsThatRowInNewCube(thatCube, thatRow, newCube)
    normalizeAggregationsToNewThatRow(thatCube, thatRow, newCube, newRow)
  }

  /**
   *
   * @param thatCube
   * @return
   */
  @inline private
  def normalizeJustAggregationsForExistingThatCube(thatCube: ZapCube2)
                                                  (implicit builder: ZapCube2Builder, text: VitalsTextCodec): ZapCube2 = {
    var index = 0
    while (index < rowsCount) {
      normalizeAggregationsForExistingThatRow(thatCube, thatCube.row(index))
      index += 1
    }
    thatCube.dictionary = this.dictionary
    thatCube
  }

  /**
   * go through dimensions and set up this new cube's cursor to correct normalized dimensions
   *
   * @param thatCube
   * @param thatRow
   * @param newCube
   * @return
   */
  @inline private
  def newNormalizedDimensionsThatRowInNewCube(thatCube: ZapCube2, thatRow: ZapCube2Row, newCube: ZapCube2)
                                             (implicit builder: ZapCube2Builder, text: VitalsTextCodec): ZapCube2Row = {
    // make sure we start with an unsullied cursor
    newCube.resetCursor()
    var d = 0
    while (d < dimCount) {
      if (!thatRow.dimIsNull(d)) {
        val oldValue = thatRow.dimRead(d)
        val newValue = if (builder.dimensionFieldTypes(d) == BrioStringKey) {
          // remap the dictionary key
          val oldString = thatCube.dictionary.stringLookup(oldValue.toShort)(text)
          this.dictionary.keyLookupWithAdd(oldString)(text).toLong
        } else oldValue
        // put the original or a normalized value into new cursor
        newCube.dimWrite(d, newValue)
      }
      d += 1
    }

    // now when we navigate we will create a new row with the normalized dimensions
    newCube.navigate()

    // and return that new row in the new cube
    ZapCube2Row(newCube.basePtr, newCube.cursorRow)
  }

  /**
   * Take new 'that' row in a new 'that' cube and normalize the aggregations from the old 'that' row using
   * the new dictionary which is the same as 'this' cube
   *
   * @param thatCube
   * @param thatRow
   * @param newCube
   * @param newRow
   */
  @inline private
  def normalizeAggregationsToNewThatRow(thatCube: ZapCube2, thatRow: ZapCube2Row, newCube: ZapCube2, newRow: ZapCube2Row)
                                       (implicit builder: ZapCube2Builder, text: VitalsTextCodec): Unit = {
    var a = 0
    while (a < aggCount) {
      if (!thatRow.aggIsNull(a)) {
        val oldValue = thatRow.aggRead(a)
        val newValue = if (builder.aggregationFieldTypes(a) == BrioStringKey) {
          // remap the dictionary key
          val oldString = thatCube.dictionary.stringLookup(oldValue.toShort)(text)
          this.dictionary.keyLookupWithAdd(oldString)(text).toLong
        } else oldValue
        newRow.aggWrite(a, newValue)
      }
      a += 1
    }
  }

  /**
   * Take an existing row from another cube and normalize the aggregations in situ
   * from 'this' dictionary
   *
   * @param thatCube
   * @param thatRow
   */
  @inline private
  def normalizeAggregationsForExistingThatRow(thatCube: ZapCube2, thatRow: ZapCube2Row)
                                             (implicit builder: ZapCube2Builder, text: VitalsTextCodec): Unit = {
    var a = 0
    while (a < aggCount) {
      if (!thatRow.aggIsNull(a)) {
        val oldValue = thatRow.aggRead(a)
        val newValue = if (builder.aggregationFieldTypes(a) == BrioStringKey) {
          // remap the dictionary key
          val oldString = thatCube.dictionary.stringLookup(oldValue.toShort)(text)
          this.dictionary.keyLookupWithAdd(oldString)(text).toLong
        } else oldValue
        thatRow.aggWrite(a, newValue)
      }
      a += 1
    }
  }

  /**
   * take the aggregations directory from one row in one cube and copy it into another row in another map
   *
   * @param thatCube
   * @param thatRow
   * @param newCube
   * @param newRow
   */
  @inline private
  def copyAggregationsToNewThatRow(thatCube: ZapCube2, thatRow: ZapCube2Row, newCube: ZapCube2, newRow: ZapCube2Row)
                                  (implicit builder: ZapCube2Builder, text: VitalsTextCodec): Unit = {
    var a = 0
    while (a < aggCount) {
      if (!thatRow.aggIsNull(a)) {
        val oldValue = thatRow.aggRead(a)
        newRow.aggWrite(a, oldValue)
      }
      a += 1
    }
  }

}
