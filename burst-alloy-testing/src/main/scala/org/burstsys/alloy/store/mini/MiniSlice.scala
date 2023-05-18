/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.store.mini

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.wave.data.model.slice.{FabricGenerationHash, FabricSlice, FabricSliceContext, FabricSliceCount, FabricSliceKey}
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.vitals.uid._

/**
 *
 */
trait MiniSlice extends FabricSlice {


}

object MiniSlice {

  def apply(
             guid: VitalsUid,
             sliceKey: FabricSliceKey,
             sliceHash: FabricGenerationHash,
             slices: FabricSliceCount,
             datasource: FabricDatasource,
             motifFilter: String,
             worker: FabricWorkerNode
           ): MiniSlice = MiniSliceContext(
    guid: VitalsUid,
    sliceKey: FabricSliceKey,
    sliceHash: FabricGenerationHash,
    slices: FabricSliceCount,
    datasource: FabricDatasource,
    motifFilter: String,
    worker: FabricWorkerNode
  )

}

final case
class MiniSliceContext(
                        var guid: VitalsUid,
                        var sliceKey: FabricSliceKey,
                        var generationHash: FabricGenerationHash,
                        var slices: FabricSliceCount,
                        var datasource: FabricDatasource,
                        var motifFilter: String,
                        var worker: FabricWorkerNode
                      ) extends FabricSliceContext() with MiniSlice {

  def this() = this(null, 0, null, 0, null, null, null)

  ///////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ///////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
  }

}
