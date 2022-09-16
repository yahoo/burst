/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.shrub

import org.burstsys.felt.model.collectors.shrub.decl.FeltShrubDecl
import org.burstsys.felt.model.collectors.shrub.{FeltShrubBuilder, FeltShrubCollector, FeltShrubPlan, FeltShrubProvider}
import org.burstsys.tesla.TeslaTypes.TeslaMemorySize

final case
class ZapShrubProvider() extends FeltShrubProvider {
  override def newBuilder: FeltShrubBuilder = ???

  override def builderClassName: String = ???

  override def collectorClass: Class[_ <: FeltShrubCollector] = ???

  override def collectorPlan(decl: FeltShrubDecl): FeltShrubPlan = ???

  override def grabCollector(builder: FeltShrubBuilder, desiredSize: TeslaMemorySize): FeltShrubCollector = ???

  override def releaseCollector(collector: FeltShrubCollector): Unit = ???
}
