/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.shrub

import org.burstsys.felt.model.collectors.runtime.{FeltCollectorBuilder, FeltCollectorBuilderContext, FeltCollectorPlane}

trait FeltShrubBuilder extends FeltCollectorBuilder {

}

abstract
class FeltShrubBuilderContext extends FeltCollectorBuilderContext with FeltShrubBuilder {

  final override def collectorPlaneClass[C <: FeltCollectorPlane[_, _]]: Class[C] =
    classOf[FeltShrubBuilder].asInstanceOf[Class[C]]

}
