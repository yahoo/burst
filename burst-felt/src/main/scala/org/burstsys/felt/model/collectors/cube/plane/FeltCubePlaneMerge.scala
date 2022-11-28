/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.plane

import org.burstsys.fabric.wave.execution.model.gather.FabricMerge
import org.burstsys.fabric.wave.execution.model.FabricMergeLevel
import org.burstsys.fabric.wave.execution.model.FabricRegionMergeLevel
import org.burstsys.fabric.wave.execution.model.FabricSliceMergeLevel
import org.burstsys.fabric.wave.execution.model.FabricWaveMergeLevel
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.take.FeltCubeAggTakeSemRt
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.take.FeltCubeBottomTakeSemMode
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.take.FeltCubeTopTakeSemMode
import org.burstsys.felt.model.collectors.cube.FeltCubeBuilder
import org.burstsys.felt.model.collectors.cube.FeltCubeCollector
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors.printStack
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.logging.log

trait FeltCubePlaneMerge extends FeltCubePlane {

  @inline final override
  def regionMerge(thatResult: FabricMerge): Unit = {
    val thatCubePlane = thatResult.asInstanceOf[FeltCubePlane]
    merge(thatCubePlane, FabricRegionMergeLevel)
  }

  @inline final override
  def sliceMerge(thatResult: FabricMerge): Unit = {
    val thatCubePlane = thatResult.asInstanceOf[FeltCubePlane]
    merge(thatCubePlane, FabricSliceMergeLevel)
  }

  @inline final override
  def waveMerge(thatResult: FabricMerge): Unit = {
    val thatCubePlane = thatResult.asInstanceOf[FeltCubePlane]
    merge(thatCubePlane, FabricWaveMergeLevel)
  }

  @inline final override
  def sliceFinalize(): Unit = {
    if (hadException)
      return
    takeReduction(this.planeBuilder, this.planeCollector, FabricSliceMergeLevel)
  }

  @inline final override
  def waveFinalize(): Unit = {
    if (hadException)
      return

    takeReduction(this.planeBuilder, this.planeCollector, FabricWaveMergeLevel)

    // assert rowLimit for the very last result to make sure it matches what is asked for.
    if (planeCollector.itemCount > planeBuilder.rowLimit) {
      planeCollector.truncateRows(planeBuilder.rowLimit)
      planeCollector.itemLimited = true
    }

  }

  /**
   * go through planes and where appropriate [[FeltCubeAggTakeSemRt]] aggregations exist, truncate/sort using
   * that pseudo multilevel topK algorithm
   *
   * @param cube
   * @param level
   */
  @inline private
  def takeReduction(builder: FeltCubeBuilder, cube: FeltCubeCollector, level: FabricMergeLevel): Unit = {
    var i = 0
    while (i < this.planeBuilder.aggregationSemantics.length) {
      this.planeBuilder.aggregationSemantics(i) match {
        case ts: FeltCubeAggTakeSemRt =>
          val k = level match {
            case FabricRegionMergeLevel =>
              ts.regionK
            case FabricSliceMergeLevel =>
              ts.sliceK
            case FabricWaveMergeLevel =>
              ts.scatterK
            case _ => ???
          }
          ts._mode match {
            case FeltCubeTopTakeSemMode =>
              cube.truncateToTopKBasedOnAggregation(builder, cube, k, i)
            case FeltCubeBottomTakeSemMode =>
              cube.truncateToBottomKBasedOnAggregation(builder, cube, k, i)
            case um =>
              throw VitalsException(s"unknown mode $um")
          }
        case _ =>
      }
      i += 1
    }
  }

  /**
   * Do the merge of two cube planes
   *
   * @param that  the plane to merge into this plane
   * @param level what level at which the merge is happening, used for topK
   */
  @inline private
  def merge(that: FeltCubePlane, level: FabricMergeLevel): Unit = {
    try {
      if (hadException)
        return
      mergeOutcome(that)

      if (thisOutcomeOrThatOutcomeInvalid(that))
        return

      if (hadException || exceededResourceConstraints(that))
        return

      // do any reduction on incoming cube before the normalize to speed reduction and get smaller dictionaries
      takeReduction(that.planeBuilder, that.planeCollector, level)

      // get a 'that' cube that has its dictionary keys normalized to be the same as 'this' dictionary
      that.planeCollector = this.planeCollector.normalize(
        builder = this.planeBuilder, thisCube = this.planeCollector, thisDictionary = this.planeDictionary,
        thatCube = that.planeCollector, thatDictionary = that.planeDictionary
      )
      if (hadException || exceededResourceConstraints(that))
        return

      // perform the inter merge (across traversals) with the (possibly) new normalized cube
      this.planeCollector.interMerge(
        this.planeBuilder, this.planeCollector, this.planeDictionary, that.planeCollector, this.planeDictionary
      )
      if (hadException || exceededResourceConstraints(that))
        return

      // do another reduction on this cube after the normalize to get the final size
      takeReduction(this.planeBuilder, this.planeCollector, level)

    } catch safely {
      case t: Throwable =>
        this.markException(t)
    }
  }

  /**
   * consistent handling of resource constraint issues across all types of merges...
   *
   * @param that
   * @return
   */
  private def exceededResourceConstraints(that: FeltCubePlane): Boolean = {
    val dictionaryOverflow = if (this.dictionaryOverflow || that.dictionaryOverflow) {
      if (true)
        log warn printStack(VitalsException(burstStdMsg(
          s"""|DICTIONARY OVERFLOW:
              |this.cube.rowCount=${this.planeCollector.itemCount}, that.cube.rowCount=${that.planeCollector.itemCount}"
              |this.dictionary.slotOverflowed=${this.planeDictionary.slotOverflowed}, that.dictionary.slotOverflowed=${that.planeDictionary.slotOverflowed}
              |this.dictionary.keyOverflowed=${this.planeDictionary.keyOverflowed}, that.dictionary.words=${that.planeDictionary.keyOverflowed}""".stripMargin
        )).fillInStackTrace())
      this.flagDictionaryOverflow()
      this.clearCollector() // don't want any rows if dictionary gets thrown
      true
    } else
      false
    val rowLimited = if (this.rowLimitExceeded || that.rowLimitExceeded) {
      true
    } else {
      false
    }
    dictionaryOverflow || rowLimited
  }

}
