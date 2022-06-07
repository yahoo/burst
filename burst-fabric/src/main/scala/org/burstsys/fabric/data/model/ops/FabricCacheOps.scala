/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.model.ops

import org.burstsys.fabric.data.model.generation.FabricGeneration
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.data.model.slice.metadata.FabricSliceMetadata
import org.burstsys.vitals.uid.VitalsUid

import scala.concurrent.Future

/**
 * master and worker side API for ops
 * associated with the worker side [[org.burstsys.fabric.data.worker.cache.FabricSnapCache]]
 */
trait FabricCacheOps extends Any {

  /**
   * Performs a cache operation on generations matching the metadata provided.
   * Any part of the generation key that is unspecified is interpreted as a wildcard.
   * Any parameters passed are ANDed together
   *
   * @param guid          a unique identifier for this request
   * @param operation     the operation to perform
   * @param generationKey the generation(s) to operate over
   * @param parameters    parameters to further restrict the operation
   * @return the set of generations affected by this operation
   */
  def cacheGenerationOp(
                         guid: VitalsUid,
                         operation: FabricCacheManageOp,
                         generationKey: FabricGenerationKey,
                         parameters: Option[Seq[FabricCacheOpParameter]]
                       ): Future[Seq[FabricGeneration]]

  /**
   * Returns the slices for a particular data generation.
   *
   * @param guid          a unique identifier for this request
   * @param generationKey the specifier for the generation to fetch slices for
   * @return
   */
  def cacheSliceOp(guid: VitalsUid, generationKey: FabricGenerationKey): Future[Seq[FabricSliceMetadata]]

}
