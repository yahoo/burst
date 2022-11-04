/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.slice

import org.burstsys.fabric.wave.data.model.snap.ColdSnap
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource

/**
 * ===Fabric Slice Identity===
 * The '''Fabric Slice''' must contain enough information to both support a full cold load of the associated dataset
 * partition, as well as be deterministic in its identity at the ''datasource'', the ''generation'', and the ''slicing'' level.
 * A slice in a cache of a given datasource must be identifiable uniquely such that
 * a hot, warm, or cold load all work correctly no matter how failures retarget slice requests to different workers.
 * Put another way a slice request should always identify the exact same data every time it is used to do a hot, warm,
 * or cold load for any reason at any time. It is up to the '''Fabric Store''' that implements the slice to provide
 * whatever information is necessary to enforce these semantics.
 * <p/>
 * === DESIGN NOTES ===
 * <ol>
 * <li>a dataset may get ''re-sliced'' on any and every single cache access and in each case any old ('''HOT/WARM''')
 * slice data '''MUST''' be ignored.</li>
 * <li>`slicingHash` '''MUST''' be correctly re-implemented by concrete slice subtypes to detect reslicing</li>
 * </ol>
 *
 */
trait FabricSliceIdentity extends Any with Equals {

  /**
   * the datasource (domain/view) for this generation. This is primary identity with
   * ancillary identity in specific generation instance variations such as reslicing.
   */
  def datasource: FabricDatasource

  /**
   * the unique identifier for this slice ordinal within a generation. This may or may not
   * be useful to establish slice identity.
   */
  def sliceKey: FabricSliceKey

  /**
   * @see [[FabricGenerationHash]]
   * @return
   */
  def generationHash: FabricGenerationHash

  /**
   * here we determine if a prior slice (of the same datsource, and slicekey)
   * has been ''re-sliced'' and hence is fully different from a previous hot or cold
   * load locally in memory or on disk. This is based on equality semantics provided by the given
   * [[org.burstsys.fabric.data.model.store.FabricStore]] and implemented in its version of the
   * [[org.burstsys.fabric.data.model.slice.FabricSliceIdentity]] API If that equality is seen
   * to be different, we set the state to [[ColdSnap]] and force a cold load with new slicing.
   * If the snap is in memory, we need to evict, if its on disk, we need to flush
   * the default version of this simply compares the ''slicingHash'' value. It is presumed that some
   * stores might want to do this differently.
   *
   * @param oldSlice
   * @return
   */
  final def isResliceOf(oldSlice: FabricSlice): Boolean = oldSlice.generationHash != this.generationHash

  /**
   * THIS SHOULD BE ALSO IMPLEMENTED BY CONCRETE SLICE SUBTYPE
   *
   * @param thatThing
   * @return
   */
  final override
  def canEqual(thatThing: Any): Boolean = thatThing match {
    case that: FabricSliceContext => true
    case _ => false
  }

  /**
   *
   * @return
   */
  final override
  def hashCode(): FabricSliceKey = {
    var result: Long = datasource.hashCode()
    result = 31 * result + sliceKey
    result.toInt
  }

  /**
   *
   * @param thatThing
   * @return
   */
  final override
  def equals(thatThing: Any): Boolean = thatThing match {
    case that: FabricSliceContext =>
      if (this.datasource != that.datasource) return false
      if (this.sliceKey != that.sliceKey) return false
      true
    case _ =>
      false
  }

}
