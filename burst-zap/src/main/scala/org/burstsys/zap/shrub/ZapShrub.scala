/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.shrub

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.felt.model.collectors.shrub.FeltShrubCollector
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.block.TeslaBlockPart
import org.burstsys.tesla.flex.TeslaFlexCollector
import org.burstsys.tesla.pool.TeslaPoolId
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.zap.shrub.state.ZapShrubState

/**
 * collector for returning tree structured result sets
 */
trait ZapShrub extends Any with FeltShrubCollector with TeslaBlockPart
  with TeslaFlexCollector[ZapShrubBuilder, ZapShrub] {

}

/**
 *
 * @param blockPtr
 */
final case
class ZapShrubAnyVal(blockPtr: TeslaMemoryPtr) extends AnyVal with ZapShrub with ZapShrubState {

  override def importCollector(sourceCollector: ZapShrub, sourceItems: Int, builder: ZapShrubBuilder): Unit = ???

  override def defaultBuilder: ZapShrubBuilder = throw VitalsException(s"default builder not allowed")

  override def builder: ZapShrubBuilder = ZapShrubBuilder()

  override def currentMemorySize: TeslaMemorySize = ???

  override def size(): TeslaMemorySize = ???

  override def initialize(pId: TeslaPoolId, builder: ZapShrubBuilder): Unit = ???

  override def reset(builder: ZapShrubBuilder): Unit = ???

  override def poolId: TeslaPoolId = ???
  ////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////

  override def write(kryo: Kryo, output: Output): Unit = ???

  override def read(kryo: Kryo, input: Input): Unit = ???

  override def itemCount: TeslaPoolId = ???

  override def itemCount_=(count: TeslaPoolId): Unit = ???

  override def itemLimited: Boolean = ???

  override def itemLimited_=(s: Boolean): Unit = ???

  override def clear(): Unit = ???

  override def isEmpty: Boolean = ???
}
