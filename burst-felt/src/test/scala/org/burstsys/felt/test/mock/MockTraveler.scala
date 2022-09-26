/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.test.mock

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.felt.model.schema.traveler.FeltTraveler
import org.burstsys.felt.model.sweep.FeltSweep


class MockTraveler extends FeltTraveler[MockRuntime] {
  override val travelerClassName: String = "MockTravelerClassName"
  override val runtimeClassName: String = "MockRuntimeClassName"
  override val brioSchema: BrioSchema = mockBrioSchema

  override def apply(runtime: MockRuntime, sweep: FeltSweep): Unit = {

  }
}

