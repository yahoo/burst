/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2

import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector, FeltCubePlan, FeltCubeProvider}
import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.zap.cube.plan.ZapCubePlan

final case
class ZapCube2Provider() extends FeltCubeProvider {

  override def newBuilder: ZapCube2Builder =
    ZapCube2Builder()

  override def builderClassName: String =
    classOf[ZapCube2Builder].getName

  override def collectorClass: Class[ZapCube2] =
    classOf[ZapCube2]

  override def collectorPlan(decl: FeltCubeDecl): FeltCubePlan =
    ZapCubePlan(decl)

  override def grabCollector(builder: FeltCubeBuilder, desiredSize: TeslaMemorySize): FeltCubeCollector = {
    // cube2.factory.grabCube2(builder.asInstanceOf[ZapCube2Builder])
    val cubeBuilder = builder.asInstanceOf[ZapCube2Builder]
    flex.grabFlexCube(cubeBuilder, desiredSize)
  }

  override def releaseCollector(collector: FeltCubeCollector): Unit = {
    // cube2.factory.releaseCube2(collector.asInstanceOf[ZapCube2])
    flex.releaseFlexCube(collector.asInstanceOf[ZapCube2])
  }

}
