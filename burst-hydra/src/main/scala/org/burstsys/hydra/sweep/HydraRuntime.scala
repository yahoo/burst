/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.sweep

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.fabric.wave.execution.model.execute.invoke.FabricInvocation
import org.burstsys.felt.binding.FeltBinding
import org.burstsys.felt.model.runtime.{FeltPlaneRuntimeContext, FeltRuntime}
import org.burstsys.ginsu.runtime.GinsuRuntime

/**
 * the base class for code generated hydra runtime (contained within the code generated sweep)
 *
 */
abstract
class HydraRuntime(invocation: FabricInvocation)
  extends FeltPlaneRuntimeContext(invocation)
    with FeltRuntime with GinsuRuntime {

  @inline override implicit def threadRuntime: BrioThreadRuntime = this

  final val binding: FeltBinding = HydraFeltBinding

}

