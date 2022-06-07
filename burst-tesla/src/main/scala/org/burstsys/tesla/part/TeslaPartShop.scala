/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.part

/**
 * Marker Trait for all part shops
 */
trait TeslaPartShop[ShopPart <: TeslaPart, ShopBuilder <: TeslaPartBuilder] extends Any {

  def partName: String

}
