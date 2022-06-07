/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.parcel.factory

import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.parcel
import org.burstsys.tesla.parcel.{TeslaParcel, TeslaParcelBuilder}
import org.burstsys.tesla.part.TeslaPartShop

/**
  * the tesla part shop for parcels
  */
trait TeslaParcelShop extends TeslaPartShop[TeslaParcel, TeslaParcelBuilder] {

  def partName: String =  parcel.partName

  /**
    * grab a parcel
    *
    * @param size of parcel needed
    * @return
    */
  def grabParcel(size: TeslaMemorySize): TeslaParcel

  /**
    * release the parcel back to the pool
    *
    * @param parcel to release
    */
  def releaseParcel(parcel: TeslaParcel): Unit

}
