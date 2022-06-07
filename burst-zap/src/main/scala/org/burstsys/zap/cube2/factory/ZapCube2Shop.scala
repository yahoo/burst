/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2.factory

import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.part
import org.burstsys.tesla.part.{TeslaPartShop, teslaBuilderUseDefaultSize}
import org.burstsys.zap.cube2
import org.burstsys.zap.cube2.{ZapCube2, ZapCube2Builder}

/**
 * part shop for brio dictionaries
 */
trait ZapCube2Shop extends TeslaPartShop[ZapCube2, ZapCube2Builder] {

  def partName: String = cube2.partName

  def grabCube2(builder: ZapCube2Builder, startSize: TeslaMemorySize = teslaBuilderUseDefaultSize): ZapCube2

  def releaseCube2(cube: ZapCube2): Unit

}
