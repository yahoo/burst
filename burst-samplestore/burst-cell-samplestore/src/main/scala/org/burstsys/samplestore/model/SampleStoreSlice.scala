/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.model

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.data.model.slice.{FabricGenerationHash, FabricSlice, FabricSliceContext, FabricSliceCount, FabricSliceKey}
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.vitals.uid._

/**
 *
 */
trait SampleStoreSlice extends Any with FabricSlice {

  /**
   * the set of '''loci''' (the set of locus each of which is a
   * sample source remote nexus stream feed location.
   *
   * @return
   */
  def loci: Array[SampleStoreLocus]

}

object SampleStoreSlice {

  def apply(
             guid: VitalsUid,
             sliceKey: FabricSliceKey,
             generationHash: FabricGenerationHash,
             slices: FabricSliceCount,
             datasource: FabricDatasource,
             motifFilter: String,
             worker: FabricWorkerNode,
             loci: Array[SampleStoreLocus]
           ): SampleStoreSlice = SampleStoreSliceContext(
    guid: VitalsUid,
    sliceKey: FabricSliceKey,
    generationHash: FabricGenerationHash,
    slices: FabricSliceCount,
    datasource: FabricDatasource,
    motifFilter: String,
    worker: FabricWorkerNode,
    loci: Array[SampleStoreLocus]
  )

}

final case
class SampleStoreSliceContext(
                               var guid: VitalsUid,
                               var sliceKey: FabricSliceKey,
                               var generationHash: FabricGenerationHash,
                               var slices: FabricSliceCount,
                               var datasource: FabricDatasource,
                               var motifFilter: String,
                               var worker: FabricWorkerNode,
                               var loci: Array[SampleStoreLocus]
                             )
  extends FabricSliceContext() with SampleStoreSlice {

  def this() = this(null, 0,  null, 0, null, null, null, null)

  ///////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ///////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    val lociLength = input.readInt
    loci = new Array[SampleStoreLocus](lociLength)
    var i = 0
    while (i < lociLength) {
      loci(i) = kryo.readClassAndObject(input).asInstanceOf[SampleStoreLocus]
      i += 1
    }
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    output.writeInt(loci.length)
    var i = 0
    while (i < loci.length) {
      kryo.writeClassAndObject(output, loci(i))
      i += 1
    }
  }

}
