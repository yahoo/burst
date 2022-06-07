/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2

import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.cube.{FeltCubeBuilder, FeltCubeCollector, FeltCubePlan, FeltCubeProvider}
import org.burstsys.zap.{cube, cube2}
import org.burstsys.zap.cube.plan.ZapCubePlan
import org.burstsys.zap.cube.{ZapCube, ZapCubeBuilder, ZapCubeContext}

final case
class ZapCube2Provider() extends FeltCubeProvider {

  override def newBuilder: ZapCube2Builder = ZapCube2Builder()

  override def builderClassName: String = classOf[ZapCube2Builder].getName

  override def collectorClass: Class[ZapCube2] = classOf[ZapCube2]

  override def collectorPlan(decl: FeltCubeDecl): FeltCubePlan = ZapCubePlan(decl)

  override def grabCollector(builder: FeltCubeBuilder): FeltCubeCollector = cube2.factory.grabCube2(builder.asInstanceOf[ZapCube2Builder])

  override def releaseCollector(collector: FeltCubeCollector): Unit = cube2.factory.releaseCube2(collector.asInstanceOf[ZapCube2])

}
