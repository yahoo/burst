/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.fabric.execution.model.execute.invoke.FabricInvocation
import org.burstsys.felt.binding.FeltBinding
import org.burstsys.felt.model.collectors.FeltCollectorProviders
import org.burstsys.felt.model.mutables.FeltMutableProviders
import org.burstsys.felt.model.runtime.{FeltPlaneRuntimeContext, FeltRuntime}
import org.burstsys.felt.model.sweep.FeltSweep
import org.burstsys.ginsu.runtime.GinsuRuntime
import org.burstsys.zap.{ZapCollectorProviders, ZapMutableProviders}

package object sweep {

  /**
   *
   */
  object HydraFeltBinding extends FeltBinding {

    final override val sweepRuntimeClass: Class[_ <: FeltRuntime] = classOf[HydraRuntime]

    final override val sweepClass: Class[_ <: FeltSweep] = classOf[HydraSweep]

    final override def name: String = "hydra"

    final override val mutables: FeltMutableProviders = ZapMutableProviders

    final override def collectors: FeltCollectorProviders = ZapCollectorProviders
  }

}
