/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.store.exceptional

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.wave.data.model.slice.{FabricGenerationHash, FabricSlice, FabricSliceContext, FabricSliceCount, FabricSliceKey}
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.vitals.uid._

trait ExceptionalSlice extends FabricSlice {
  def failureMode: StoreFailureMode
}


object ExceptionalSlice {
  def apply(
             guid: VitalsUid,
             sliceKey: FabricSliceKey,
             sliceHash: FabricGenerationHash,
             slices: FabricSliceCount,
             datasource: FabricDatasource,
             worker: FabricWorkerNode,
             failureMode: StoreFailureMode
           ): ExceptionalSlice = {
    ExceptionalSliceContext(
      guid,
      sliceKey,
      sliceHash: FabricGenerationHash,
      slices,
      datasource,
      "no-filter",
      worker,
      failureMode
    )
  }
}

final case
class ExceptionalSliceContext(
                               var guid: VitalsUid,
                               var sliceKey: FabricSliceKey,
                               var generationHash: FabricGenerationHash,
                               var slices: FabricSliceCount,
                               var datasource: FabricDatasource,
                               var motifFilter: String,
                               var worker: FabricWorkerNode,
                               var failureMode: StoreFailureMode
                             ) extends FabricSliceContext() with ExceptionalSlice {

  ///////////////////////////////////////////////////////////////////
  // KRYO SERDE
  ///////////////////////////////////////////////////////////////////

  override
  def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    failureMode = kryo.readClassAndObject(input).asInstanceOf[StoreFailureMode]
  }

  override
  def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    kryo.writeClassAndObject(output, failureMode)
  }

}
