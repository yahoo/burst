/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.test.mock

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.fabric.execution.model.execute.invoke.FabricInvocation
import org.burstsys.felt.binding.FeltBinding
import org.burstsys.felt.model.runtime.{FeltPlaneRuntimeContext, FeltRuntime}
import org.burstsys.ginsu.runtime.GinsuRuntime

abstract
class MockRuntime(invocation: FabricInvocation)
  extends FeltPlaneRuntimeContext(invocation)
    with FeltRuntime with GinsuRuntime {
  @inline override implicit def threadRuntime: BrioThreadRuntime = this

  final val binding: FeltBinding = MockBinding
}
