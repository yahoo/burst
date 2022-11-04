/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.test.mock

import org.burstsys.brio.types.BrioPath.BrioPathKey
import org.burstsys.fabric.wave.execution.model.execute.invoke.FabricInvocation
import org.burstsys.felt.binding.FeltBinding
import org.burstsys.felt.model.collectors.runtime.FeltCollectorBuilder
import org.burstsys.felt.model.runtime.FeltRuntime
import org.burstsys.felt.model.schema.traveler.FeltTraveler
import org.burstsys.felt.model.sweep.FeltSweep
import org.burstsys.felt.model.sweep.splice.FeltPlacementKey


case
class MockSweep() extends FeltSweep {
  override val sweepName: String = "MockSweep"
  override val sweepClassName: String = "MockSweepClass"
  override val feltTraveler: FeltTraveler[_] = new MockTraveler
  override val feltBinding: FeltBinding = MockBinding

  override def apply(runtime: FeltRuntime): Unit = {

  }

  override def newRuntime(call: FabricInvocation): FeltRuntime = {
    ???
  }

  override def rootSplice(runtime: FeltRuntime, path: BrioPathKey, placement: FeltPlacementKey): Unit = {

  }

  override def collectorBuilders: Array[_ <: FeltCollectorBuilder] = Array.empty

  override def dynamicRelationSplices(runtime: FeltRuntime, pathKey: BrioPathKey, placement: FeltPlacementKey): Unit = {

  }

}
