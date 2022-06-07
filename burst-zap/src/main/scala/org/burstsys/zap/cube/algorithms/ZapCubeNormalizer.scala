/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.algorithms

import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector}
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.text.VitalsTextCodec
import org.burstsys.zap
import org.burstsys.zap.cube.{ZapCube, ZapCubeBuilder, ZapCubeContext, ZapCubeRow}

/**
 * If we use strings for dimensions or aggregations in a zapcube,
 * and if the dictionaries are different, then we have to perform dictionary normalization.
 * since the string keys will not necessarily be the same between cubes and if one or more
 * dimensions are strings, then this also means when we adjust the string keys, we have to rebucket
 * tc row. The easiest way to do tc for now, is to create a new cube and import those rows
 * into it after normalization so they find their correct bucket automatically. If there are
 * string aggregations then we simply adjust them where we find them regardless of the existence of
 * string dimensions or not. NOTE: eventually we can do this re-bucketing in situ (no new cube) though its not
 * clear tc's better in time/space.
 */
trait ZapCubeNormalizer extends Any with ZapCube {

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////

  @inline final override
  def normalize(builder: FeltCubeBuilder, thisC: FeltCubeCollector, thisDictionary: BrioMutableDictionary,
                thatc: FeltCubeCollector, thatDictionary: BrioMutableDictionary)(implicit text: VitalsTextCodec): FeltCubeCollector = {

    if (thisDictionary.overflowed || thatDictionary.overflowed) {
      return thatc
    }

    val cubeBuilder = builder.asInstanceOf[ZapCubeBuilder]
    val thisCube = thisC.asInstanceOf[ZapCubeContext]
    val thatCube = thatc.asInstanceOf[ZapCubeContext]

    if (!builder.hasStringDimensions && !builder.hasStringAggregations) {
      thatc // nothing to do...
    } else if (!builder.hasStringDimensions && builder.hasStringAggregations) {
      normalizeJustAggregationsForExistingThatCube(cubeBuilder, thisCube, thisDictionary, thatCube, thatDictionary)
    } else if (builder.hasStringDimensions && !builder.hasStringAggregations) {
      normalizeJustDimensionsToNewThatCube(cubeBuilder, thisCube, thisDictionary, thatCube, thatDictionary)
    } else {
      normalizeDimensionsAndAggregationsToNewThatCube(cubeBuilder, thisCube, thisDictionary, thatCube, thatDictionary)
    }
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
  def normalizeJustDimensionsToNewThatCube(
                                            builder: ZapCubeBuilder,
                                            thisCube: ZapCubeContext, thisDictionary: BrioMutableDictionary,
                                            thatCube: ZapCubeContext, thatDictionary: BrioMutableDictionary)
                                          (implicit text: VitalsTextCodec): ZapCubeContext = {
    // we need to create a new cube and copy all rows over so they re-bucket
    val newThatCube = zap.cube.factory.grabZapCube(builder)
    try {
      // go through all the rows
      thatCube.foreachRow(builder, thatCube, {
        thatRow =>
          normalizeJustDimensionsForExistingThatRowToNewThatCube(
            builder = builder, thisCube = thisCube, thisDictionary = thisDictionary,
            thatCube = thatCube, thatDictionary = thatDictionary,
            oldRow = thatRow, newCube = newThatCube
          )
      })
    } catch safely {
      case t: Throwable =>
        // newThatCube is not set into the merge-source plane if we throw an exception here and we'll leak newThatCube
        // correlary: thatCube will still be free'd by the merge-source plane so we shouldn't free it here
        zap.cube.factory.releaseZapCube(newThatCube)
        throw t
    }
    zap.cube.factory.releaseZapCube(thatCube) // we are done with the old tc cube
    newThatCube
  }

  /**
   *
   * @param thatCube
   * @return
   */
  @inline private
  def normalizeDimensionsAndAggregationsToNewThatCube(
                                                       builder: ZapCubeBuilder,
                                                       thisCube: ZapCubeContext, thisDictionary: BrioMutableDictionary,
                                                       thatCube: ZapCubeContext, thatDictionary: BrioMutableDictionary)
                                                     (implicit text: VitalsTextCodec): ZapCubeContext = {
    // we need to create a new cube and copy all rows over so they re-bucket
    val newThatCube = zap.cube.factory.grabZapCube(builder)
    try {
      // got through all the rows
      thatCube.foreachRow(builder, thatCube, {
        thatRow =>
          normalizeDimensionsAndAggregationsForExistingThatRowToNewThatCube(
            builder = builder, thisCube = thisCube, thisDictionary = thisDictionary,
            thatCube = thatCube, thatDictionary = thatDictionary, oldRow = thatRow,
            newCube = newThatCube
          )
      })
    } catch safely {
      case t: Throwable =>
        // newThatCube is not set into the merge-source plane if we throw an exception here and we'll leak newThatCube
        // correlary: thatCube will still be free'd by the merge-source plane so we shouldn't free it here
        zap.cube.factory.releaseZapCube(newThatCube)
        throw t
    }
    zap.cube.factory.releaseZapCube(thatCube) // we are done with the old tc cube
    newThatCube
  }

  /**
   *
   * @param thatCube
   * @param oldRow
   * @param newCube
   */
  @inline private
  def normalizeJustDimensionsForExistingThatRowToNewThatCube(
                                                              builder: ZapCubeBuilder,
                                                              thisCube: ZapCubeContext, thisDictionary: BrioMutableDictionary,
                                                              thatCube: ZapCubeContext,
                                                              thatDictionary: BrioMutableDictionary,
                                                              oldRow: ZapCubeRow, newCube: ZapCubeContext)
                                                            (implicit text: VitalsTextCodec): Unit = {
    val newRow = newNormalizedDimensionsThatRowInNewCube(
      builder = builder, thisCube = thisCube, thisDictionary = thisDictionary,
      oldCube = thatCube, oldDictionary = thatDictionary, oldRow = oldRow, newCube = newCube
    )
    copyAggregationsToNewThatRow(
      builder = builder, thisCube = thisCube, oldCube = thatCube, oldRow = oldRow, newCube = newCube, newRow = newRow
    )
  }

  /**
   *
   * @param thatCube
   * @param oldRow
   * @param newCube
   */
  @inline private
  def normalizeDimensionsAndAggregationsForExistingThatRowToNewThatCube(
                                                                         builder: ZapCubeBuilder,
                                                                         thisCube: ZapCubeContext, thisDictionary: BrioMutableDictionary,
                                                                         thatCube: ZapCubeContext,
                                                                         thatDictionary: BrioMutableDictionary,
                                                                         oldRow: ZapCubeRow,
                                                                         newCube: ZapCubeContext)
                                                                       (implicit text: VitalsTextCodec): Unit = {
    val newRow = newNormalizedDimensionsThatRowInNewCube(
      builder = builder, thisCube = thisCube, thisDictionary = thisDictionary,
      oldCube = thatCube, oldDictionary = thatDictionary, oldRow = oldRow, newCube = newCube
    )
    normalizeAggregationsToNewThatRow(
      builder = builder, thisCube = thisCube, thisDictionary = thisDictionary,
      oldCube = thatCube, oldDictionary = thatDictionary, oldRow = oldRow, newCube = newCube, newRow = newRow
    )
  }

  /**
   *
   * @param thatCube
   * @return
   */
  @inline private
  def normalizeJustAggregationsForExistingThatCube(builder: ZapCubeBuilder,
                                                   thisCube: ZapCubeContext, thisDictionary: BrioMutableDictionary,
                                                   thatCube: ZapCubeContext,
                                                   thatDictionary: BrioMutableDictionary)
                                                  (implicit text: VitalsTextCodec): ZapCubeContext = {
    thatCube foreachRow(builder, thatCube, {
      thatRow =>
        normalizeAggregationsForExistingThatRow(
          builder = builder, thisCube = thisCube, thisDictionary = thisDictionary,
          thatCube = thatCube, thatDictionary = thatDictionary, thatRow = thatRow
        )
    })
    //    thatCube.dictionary = this.dictionary
    thatCube
  }

  /**
   * go through dimensions and set up this new cube's cursor to correct normalized dimensions
   *
   * @param oldCube
   * @param oldRow
   * @param newCube
   * @return
   */
  @inline private
  def newNormalizedDimensionsThatRowInNewCube(builder: ZapCubeBuilder,
                                              thisCube: ZapCubeContext, thisDictionary: BrioMutableDictionary,
                                              oldCube: ZapCubeContext, oldDictionary: BrioMutableDictionary,
                                              oldRow: ZapCubeRow, newCube: ZapCubeContext)
                                             (implicit text: VitalsTextCodec): ZapCubeRow = {
    // make sure we start with an unsullied cursor
    newCube.initCursor(builder, newCube)
    var d = 0
    while (d < builder.dimensionCount) {
      if (!oldRow.readRowDimensionIsNull(builder, oldCube, d)) {
        val oldValue = oldRow.readRowDimensionPrimitive(builder, oldCube, d)
        // if this is a string dimension, then we need to move tc to the new dictionary
        val newValue = if (builder.dimensionFieldTypes(d) == BrioStringKey) {
          val oldString = oldDictionary.stringLookup(oldValue.toShort)
          thisDictionary.keyLookupWithAdd(oldString).toLong
        } else oldValue
        // put the original or a normalized value into new cursor
        newCube.writeDimensionPrimitive(builder, newCube, d, newValue)
      }
      d += 1
    }

    // now when we navigate we will create a new row with the normalized dimensions
    newCube.navigate(builder, newCube)

    // and finally we update it with normalized aggregations (strings or not)
    newCube.cursorRow
  }

  /**
   * Take new 'tc' row in a new 'tc' cube and normalize the aggregations from the old 'tc' row using
   * the new dictionary which is the same as 'this' cube
   *
   * @param oldCube
   * @param oldRow
   * @param newCube
   * @param newRow
   */
  @inline private
  def normalizeAggregationsToNewThatRow(builder: ZapCubeBuilder,
                                        thisCube: ZapCubeContext, thisDictionary: BrioMutableDictionary,
                                        oldCube: ZapCubeContext, oldDictionary: BrioMutableDictionary,
                                        oldRow: ZapCubeRow, newCube: ZapCubeContext, newRow: ZapCubeRow)
                                       (implicit text: VitalsTextCodec): Unit = {
    var a = 0
    while (a < builder.aggregationCount) {
      if (!oldRow.readRowAggregationIsNull(builder, oldCube, a)) {
        val oldValue = oldRow.readRowAggregationPrimitive(builder, oldCube, a)
        val newValue = if (builder.aggregationFieldTypes(a) == BrioStringKey) {
          val oldString = oldDictionary.stringLookup(oldValue.toShort)
          thisDictionary.keyLookupWithAdd(oldString).toLong
        } else oldValue
        newRow.writeRowAggregationPrimitive(builder, newCube, a, newValue)
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
  def normalizeAggregationsForExistingThatRow(
                                               builder: ZapCubeBuilder,
                                               thisCube: ZapCubeContext, thisDictionary: BrioMutableDictionary,
                                               thatCube: ZapCubeContext, thatDictionary: BrioMutableDictionary,
                                               thatRow: ZapCubeRow)
                                             (implicit text: VitalsTextCodec): Unit = {
    var a = 0
    while (a < builder.aggregationCount) {
      if (!thatRow.readRowAggregationIsNull(builder, thatCube, a)) {
        val oldValue = thatRow.readRowAggregationPrimitive(builder, thatCube, a)
        val newValue = if (builder.aggregationFieldTypes(a) == BrioStringKey) {
          val oldString = thatDictionary.stringLookup(oldValue.toShort)
          thisDictionary.keyLookupWithAdd(oldString).toLong
        } else oldValue
        thatRow.writeRowAggregationPrimitive(builder, thatCube, a, newValue)
      }
      a += 1
    }
  }

  /**
   * take the aggregations directory from one row in one cube and copy it into another row in another map
   *
   * @param oldCube
   * @param oldRow
   * @param newCube
   * @param newRow
   */
  @inline private
  def copyAggregationsToNewThatRow(
                                    builder: ZapCubeBuilder, thisCube: ZapCubeContext, oldCube: ZapCubeContext,
                                    oldRow: ZapCubeRow, newCube: ZapCubeContext, newRow: ZapCubeRow)
                                  (implicit text: VitalsTextCodec): Unit = {
    var a = 0
    while (a < builder.aggregationCount) {
      if (!oldRow.readRowAggregationIsNull(builder, oldCube, a)) {
        val oldValue = oldRow.readRowAggregationPrimitive(builder, oldCube, a)
        newRow.writeRowAggregationPrimitive(builder, newCube, a, oldValue)
      }
      a += 1
    }
  }

}
