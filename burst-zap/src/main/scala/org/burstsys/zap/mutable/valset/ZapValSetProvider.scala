/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valset

import org.burstsys.felt.model.mutables.valset.{FeltMutableValSet, FeltMutableValSetBuilder, FeltMutableValSetProv}
import org.burstsys.zap

final case
class ZapValSetProvider() extends FeltMutableValSetProv {

  override def newBuilder: FeltMutableValSetBuilder = ZapValSetBuilder()

  override def builderClassName: String = classOf[ZapValSetBuilder].getName

  override def mutableClass: Class[_ <: FeltMutableValSet] = classOf[ZapValSet]

  override def grabMutable(builder: FeltMutableValSetBuilder): FeltMutableValSet =
    zap.mutable.valset.factory.grabValSet(builder.asInstanceOf[ZapValSetBuilder])

  override def releaseMutable(mutable: FeltMutableValSet): Unit =
    zap.mutable.valset.factory.releaseValSet(mutable.asInstanceOf[ZapValSet])

}
