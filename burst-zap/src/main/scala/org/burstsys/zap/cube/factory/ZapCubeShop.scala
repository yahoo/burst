/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube.factory

import org.burstsys.tesla.part.TeslaPartShop
import org.burstsys.zap.cube
import org.burstsys.zap.cube.{ZapCubeBuilder, ZapCubeContext}

trait ZapCubeShop extends TeslaPartShop[ZapCubeContext, ZapCubeBuilder] {

  def partName: String = cube.partName

  /**
   *
   * @param builder
   * @return
   */
  def grabZapCube(builder: ZapCubeBuilder): ZapCubeContext

  /**
   *
   * @param cube
   */
  def releaseZapCube(cube: ZapCubeContext): Unit

}
