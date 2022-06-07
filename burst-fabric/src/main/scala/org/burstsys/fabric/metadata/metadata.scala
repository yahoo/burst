/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric

import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties.VitalsPropertyKey

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * The execution & data model for the metadata plane in the fabric.
 * These work for Kryo and JSON
 */
package object metadata extends VitalsLogger {

  ///////////////////////////////////////////////////////////////////////////////////////////////
  // VIEW PROPERTIES
  // View properties used to configure loads and report performance.
  // Additions to this list should be documented in the readme in this directory
  ///////////////////////////////////////////////////////////////////////////////////////////////

  //////////////////////////////////////////////
  // Client-provided properties (optional)
  /////////////////////////////////////////////

  /**
   * The number of milliseconds after the last access the worker cache waits before evicting a view (clearing from memory).
   */
  final val ViewCacheEvictTtlMsProperty: VitalsPropertyKey = "burst.view.evict.ttl.ms"

  /**
   * The number of milliseconds after the last access the worker cache waits before flushing a view (clearing from disk).
   */
  final val ViewCacheFlushTtlMsProperty: VitalsPropertyKey = "burst.view.flush.ttl.ms"

  /**
   * The number of milliseconds after the last access the worker cache waits before erasing a view (completely forgetting).
   */
  final val ViewCacheEraseTtlMsProperty: VitalsPropertyKey = "burst.view.erase.ttl.ms"

  /**
   * The client requested maxmium amount of data that should be loaded on the next load. This value is opaque to Burst
   * and is passed to the store responsible for loading data.
   */
  final val ViewNextDatasetSizeMaxProperty: VitalsPropertyKey = "burst.view.next.dataset.size.max.bytes"

  /**
   * The number of milliseconds after which the current generation should be marked as stale and reloaded. It is used to
   * comput `burst.view.earliest.load.at`.
   */
  final val ViewNextLoadStaleMsProperty: VitalsPropertyKey = "burst.view.next.load.stale"

  final val ViewNextLoadStaleMsDefault: Long = (1 day).toMillis

  /**
   * The desired sample rate to be used on the next load. Values outside the range `0 < rate <= 1` will likely be ignored
   */
  final val ViewNextSampleRateProperty: VitalsPropertyKey = "burst.view.next.sample.rate"

  final val ViewNextSampleRateDefault: Double = 1.0

  ///////////////////////////////////////////////
  // System-suggested properties
  ///////////////////////////////////////////////

  /**
   *  The suggested sample ratio, computed by dividing the count of items received by the number of potential items in the dataset
   */
  final val ViewSuggestedSampleRateProperty: VitalsPropertyKey = "burst.view.suggested.sample.rate"

  // TODO remove this property, it is only ever set to the most recent slice count, and we have no user-provided corollary
  final val ViewSuggestedSliceCountProperty: VitalsPropertyKey = "burst.view.suggested.slice.count"

  ///////////////////////////////////////////////
  // System-actual properties
  ///////////////////////////////////////////////

  /**
   * The epoch timestamp when this generation was first loaded (in milliseconds)
   */
  final val ViewEarliestLoadAtProperty: VitalsPropertyKey = "burst.view.earliest.load.at"

  /**
   * The actual number of bytes that were loaded from the fabric store during the most recent load
   */
  final val ViewLastDatasetSizeProperty: VitalsPropertyKey = "burst.view.last.dataset.size"

  /**
   * The number of bytes loaded during the last generation
   */
  final val ViewLastItemSizeProperty: VitalsPropertyKey = "burst.view.last.item.size"

  // TODO remove this property as it is not currently calculated and does not have a clear definition
  final val ViewLastItemVariationProperty: VitalsPropertyKey = "burst.view.last.item.variation"

  /**
   * The epoch timestamp when this generation was loaded (in milliseconds)
   */
  final val ViewLastColdLoadAtProperty: VitalsPropertyKey = "burst.view.last.load.at"

  /**
   * If there was an error condition detected during the most recent load
   */
  final val ViewLastLoadInvalidProperty: VitalsPropertyKey = "burst.view.last.load.invalid"

  /**
   * The epoch timestamp after which the data will be considered stale and reloaded (in milliseconds).
   * This is computed by adding `burst.view.next.load.stale` to the time of the most recent load
   */
  final val ViewLastLoadStaleMsProperty: VitalsPropertyKey = "burst.view.last.load.stale"

  /**
   *  The duration of the slowest slice of the most recent cold load (in milliseconds)
   */
  final val ViewLastColdLoadTookProperty: VitalsPropertyKey = "burst.view.last.load.took"

  /**
   * The number of items that the store expected this generation to contain
   */
  final val ViewLastExpectedItemCountProperty: VitalsPropertyKey = "burst.view.last.expected.item.count"

  /**
   * The potential number of items the generation would hold without sampling or size constraints
   */
  final val ViewLastPotentialItemCountProperty: VitalsPropertyKey = "burst.view.last.potential.item.count"

  /**
   * The number of items that were discarded by the remote store during the load process
   */
  final val ViewLastRejectedItemCountProperty: VitalsPropertyKey = "burst.view.last.rejected.item.count"

  /**
   * The number of slices during the most recent load
   */
  final val ViewLastSliceCountProperty: VitalsPropertyKey = "burst.view.last.slice.count"

}
