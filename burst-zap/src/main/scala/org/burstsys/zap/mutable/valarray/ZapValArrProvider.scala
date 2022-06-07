/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.mutable.valarray

import org.burstsys.felt.model.mutables.valarr.{FeltMutableValArr, FeltMutableValArrBuilder, FeltMutableValArrProv}
import org.burstsys.zap

final case
class ZapValArrProvider() extends FeltMutableValArrProv {

  override def newBuilder: FeltMutableValArrBuilder = ZapValArrayBuilder()

  override def builderClassName: String = classOf[ZapValArrayBuilder].getName

  override def mutableClass: Class[_ <: FeltMutableValArr] = classOf[ZapValArr]

  override def grabMutable(builder: FeltMutableValArrBuilder): FeltMutableValArr =
    zap.mutable.valarray.factory.grabValArray(builder.asInstanceOf[ZapValArrayBuilder])

  override def releaseMutable(mutable: FeltMutableValArr): Unit =
    zap.mutable.valarray.factory.releaseValArray(mutable.asInstanceOf[ZapValArr])

}
