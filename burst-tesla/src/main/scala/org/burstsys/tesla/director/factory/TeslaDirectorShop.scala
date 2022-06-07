/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.director.factory

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.director
import org.burstsys.tesla.director.{TeslaDirector, TeslaDirectorBuilder}
import org.burstsys.tesla.part.TeslaPartShop

/**
  * tesla part shop for director objects
  */
trait TeslaDirectorShop extends TeslaPartShop[TeslaDirector, TeslaDirectorBuilder] {

  def partName: String = director.partName

  /**
    * grab a tesla director
    *
    * @param size
    * @return
    */
  def grabDirector(size: TeslaMemorySize): TeslaDirector

  /**
    * release a tesla director
    *
    * @param director
    */
  def releaseDirector(director: TeslaDirector): Unit

}
