/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.shrub

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.felt.model.collectors.shrub.{FeltShrubBuilder, FeltShrubBuilderContext}
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemorySize}

trait ZapShrubBuilder extends FeltShrubBuilder

object ZapShrubBuilder {

  def apply(): ZapShrubBuilder = ZapShrubBuilderContext()

}

/**
 * parameters for building zap shrubs
 */
final case
class ZapShrubBuilderContext() extends FeltShrubBuilderContext with ZapShrubBuilder {
  var defaultStartSize: TeslaMemorySize = flex.defaultStartSize

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
