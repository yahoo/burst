/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.shrub.generate.splice

import org.burstsys.felt.model.collectors.shrub.decl.FeltShrubDecl
import org.burstsys.felt.model.collectors.tablet.decl.FeltTabletDecl
import org.burstsys.felt.model.sweep.splice._

import scala.language.implicitConversions

/**
 * generate splices for a [[FeltTabletDecl]]
 */
trait FeltShrubSplicer extends FeltSplicer {

}

object FeltShrubSplicer {
  def apply(shrub: FeltShrubDecl): FeltShrubSplicer =
    FeltShrubSplicerContext(shrub: FeltShrubDecl)
}

private final case
class FeltShrubSplicerContext(shrub: FeltShrubDecl) extends FeltShrubSplicer
  with FeltSpliceStore {


  override def collectSplices: Array[FeltSplice] = Array.empty

}
