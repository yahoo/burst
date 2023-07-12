/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet

import org.burstsys.felt.model.collectors.runtime.{FeltCollectorBuilder, FeltCollectorBuilderContext, FeltCollectorPlane}
import org.burstsys.felt.model.collectors.tablet.plane.FeltTabletPlaneContext
import org.burstsys.tesla.TeslaTypes.TeslaMemorySize

trait FeltTabletBuilder extends FeltCollectorBuilder {
}


abstract
class FeltTabletBuilderContext extends FeltCollectorBuilderContext with FeltTabletBuilder {

  final override def collectorPlaneClass[C <: FeltCollectorPlane[_, _]]: Class[C] =
    classOf[FeltTabletPlaneContext].asInstanceOf[Class[C]]

  override def defaultStartSize: TeslaMemorySize = FeltDefaultTabletSize

}

