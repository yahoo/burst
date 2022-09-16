/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.tablet

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.felt.model.collectors.tablet.{FeltDefaultTabletSize, FeltTabletBuilder, FeltTabletBuilderContext}
import org.burstsys.tesla.TeslaTypes.{SizeOfDouble, TeslaMemorySize}

trait ZapTabletBuilder extends FeltTabletBuilder

object ZapTabletBuilder {

  def apply(itemSize: Int = SizeOfDouble): ZapTabletBuilder = ZapTabletBuilderContext(itemSize)

}

/**
 * parameters for building zap shrubs
 */
private final case
class ZapTabletBuilderContext(itemSize:  Int) extends FeltTabletBuilderContext with ZapTabletBuilder {

  override def requiredMemorySize: TeslaMemorySize = FeltDefaultTabletSize

  ///////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ///////////////////////////////////////////////////////////////////////////////////

  override def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
  }

  override def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
  }

}
