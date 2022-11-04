/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.slice

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.vitals.uid._

/**
 * ==Slices==
 * A '''Fabric Slice''' is a self contained '''specification''' of a single partition of a Burst dataset created from a domain/view
 * combination. Each '''Fabric Slice''' is hosted on one (or more) worker nodes and they may at any time be ''redistributed'' to
 * a different worker node in the face of dynamic failure or proactive performance optimizations.
 * ===Fabric Stores===
 * Each '''Fabric Store''' defines and implements a '''Fabric Slice''' subtype to contain the information is requires for its own
 * purposes. That specialized information is passed to the appropriate worker-side '''Fabric Store''' implementation at runtime.
 * ===Fabric Slice Identity===
 * The '''Fabric Slice''' must contain enough information to both support a full cold load of the associated dataset
 * partition, as well as be deterministic in their identity. A slice in a cache must be identifiable uniquely such that
 * a hot, warm, or cold load all work correctly no matter how failures retarget slice requests to different workers.
 * Put another way a slice request should always identify the exact same data every time it is used to do a hot, warm,
 * or cold load for any reason at any time. It is up to the '''Fabric Store''' that implements the slice to provide
 * whatever information is necessary to enforce these semantics. '''NOTE:''' a dataset may get ''repartitioned''
 * (''resliced'') and in that case any old slice data must be ignored.
 */
trait FabricSlice extends Any with Equals with FabricSliceIdentity {

  /**
   * global option uid for the slice (including the load operation)
   * Generally speaking, this is likely __not__ to be part of the slice identity.
   *
   * @return
   */
  def guid: VitalsUid

  /**
   * how many slices in this dataset
   *
   * @return
   */
  def slices: FabricSliceCount

  /**
   * the ip address for the hosting node
   *
   * @return
   */
  def worker: FabricWorkerNode

  /**
   * motif filter if any. This may or may not be useful to establish slice identity.
   *
   * @return
   */
  def motifFilter: String

  /**
   * @return s"domainKey viewKey generationClock hash"
   */
  def identity: String = s"domainKey=${datasource.domain.domainKey} viewKey=${datasource.view.viewKey} generationClock=${datasource.view.generationClock} generationHash=${generationHash}"

}

object FabricSlice {

  /**
   * this is for convenience in unit tests...
   *
   * @param guid
   * @param datasource
   * @param sliceKey
   * @param worker
   * @param motifFilter
   */
  final case
  class MockSlice(
                   var guid: VitalsUid,
                   var datasource: FabricDatasource,
                   var sliceKey: FabricSliceKey,
                   var generationHash: FabricGenerationHash,
                   var slices: FabricSliceCount,
                   var worker: FabricWorkerNode,
                   var motifFilter: String
                 ) extends FabricSliceContext() {

    def this() = this(null, null, 0, null, 0, null, null)

  }

  /**
   * constructor for unit tests
   *
   * @param guid
   * @param datasource
   * @param sliceKey
   * @param worker
   * @param motifFilter
   * @return
   */
  def apply(guid: VitalsUid,
            datasource: FabricDatasource,
            sliceKey: FabricSliceKey,
            generationHash: FabricGenerationHash,
            slices: FabricSliceCount,
            worker: FabricWorkerNode,
            motifFilter: String = ""
           ): FabricSlice =
    MockSlice(
      guid = guid,
      datasource = datasource,
      sliceKey = sliceKey,
      generationHash = generationHash,
      slices = slices,
      worker = worker,
      motifFilter = motifFilter
    )
}

abstract
class FabricSliceContext() extends AnyRef with KryoSerializable with FabricSlice {

  def parameters = s"guid=$guid, sliceKey=$sliceKey, slices=$slices, workerAddress=$worker, datasource=$datasource"

  ///////////////////////////////////////////////////////////////////
  // setters
  ///////////////////////////////////////////////////////////////////

  protected def guid_=(uid: VitalsUid): Unit

  protected def datasource_=(ds: FabricDatasource): Unit

  protected def sliceKey_=(key: FabricSliceKey): Unit

  protected def generationHash_=(hash: FabricGenerationHash): Unit

  protected def slices_=(count: FabricSliceCount): Unit

  protected def motifFilter_=(filter: String): Unit

  protected def worker_=(worker: FabricWorkerNode): Unit

  override def toString: String =  s"FabricSlice($parameters)"

  ///////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    guid = input.readString
    datasource = kryo.readClassAndObject(input).asInstanceOf[FabricDatasource]
    sliceKey = input.readInt
    generationHash = input.readString()
    slices = input.readInt
    worker = kryo.readClassAndObject(input).asInstanceOf[FabricWorkerNode]
    motifFilter = input.readString
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    output.writeString(guid)
    kryo.writeClassAndObject(output, datasource)
    output.writeInt(sliceKey)
    output.writeString(generationHash)
    output.writeInt(slices)
    kryo.writeClassAndObject(output, worker.forExport)
    output.writeString(motifFilter)
  }
}
