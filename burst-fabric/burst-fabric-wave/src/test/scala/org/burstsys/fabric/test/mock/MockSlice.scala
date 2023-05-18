/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.mock

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.wave.data.model.slice.{FabricGenerationHash, FabricSlice, FabricSliceContext, FabricSliceCount, FabricSliceKey}
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.vitals.uid._

trait MockSlice extends FabricSlice

object MockSlice {

  def apply(
             guid: VitalsUid,
             sliceKey: FabricSliceKey,
             generationHash: FabricGenerationHash,
             slices: FabricSliceCount,
             datasource: FabricDatasource,
             motifFilter: String,
             worker: FabricWorkerNode
           ): MockSlice = MockSliceContext(
    guid: VitalsUid,
    sliceKey: FabricSliceKey,
    generationHash: FabricGenerationHash,
    slices: FabricSliceCount,
    datasource: FabricDatasource,
    motifFilter: String,
    worker: FabricWorkerNode
  )

}

final case
class MockSliceContext(
                        var guid: VitalsUid,
                        var sliceKey: FabricSliceKey,
                        var generationHash: FabricGenerationHash,
                        var slices: FabricSliceCount,
                        var datasource: FabricDatasource,
                        var motifFilter: String,
                        var worker: FabricWorkerNode
                      ) extends FabricSliceContext() with MockSlice {

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
