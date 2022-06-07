/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube

import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector, FeltCubePlan, FeltCubeProvider}
import org.burstsys.zap.cube
import org.burstsys.zap.cube.plan.ZapCubePlan

final case
class ZapCubeProvider() extends FeltCubeProvider  {

  override def newBuilder: ZapCubeBuilder = ZapCubeBuilder()

  override def builderClassName: String = classOf[ZapCubeBuilder].getName

  override def collectorClass: Class[ZapCube] = classOf[ZapCube]

  override def collectorPlan(decl: FeltCubeDecl): FeltCubePlan = ZapCubePlan(decl)

  override def grabCollector(builder: FeltCubeBuilder): FeltCubeCollector = cube.factory.grabZapCube(builder.asInstanceOf[ZapCubeBuilder])

  override def releaseCollector(collector: FeltCubeCollector): Unit = cube.factory.releaseZapCube(collector.asInstanceOf[ZapCubeContext])

}
