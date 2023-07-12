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

final case
class ZapShrubAnyVal(blockPtr: TeslaMemoryPtr) extends AnyVal with ZapShrub with ZapShrubState {

  override def importCollector(sourceCollector: ZapShrub, sourceItems: Int, builder: ZapShrubBuilder): Unit = throw new UnsupportedOperationException(s"import collector not implemented")

  override def defaultBuilder: ZapShrubBuilder = throw VitalsException(s"default builder not allowed")

  override def builder: ZapShrubBuilder = ZapShrubBuilder()

  override def currentMemorySize: TeslaMemorySize = throw new UnsupportedOperationException(s"currentMemorySize not allowed")

  override def size(): TeslaMemorySize = throw new UnsupportedOperationException(s"size not allowed")

  override def initialize(pId: TeslaPoolId, builder: ZapShrubBuilder): Unit = throw new UnsupportedOperationException(s"initialize not allowed")

  override def reset(builder: ZapShrubBuilder): Unit = throw new UnsupportedOperationException(s"reset not allowed")

  override def poolId: TeslaPoolId = throw new UnsupportedOperationException(s"poolId not allowed")

  ////////////////////////////////////////////////////////////////////////////////////
  // KRYO SERIALIZATION
  ////////////////////////////////////////////////////////////////////////////////////

  override def write(kryo: Kryo, output: Output): Unit = throw new UnsupportedOperationException(s"write not allowed")

  override def read(kryo: Kryo, input: Input): Unit = throw new UnsupportedOperationException(s"read not allowed")

  override def itemCount: TeslaPoolId = throw new UnsupportedOperationException(s"itemCount not allowed")

  override def itemCount_=(count: TeslaPoolId): Unit = throw new UnsupportedOperationException(s"itemCount not allowed")

  override def itemLimited: Boolean = throw new UnsupportedOperationException(s"itemLimited not allowed")

  override def itemLimited_=(s: Boolean): Unit = throw new UnsupportedOperationException(s"itemLimited not allowed")

  override def clear(): Unit = throw new UnsupportedOperationException(s"clear not allowed")

  override def isEmpty: Boolean = throw new UnsupportedOperationException(s"isEmpty not allowed")
}
