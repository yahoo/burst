/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valmap

import org.burstsys.felt.model.mutables.valmap.{FeltMutableValMap, FeltMutableValMapBuilder, FeltMutableValMapProv}
import org.burstsys.zap

final case
class ZapValMapProvider() extends FeltMutableValMapProv  {
  override def newBuilder: FeltMutableValMapBuilder = ZapValMapBuilder()

  override def builderClassName: String = classOf[ZapValMapBuilder].getName

  override def mutableClass: Class[_ <: FeltMutableValMap] = classOf[ZapValMap]

  override def grabMutable(builder: FeltMutableValMapBuilder): FeltMutableValMap =
    zap.mutable.valmap.factory.grabValMap(builder.asInstanceOf[ZapValMapBuilder])

  override def releaseMutable(mutable: FeltMutableValMap): Unit =
    zap.mutable.valmap.factory.releaseValMap(mutable.asInstanceOf[ZapValMap])
}
